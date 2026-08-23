package com.person98.imevoquest.controller;

import com.person98.imevoquest.ImevoQuest;
import com.person98.imevoquest.data.User;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.bukkit.entity.Player;

public interface UserController {
   void constructor(ImevoQuest quests);

   void destructor(ImevoQuest quests);

   User find(UUID uniqueId);

   default User find(Player player) {
      return this.find(player.getUniqueId());
   }

   void create(UUID uniqueId);

   default void create(Player player) {
      this.create(player.getUniqueId());
   }

   void destroy(UUID uniqueId);

   default void destroy(Player player) {
      this.destroy(player.getUniqueId());
   }

   void forEach(Consumer<User> consumer);

   Stream<User> stream();

   void resetAllDailyQuestProgress();
}
