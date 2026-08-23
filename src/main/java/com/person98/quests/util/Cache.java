package com.person98.quests.util;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

public class Cache<K, V> {
   private final Map<K, V> internal = new HashMap<>();
   private final Multimap<Long, K> expiry = TreeMultimap.create(Long::compareTo, (left, right) -> {
      if (Objects.equals(left, right)) {
         return 0;
      }
      int comparison = String.valueOf(left).compareTo(String.valueOf(right));
      return comparison != 0 ? comparison : Integer.compare(System.identityHashCode(left), System.identityHashCode(right));
   });
   private final long retention;

   public Cache(long retention) {
      this.retention = retention;
   }

   public void put(K key, V value) {
      this.invalidate(key);
      synchronized (this.internal) {
         this.internal.put(key, value);
         this.expiry.put(System.currentTimeMillis() + this.retention, key);
      }
   }

   public Collection<V> values() {
      this.lazyCheck();
      synchronized (this.internal) {
         return this.internal.values();
      }
   }

   public V get(K key) {
      this.lazyCheck();
      synchronized (this.internal) {
         return this.internal.get(key);
      }
   }

   public boolean containsKey(K key) {
      this.lazyCheck();
      synchronized (this.internal) {
         return this.internal.containsKey(key);
      }
   }

   public void invalidate(K key) {
      this.lazyCheck();
      synchronized (this.internal) {
         if (this.internal.containsKey(key)) {
            this.internal.remove(key);
            Iterator<Entry<Long, K>> iterator = this.expiry.entries().iterator();

            while (iterator.hasNext()) {
               if (key.equals(iterator.next().getValue())) {
                  iterator.remove();
                  break;
               }
            }
         }
      }
   }

   public void invalidateAll() {
      synchronized (this.internal) {
         this.expiry.clear();
         this.internal.clear();
      }
   }

   private void lazyCheck() {
      long now = System.currentTimeMillis();
      synchronized (this.internal) {
         Iterator<Entry<Long, K>> iterator = this.expiry.entries().iterator();

         while (iterator.hasNext()) {
            Entry<Long, K> entry = iterator.next();
            if (entry.getKey() > now) {
               break;
            }

            iterator.remove();
            this.internal.remove(entry.getValue());
         }
      }
   }
}
