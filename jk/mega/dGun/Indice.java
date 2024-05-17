   package jk.mega.dGun;
    class Indice implements Comparable{
      double position,  height;
       public int compareTo(Object o){
         // if(((Indice)o).position > position)
            // return -1;
         // return 1;
         return (int)Math.signum(position - ((Indice)o).position);
      }
   }