package com.person98.quests.util;

public class PageInfo {
   private int totalEntries = 0;
   private int totalPages = 0;
   private int start = 0;
   private int end = 0;
   private int currentPage = 0;
   private int currentEntry = 0;
   private int perPage = 6;

   public PageInfo(int perPage, int totalEntries, int currentPage) {
      this.perPage = perPage;
      this.totalEntries = totalEntries;
      this.currentPage = Math.max(currentPage, 1);
      this.calculate();
   }

   public int getPositionForOutput() {
      return this.currentEntry;
   }

   public int getPositionForOutput(int index) {
      return this.start + index + 1;
   }

   private void calculate() {
      this.currentEntry = 0;
      this.start = (this.currentPage - 1) * this.perPage;
      this.end = this.start + this.perPage - 1;
      if (this.end + 1 > this.totalEntries) {
         this.end = this.totalEntries - 1;
      }

      this.totalPages = (int)Math.ceil((double)this.totalEntries / this.perPage);
   }

   public boolean isInRange(int index) {
      return index >= this.start && index <= this.end;
   }

   public boolean isEntryOk() {
      this.currentEntry++;
      return this.currentEntry - 1 >= this.start && this.currentEntry - 1 <= this.end;
   }

   public boolean isContinue() {
      return !this.isEntryOk();
   }

   public boolean isContinueNoAdd() {
      return this.currentEntry - 1 >= this.start && this.currentEntry - 1 <= this.end;
   }

   public boolean isBreak() {
      return this.currentEntry - 1 > this.end;
   }

   public boolean isPageOk() {
      return this.isPageOk(this.currentPage);
   }

   public boolean isPageOk(int page) {
      return page >= 1 && page <= this.totalPages;
   }

   public int getStart() {
      return this.start;
   }

   public int getEnd() {
      return this.end;
   }

   public int getTotalPages() {
      return this.totalPages;
   }

   public int getCurrentPage() {
      return this.currentPage;
   }

   public int getTotalEntries() {
      return this.totalEntries;
   }

   public int getNextPageNumber() {
      return Math.min(this.getCurrentPage() + 1, this.getTotalPages());
   }

   public int getPrevPageNumber() {
      return Math.max(this.getCurrentPage() - 1, 1);
   }

   public Boolean pageChange(int page) {
      return true;
   }

   public PageInfo setCurrentPage(int currentPage) {
      this.currentPage = currentPage;
      this.calculate();
      return this;
   }

   @Override
   public String toString() {
      return "PageInfo{totalEntries="
         + this.totalEntries
         + ", totalPages="
         + this.totalPages
         + ", start="
         + this.start
         + ", end="
         + this.end
         + ", currentPage="
         + this.currentPage
         + ", currentEntry="
         + this.currentEntry
         + ", perPage="
         + this.perPage
         + "}";
   }
}
