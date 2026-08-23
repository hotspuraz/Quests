package com.person98.quests.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import org.junit.jupiter.api.Test;

class CacheTest {
   @Test
   void valuesReturnsStableSnapshot() {
      Cache<String, String> cache = new Cache<>(60_000L);
      cache.put("first", "one");

      Collection<String> snapshot = cache.values();
      cache.put("second", "two");
      cache.invalidate("first");

      assertEquals(java.util.List.of("one"), snapshot.stream().toList());
   }
}
