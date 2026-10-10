package com.person98.quests.data;

import com.google.common.collect.Lists;
import com.person98.quests.Quests;
import com.person98.quests.data.stage.Stage;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

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

   /** Quests are identified by their config key alone; hashing stages or the story chain would be costly and can recurse. */
   @Override
   public boolean equals(Object o) {
      return o == this || o instanceof Quest other && this.identifier != null && this.identifier.equals(other.identifier);
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.identifier);
   }

   @Override
   public String toString() {
      return "Quest(" + this.identifier + ")";
   }
}
