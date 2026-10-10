package com.person98.quests.service.sql;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Single-threaded writer for database work. Every failure is logged with a description of what was being
 * written; a failed write is retried once before it is reported as lost. Tasks must be idempotent (upserts).
 */
public class WriteQueue {
   private static final int ATTEMPTS = 2;

   private final Logger logger;
   private final Duration retryDelay;
   private final AtomicLong failures = new AtomicLong();
   private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
      Thread thread = new Thread(runnable, "Quests-SQL-Writer");
      thread.setDaemon(true);
      return thread;
   });

   public WriteQueue(Logger logger, Duration retryDelay) {
      this.logger = logger;
      this.retryDelay = retryDelay;
   }

   /** Runs the task on the writer thread. onGiveUp is called there too, only if every attempt failed. */
   public void submit(String description, Runnable task, Consumer<Throwable> onGiveUp) {
      this.executor.execute(() -> {
         if (!this.attempt(description, task)) {
            try {
               onGiveUp.accept(new IllegalStateException("Quest database write failed: " + description));
            } catch (RuntimeException exception) {
               this.logger.log(Level.SEVERE, "Quests failure handler threw for " + description, exception);
            }
         }
      });
   }

   /** Waits for queued writes, then runs the task on the calling thread. Returns whether it succeeded. */
   public boolean runNow(String description, Runnable task) {
      this.flush();
      return this.attempt(description, task);
   }

   private boolean attempt(String description, Runnable task) {
      for (int attempt = 1; attempt <= ATTEMPTS; attempt++) {
         try {
            task.run();
            return true;
         } catch (RuntimeException exception) {
            boolean last = attempt == ATTEMPTS;
            this.logger.log(
               last ? Level.SEVERE : Level.WARNING,
               "Quest database write failed (" + description + "), attempt " + attempt + "/" + ATTEMPTS + (last ? "; progress not saved" : "; retrying"),
               exception
            );
            if (last) {
               this.failures.incrementAndGet();
            } else if (!this.pause()) {
               this.failures.incrementAndGet();
               return false;
            }
         }
      }
      return false;
   }

   private boolean pause() {
      if (this.retryDelay.isZero()) {
         return true;
      }
      try {
         Thread.sleep(this.retryDelay.toMillis());
         return true;
      } catch (InterruptedException exception) {
         Thread.currentThread().interrupt();
         return false;
      }
   }

   /** Blocks until every write queued so far has finished (successfully or not). */
   public void flush() {
      try {
         this.executor.submit(() -> { }).get();
      } catch (InterruptedException exception) {
         Thread.currentThread().interrupt();
         throw new IllegalStateException("Interrupted while flushing quest progress", exception);
      } catch (ExecutionException exception) {
         throw new IllegalStateException("Could not flush quest progress", exception.getCause());
      }
   }

   public void shutdown(Duration timeout) {
      this.flush();
      this.executor.shutdown();
      try {
         if (!this.executor.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
            this.logger.warning("Quest database writer did not stop within " + timeout.toSeconds() + " seconds.");
         }
      } catch (InterruptedException exception) {
         Thread.currentThread().interrupt();
      }
   }

   /** Number of writes that were abandoned after all attempts failed. */
   public long getFailureCount() {
      return this.failures.get();
   }
}
