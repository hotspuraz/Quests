package com.person98.quests.command;

import com.person98.quests.Quests;
import com.person98.quests.data.SimpleUser;
import com.person98.quests.service.sql.Storage;
import com.person98.quests.util.PageInfo;
import java.util.Iterator;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class QuestsTopCommand implements CommandExecutor {
   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
      int page = 1;
      if (args.length > 0) {
         try {
            page = Integer.parseInt(args[0]);
         } catch (NumberFormatException exception) {
         }
      }

      if (page < 0) {
         page = 1;
      }

      Storage storage = Quests.getInstance().getStorage();
      List<SimpleUser> leaderboard = storage.getLeaderboard();
      if (leaderboard.isEmpty() && storage.isRecalculating()) {
         sender.sendMessage(Component.text("Leaderboard is being recalculated, please wait a couple of seconds and try again.").color(NamedTextColor.GREEN));
         return true;
      } else {
         PageInfo pageInfo = new PageInfo(10, leaderboard.size(), page);
         Iterator<SimpleUser> iterator = leaderboard.iterator();
         sender.sendMessage(
            ((TextComponent)((TextComponent)((TextComponent)Component.text("----- Quests Top -----").decorate(TextDecoration.STRIKETHROUGH))
                     .color(NamedTextColor.AQUA))
                  .append(Component.text(" Quests Top ").color(NamedTextColor.DARK_AQUA)))
               .append(Component.text("-----").decorate(TextDecoration.STRIKETHROUGH))
         );

         while (iterator.hasNext()) {
            SimpleUser next = iterator.next();
            if (pageInfo.isEntryOk()) {
               if (pageInfo.isBreak()) {
                  break;
               }

               sender.sendMessage(
                  ((TextComponent)((TextComponent)Component.text(pageInfo.getPositionForOutput() + ". ").color(NamedTextColor.AQUA))
                        .append(Component.text(next.getName() + ": ").color(NamedTextColor.DARK_AQUA)))
                     .append(Component.text(next.points()).color(NamedTextColor.AQUA))
               );
            }
         }

         Builder builder = Component.text();
         builder.append(
            ((TextComponent)((TextComponent)Component.text("----<< Prev").color(NamedTextColor.GREEN))
                  .hoverEvent(HoverEvent.showText(Component.text(">|").color(NamedTextColor.GRAY))))
               .clickEvent(ClickEvent.runCommand("/queststop " + pageInfo.getPrevPageNumber()))
         );
         builder.append(Component.text(" "));
         builder.append(
            ((TextComponent)Component.text(pageInfo.getCurrentPage() + "/" + pageInfo.getTotalPages()).color(NamedTextColor.GREEN))
               .hoverEvent(HoverEvent.showText(Component.text(pageInfo.getTotalEntries() + " entries").color(NamedTextColor.AQUA)))
         );
         builder.append(Component.text(" "));
         builder.append(
            ((TextComponent)((TextComponent)Component.text("Next >>----").color(NamedTextColor.DARK_AQUA))
                  .hoverEvent(HoverEvent.showText(Component.text(">>>").color(NamedTextColor.GRAY))))
               .clickEvent(ClickEvent.runCommand("/queststop " + pageInfo.getNextPageNumber()))
         );
         sender.sendMessage(builder.build());
         return true;
      }
   }
}
