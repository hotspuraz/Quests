package com.person98.quests.data.stage;

import com.google.common.collect.Lists;
import com.person98.quests.data.stage.impl.StageData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Stage implements Cloneable {
   private List<StageData> data = Lists.newArrayList();
   private String bossTitle;
   private List<String> startMessages = Lists.newArrayList();
   private List<String> finishMessages = Lists.newArrayList();
   private List<String> rewards = Lists.newArrayList();

   public Stage() {
   }

   public Stage(List<StageData> data) {
      data.forEach(value -> this.data.add(new StageData(value.getType(), value.getRoot(), 0, value.getRootType())));
   }

   private int getTotalAmount(List<StageData> data) {
      int total = 0;

      for (StageData stageData : data) {
         total += stageData.getAmount();
      }

      return total;
   }

   public double getProgress(Stage stage) {
      int total = this.getTotalAmount(this.data);
      if (total <= 0 || stage == null) {
         return 0.0;
      }

      int amount = 0;
      Set<Integer> matchedProgressIndexes = new HashSet<>();
      for (StageData required : this.data) {
         for (int i = 0; i < stage.data.size(); i++) {
            StageData progress = stage.data.get(i);
            if (!matchedProgressIndexes.contains(i) && required.identifiesSameObjective(progress)) {
               matchedProgressIndexes.add(i);
               amount += Math.min(progress.getAmount(), required.getAmount());
               break;
            }
         }
      }
      return Math.min(1.0 * amount / total, 1.0);
   }

   public void incrementObjective(Player player, Stage stage, String type, Object root, int amount) {
      StageData required = stage.data.stream()
         .filter(data -> data.getType().equalsIgnoreCase(type) && data.compareRoot(player, root))
         .findFirst()
         .orElse(null);
      if (required == null) {
         return;
      }

      StageData progress = this.data.stream().filter(required::identifiesSameObjective).findFirst().orElse(null);
      if (progress == null) {
         // A quest definition may gain an objective after progress was persisted. Add the
         // missing counter lazily so that it can progress instead of remaining impossible.
         progress = new StageData(required.getType(), required.getRoot(), 0, required.getRootType());
         this.data.add(progress);
      }

      progress.setAmount(Math.min(progress.getAmount() + amount, required.getAmount()));
   }

   public int getAmount(Player player, String type, Object root) {
      for (StageData v : this.data) {
         if (v.getType().equalsIgnoreCase(type) && v.compareRoot(player, root)) {
            return v.getAmount();
         }
      }

      return 0;
   }

   public void setFinishMessages(List<String> finishMessages) {
      this.finishMessages = finishMessages.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
   }

   public void setStartMessages(List<String> startMessages) {
      this.startMessages = startMessages.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
   }

   public boolean isFinished(Stage stage) {
      if (stage == null) {
         return false;
      }

      // Compatibility fix for persisted/multi-objective stages: compare by objective
      // identity rather than YAML/JSON list position. A missing objective must not make
      // an incomplete stage appear complete.
      Set<Integer> matchedProgressIndexes = new HashSet<>();
      for (StageData required : this.data) {
         StageData progress = null;
         for (int i = 0; i < stage.data.size(); i++) {
            if (!matchedProgressIndexes.contains(i) && required.identifiesSameObjective(stage.data.get(i))) {
               matchedProgressIndexes.add(i);
               progress = stage.data.get(i);
               break;
            }
         }
         if (progress == null || progress.getAmount() < required.getAmount()) {
            return false;
         }
      }

      return true;
   }

   public Stage clone() {
      return new Stage(this.data);
   }

   public Stage snapshot() {
      Stage copy = new Stage();
      copy.data = this.data.stream().map(StageData::copy).collect(Collectors.toCollection(ArrayList::new));
      copy.bossTitle = this.bossTitle;
      copy.startMessages = new ArrayList<>(this.startMessages);
      copy.finishMessages = new ArrayList<>(this.finishMessages);
      copy.rewards = new ArrayList<>(this.rewards);
      return copy;
   }

   public List<StageData> getData() {
      return this.data;
   }

   public String getBossTitle() {
      return this.bossTitle;
   }

   public List<String> getStartMessages() {
      return this.startMessages;
   }

   public List<String> getFinishMessages() {
      return this.finishMessages;
   }

   public List<String> getRewards() {
      return this.rewards;
   }

   public void setData(List<StageData> data) {
      this.data = data;
   }

   public void setBossTitle(String bossTitle) {
      this.bossTitle = bossTitle;
   }

   public void setRewards(List<String> rewards) {
      this.rewards = rewards;
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Stage other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else {
         Object this$data = this.getData();
         Object other$data = other.getData();
         if (this$data == null ? other$data == null : this$data.equals(other$data)) {
            Object this$bossTitle = this.getBossTitle();
            Object other$bossTitle = other.getBossTitle();
            if (this$bossTitle == null ? other$bossTitle == null : this$bossTitle.equals(other$bossTitle)) {
               Object this$startMessages = this.getStartMessages();
               Object other$startMessages = other.getStartMessages();
               if (this$startMessages == null ? other$startMessages == null : this$startMessages.equals(other$startMessages)) {
                  Object this$finishMessages = this.getFinishMessages();
                  Object other$finishMessages = other.getFinishMessages();
                  if (this$finishMessages == null ? other$finishMessages == null : this$finishMessages.equals(other$finishMessages)) {
                     Object this$rewards = this.getRewards();
                     Object other$rewards = other.getRewards();
                     return this$rewards == null ? other$rewards == null : this$rewards.equals(other$rewards);
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   protected boolean canEqual(Object other) {
      return other instanceof Stage;
   }

   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $data = this.getData();
      result = result * 59 + ($data == null ? 43 : $data.hashCode());
      Object $bossTitle = this.getBossTitle();
      result = result * 59 + ($bossTitle == null ? 43 : $bossTitle.hashCode());
      Object $startMessages = this.getStartMessages();
      result = result * 59 + ($startMessages == null ? 43 : $startMessages.hashCode());
      Object $finishMessages = this.getFinishMessages();
      result = result * 59 + ($finishMessages == null ? 43 : $finishMessages.hashCode());
      Object $rewards = this.getRewards();
      return result * 59 + ($rewards == null ? 43 : $rewards.hashCode());
   }

   @Override
   public String toString() {
      return "Stage(data="
         + this.getData()
         + ", bossTitle="
         + this.getBossTitle()
         + ", startMessages="
         + this.getStartMessages()
         + ", finishMessages="
         + this.getFinishMessages()
         + ", rewards="
         + this.getRewards()
         + ")";
   }
}
