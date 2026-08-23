package com.person98.quests.data;

import com.google.common.collect.Lists;
import com.person98.quests.Quests;
import com.person98.quests.data.stage.Stage;
import java.util.LinkedList;
import java.util.List;

public class Quest {
   private String identifier;
   private String name;
   private int points;
   private boolean story;
   private boolean first;
   private List<String> description = Lists.newArrayList();
   private String next;
   private long delay;
   private LinkedList<Stage> stages = Lists.newLinkedList();

   public Quest(String name, int points, boolean story) {
      this.name = name;
      this.points = points;
      this.story = story;
   }

   public Quest getNext() {
      return this.story && this.next != null ? Quests.getInstance().getQuestController().find(this.next) : null;
   }

   public String getIdentifier() {
      return this.identifier;
   }

   public String getName() {
      return this.name;
   }

   public int getPoints() {
      return this.points;
   }

   public boolean isStory() {
      return this.story;
   }

   public boolean isFirst() {
      return this.first;
   }

   public List<String> getDescription() {
      return this.description;
   }

   public long getDelay() {
      return this.delay;
   }

   public LinkedList<Stage> getStages() {
      return this.stages;
   }

   public void setIdentifier(String identifier) {
      this.identifier = identifier;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setPoints(int points) {
      this.points = points;
   }

   public void setStory(boolean story) {
      this.story = story;
   }

   public void setFirst(boolean first) {
      this.first = first;
   }

   public void setDescription(List<String> description) {
      this.description = description;
   }

   public void setNext(String next) {
      this.next = next;
   }

   public void setDelay(long delay) {
      this.delay = delay;
   }

   public void setStages(LinkedList<Stage> stages) {
      this.stages = stages;
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Quest other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else if (this.getPoints() != other.getPoints()) {
         return false;
      } else if (this.isStory() != other.isStory()) {
         return false;
      } else if (this.isFirst() != other.isFirst()) {
         return false;
      } else if (this.getDelay() != other.getDelay()) {
         return false;
      } else {
         Object this$identifier = this.getIdentifier();
         Object other$identifier = other.getIdentifier();
         if (this$identifier == null ? other$identifier == null : this$identifier.equals(other$identifier)) {
            Object this$name = this.getName();
            Object other$name = other.getName();
            if (this$name == null ? other$name == null : this$name.equals(other$name)) {
               Object this$description = this.getDescription();
               Object other$description = other.getDescription();
               if (this$description == null ? other$description == null : this$description.equals(other$description)) {
                  Object this$next = this.getNext();
                  Object other$next = other.getNext();
                  if (this$next == null ? other$next == null : this$next.equals(other$next)) {
                     Object this$stages = this.getStages();
                     Object other$stages = other.getStages();
                     return this$stages == null ? other$stages == null : this$stages.equals(other$stages);
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
      return other instanceof Quest;
   }

   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + this.getPoints();
      result = result * 59 + (this.isStory() ? 79 : 97);
      result = result * 59 + (this.isFirst() ? 79 : 97);
      long $delay = this.getDelay();
      result = result * 59 + (int)($delay >>> 32 ^ $delay);
      Object $identifier = this.getIdentifier();
      result = result * 59 + ($identifier == null ? 43 : $identifier.hashCode());
      Object $name = this.getName();
      result = result * 59 + ($name == null ? 43 : $name.hashCode());
      Object $description = this.getDescription();
      result = result * 59 + ($description == null ? 43 : $description.hashCode());
      Object $next = this.getNext();
      result = result * 59 + ($next == null ? 43 : $next.hashCode());
      Object $stages = this.getStages();
      return result * 59 + ($stages == null ? 43 : $stages.hashCode());
   }

   @Override
   public String toString() {
      return "Quest(identifier="
         + this.getIdentifier()
         + ", name="
         + this.getName()
         + ", points="
         + this.getPoints()
         + ", story="
         + this.isStory()
         + ", first="
         + this.isFirst()
         + ", description="
         + this.getDescription()
         + ", next="
         + this.getNext()
         + ", delay="
         + this.getDelay()
         + ", stages="
         + this.getStages()
         + ")";
   }
}
