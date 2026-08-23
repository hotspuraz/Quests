package com.person98.imevoquest.service.quest;

import java.math.BigInteger;
import org.bukkit.entity.Player;

public interface QuestExecutionBuilder {
   static QuestExecutionBuilderImpl of(QuestContainer container, String type) {
      return new QuestExecutionBuilderImpl(container, type);
   }

   void buildAndExecute();

   QuestExecutionBuilder player(Player player);

   QuestExecutionBuilder root(Object root);

   QuestExecutionBuilder processSingle();

   default QuestExecutionBuilder progress(int progress) {
      return this.progress(BigInteger.valueOf(progress));
   }

   QuestExecutionBuilder progress(BigInteger progress);
}
