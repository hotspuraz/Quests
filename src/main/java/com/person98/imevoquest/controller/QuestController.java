package com.person98.imevoquest.controller;

import com.person98.imevoquest.ImevoQuest;
import com.person98.imevoquest.data.Quest;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface QuestController {
   void constructor(ImevoQuest quests);

   Quest find(String name);

   Quest firstStory();

   List<Quest> getDailyQuests();

   void forEach(Consumer<Quest> consumer);

   Stream<Quest> stream();

   void selectNewDailyQuests(int count);

   void savePersistedDailyQuests(String date);

   void reloadDailyQuestConfig();
}
