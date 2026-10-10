package com.person98.quests.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import com.person98.quests.data.stage.Stage;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IdentityEqualityTest {
   private static Quest quest(String id) {
      Quest quest = new Quest("Name of " + id, 5, true);
      quest.setIdentifier(id);
      return quest;
   }

   @Test
   void questsWithTheSameIdentifierAreEqualRegardlessOfContents() {
      Quest a = quest("intro");
      Quest b = quest("intro");
      b.setPoints(99);
      b.getStages().add(new Stage());
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertNotEquals(a, quest("other"));
   }

   @Test
   void questHashAndEqualsDoNotFollowTheStoryChain() {
      // A config typo can make two story quests point at each other. getNext() would need the plugin
      // instance (absent here), so any attempt to follow the chain would throw instead of returning.
      Quest a = quest("a");
      Quest b = quest("b");
      a.setNext("b");
      b.setNext("a");
      assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
         assertNotNull(a.hashCode());
         assertNotEquals(a, b);
         assertEquals(a, a);
      });
   }

   @Test
   void questStillFindsItsEntryAfterBeingMutated() {
      Map<Quest, Integer> stages = new HashMap<>();
      Quest quest = quest("mutable");
      stages.put(quest, 3);
      quest.setName("Renamed");
      quest.setPoints(50);
      quest.getStages().add(new Stage());
      assertEquals(3, stages.get(quest));
      assertEquals(3, stages.get(quest("mutable")));
   }

   @Test
   void questWithoutIdentifierOnlyEqualsItself() {
      Quest a = new Quest("x", 1, false);
      assertEquals(a, a);
      assertNotEquals(a, new Quest("x", 1, false));
   }

   @Test
   void usersAreEqualByUuidOnly() {
      UUID id = UUID.randomUUID();
      User a = new User(id);
      User b = new User(id);
      b.setPoints(100);
      b.setViewStatus(false);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertNotEquals(a, new User(UUID.randomUUID()));
   }

   @Test
   void snapshotEqualsItsSource() {
      User user = new User(UUID.randomUUID());
      user.setPoints(7);
      assertEquals(user, user.snapshot());
   }
}
