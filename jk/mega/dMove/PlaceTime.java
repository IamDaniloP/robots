package jk.mega.dMove;
import java.awt.geom.*;
import java.util.*;


public class PlaceTime implements Comparable{
   Point2D.Double place;
   long time;
   
   PredictionStatus predictionStatus;
   
   	//speed optimizations - don't try this at home, kids!
   float danger;
   

   public int compareTo(Object o){
      return (int)Math.signum(danger - ((PlaceTime)o).danger);
   }
}