package com.person98.imevoquest.util;

public class TimeUtils {
   public static String timeLeft(int time) {
      StringBuilder sb = new StringBuilder();
      int aux = time;
      if (time >= 3600) {
         int hours = time / 3600;
         aux = time - hours * 3600;
         sb.append(hours == 1 ? hours + " hour " : hours + " hours ");
      }

      if (aux >= 60) {
         int minutes = aux / 60;
         aux -= minutes * 60;
         sb.append(minutes == 1 ? minutes + " minute " : minutes + " minutes ");
      }

      if (aux > 0) {
         sb.append(aux == 1 ? aux + " second" : aux + " seconds");
      }

      return sb.substring(0, sb.toString().length());
   }
}
