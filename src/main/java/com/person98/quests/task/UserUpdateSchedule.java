package com.person98.quests.task;

import com.google.common.collect.Lists;
import com.person98.quests.Quests;
import com.person98.quests.controller.UserController;
import com.person98.quests.data.User;
import java.util.List;
import org.bukkit.scheduler.BukkitRunnable;

public class UserUpdateSchedule extends BukkitRunnable {
   public void run() {
      UserController userController = Quests.getInstance().getUserController();
      List<User> available = Lists.newArrayList();
      userController.forEach(user -> {
         if (user.isAvailableToUpdate()) {
            available.add(user);
         }
      });
      if (!available.isEmpty()) {
         User[] users = new User[available.size()];
         users = available.toArray(users);
         Quests.getInstance().getStorage().saveUser(users);
      }
   }
}
