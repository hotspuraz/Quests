package com.person98.quests.service.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WriteQueueTest {
   private final List<LogRecord> records = new CopyOnWriteArrayList<>();
   private WriteQueue queue;

   @BeforeEach
   void setUp() {
      Logger logger = Logger.getAnonymousLogger();
      logger.setUseParentHandlers(false);
      logger.addHandler(new Handler() {
         @Override
         public void publish(LogRecord record) {
            WriteQueueTest.this.records.add(record);
         }

         @Override
         public void flush() {
         }

         @Override
         public void close() {
         }
      });
      this.queue = new WriteQueue(logger, Duration.ZERO);
   }

   @AfterEach
   void tearDown() {
      this.queue.shutdown(Duration.ofSeconds(2));
   }

   @Test
   void successfulWriteIsSilent() {
      AtomicInteger runs = new AtomicInteger();
      this.queue.submit("ok", runs::incrementAndGet, failure -> { });
      this.queue.flush();
      assertEquals(1, runs.get());
      assertTrue(this.records.isEmpty());
      assertEquals(0, this.queue.getFailureCount());
   }

   @Test
   void transientFailureIsRetriedAndWarnedButNotLost() {
      AtomicInteger runs = new AtomicInteger();
      List<Throwable> gaveUp = new ArrayList<>();
      this.queue.submit("flaky", () -> {
         if (runs.incrementAndGet() == 1) {
            throw new IllegalStateException("connection reset");
         }
      }, gaveUp::add);
      this.queue.flush();
      assertEquals(2, runs.get());
      assertTrue(gaveUp.isEmpty());
      assertEquals(0, this.queue.getFailureCount());
      assertEquals(1, this.records.size());
      assertEquals(Level.WARNING, this.records.get(0).getLevel());
   }

   @Test
   void permanentFailureIsLoggedSevereWithContextAndReported() {
      List<Throwable> gaveUp = new ArrayList<>();
      this.queue.submit("save 3 user(s)", () -> {
         throw new IllegalStateException("database is down");
      }, gaveUp::add);
      this.queue.flush();
      assertEquals(1, gaveUp.size());
      assertEquals(1, this.queue.getFailureCount());
      LogRecord last = this.records.get(this.records.size() - 1);
      assertEquals(Level.SEVERE, last.getLevel());
      assertTrue(last.getMessage().contains("save 3 user(s)"));
      assertEquals("database is down", last.getThrown().getMessage());
   }

   @Test
   void failedWriteDoesNotStopLaterWrites() {
      AtomicInteger runs = new AtomicInteger();
      this.queue.submit("bad", () -> {
         throw new IllegalStateException("boom");
      }, failure -> { });
      this.queue.submit("good", runs::incrementAndGet, failure -> { });
      this.queue.flush();
      assertEquals(1, runs.get());
   }

   @Test
   void giveUpHandlerThrowingIsContained() {
      this.queue.submit("bad", () -> {
         throw new IllegalStateException("boom");
      }, failure -> {
         throw new IllegalArgumentException("handler bug");
      });
      this.queue.flush();
      AtomicInteger runs = new AtomicInteger();
      this.queue.submit("good", runs::incrementAndGet, failure -> { });
      this.queue.flush();
      assertEquals(1, runs.get());
   }

   @Test
   void runNowWaitsForQueuedWritesAndReportsOutcome() {
      List<String> order = new CopyOnWriteArrayList<>();
      this.queue.submit("first", () -> order.add("queued"), failure -> { });
      assertTrue(this.queue.runNow("second", () -> order.add("sync")));
      assertEquals(List.of("queued", "sync"), order);
      assertFalse(this.queue.runNow("failing", () -> {
         throw new IllegalStateException("nope");
      }));
      assertEquals(1, this.queue.getFailureCount());
   }
}
