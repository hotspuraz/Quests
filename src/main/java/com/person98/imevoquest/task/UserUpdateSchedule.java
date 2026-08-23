package com.person98.imevoquest.task;

import com.google.common.collect.Lists;
import com.person98.imevoquest.ImevoQuest;
import com.person98.imevoquest.controller.UserController;
import com.person98.imevoquest.data.User;
import java.util.List;
import org.bukkit.scheduler.BukkitRunnable;

public class UserUpdateSchedule extends BukkitRunnable {
   public void run() {
      UserController userController = ImevoQuest.getInstance().getUserController();
      List<User> available = Lists.newArrayList();
      userController.forEach(user -> {
         if (user.isAvailableToUpdate()) {
            available.add(user);
         }
      });
      if (!available.isEmpty()) {
         User[] users = new User[available.size()];
         users = available.toArray(users);
         ImevoQuest.getInstance().getStorage().saveUser(users);
      }
   }
}
