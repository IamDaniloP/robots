   package jk.mega.dGun;
   public class GFRange implements Comparable{
      double max = -1,min = 1,center = 0,width = 0;
      public int compareTo(Object g){
         if(center < ((GFRange)g).center)
            return -1;
         return 1;
      }
   }