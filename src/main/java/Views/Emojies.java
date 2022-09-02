package Views;

public enum Emojies {
   SPIDER ("\uD83D\uDD77"),
   WEB ("\uD83D\uDD78"),
   SETTINGS ("⚙"),
   HELP ("\uD83D\uDC68\u200D\uD83D\uDCBB"),
   BOOK ("\uD83D\uDCD6"),
   SCROLL ("\uD83D\uDCDC"),
   BANK_CARD ("\uD83D\uDCB3"),
   FLY ("\uD83E\uDEB0"),
   WATER ("\uD83D\uDCA6"),
   LIGHTNING ("⚡"),
   UP_ROW ("⬆");


   private String emoji;

   Emojies(String emoji){
      this.emoji = emoji;
   }

   @Override
   public String toString(){
      return emoji;
   }
}