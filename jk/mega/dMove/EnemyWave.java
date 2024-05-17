package jk.mega.dMove;
import java.awt.geom.*;
import java.util.*;
import robocode.*;

import jk.precise.util.*;
import jk.mega.*;
import jk.math.FastTrig;

public class EnemyWave {
   Point2D.Double fireLocation;
   long fireTime;
   double bulletVelocity, directAngle, distanceTraveled;
   int direction;
   ArrayList allStats;
   ArrayList flattenerStats;
   ArrayList ABSStats;
   ArrayList flattenerTickStats;
   boolean flattenerLogged = false;
   float[] bestBins;
   float[] binCleared;
   ArrayList safePoints;
   PlaceTime safestPoint;
   
   int[][] indexes;
   
   boolean bulletGone = false;
   boolean imaginary = false;
   boolean futureWave = false;
   	
   float weight = 0;
   
   Scan scan;
   
   	//used for second-wave surfing
   ArrayList possPoints;
   PlaceTime possSafePT;

   public EnemyWave() { }
   
   static final int BINS = 171;
   static final int MIDDLE_BIN = (BINS - 1)/2;
   
   public boolean logShadow(double time, BulletTracker bt){
      Bullet b = bt.b;
      double t = 0;
      Point2D.Double currentbP = new Point2D.Double(b.getX(), b.getY());
      boolean change = false;
      if(binCleared == null){
         binCleared = new float[BINS];
         for(int k = 0; k < BINS; k++)
            binCleared[k] = 1;
      }
      while(true){
         t++;     
      
         Point2D.Double bP = project(currentbP,b.getHeadingRadians(),b.getVelocity()*(t+1));
         Point2D.Double lastbP = project(currentbP,b.getHeadingRadians(),b.getVelocity()*t);
      
         double en = bP.distance(fireLocation);
         double len = lastbP.distance(fireLocation);
            
         double actDistTrav = bulletVelocity*(time - fireTime + t);
         double lastDistTraveled = bulletVelocity*(time - fireTime + t-1);
         if( len < lastDistTraveled | len < en )
            return change;
            
         if( en < actDistTrav & len > lastDistTraveled & en < len ){
               //we have intersection!
               // 4 cases:
               //1: closer to me
               //2: closer to enemy
               //3: overlaps entire wave
               //4: contained within wave
         		
            Point2D.Double p1, p2;
               
            if(en >= lastDistTraveled)// 1 & 4
               p1 = bP;
            else{// 2 & 3
               PreciseWave wv = new PreciseWave();
               wv.fireLocation = fireLocation;
               wv.distanceTraveled = lastDistTraveled;
               p1 = PreciseUtils.intersection(lastbP,bP,wv);      
            }
            if(len > actDistTrav){//1 & 3
               PreciseWave wv = new PreciseWave();
               wv.fireLocation = fireLocation;
               wv.distanceTraveled = actDistTrav;
               p2 = PreciseUtils.intersection(lastbP,bP,wv);
            }
            else // 2 & 4
               p2 = lastbP;
            
            double i1 = getFactorIndex(p1);
            double i2 = getFactorIndex(p2);
            double lowIndex = limit(0,Math.min(i1,i2),BINS);
            double highIndex = limit(-1,Math.max(i1,i2),BINS-1);
            
            if(lowIndex == BINS | highIndex == -1)
               continue;
               
         
            int k = (int)Math.ceil(lowIndex), m = (int)Math.floor(highIndex);
            for(; k <= m; k++)
               binCleared[k] = 0;
            if(k > 0)
               binCleared[k-1] = (float)Math.max(0,binCleared[k-1] - k + lowIndex);
            if(m < BINS-1)
               binCleared[m+1] = (float)Math.max(0,binCleared[m+1] - highIndex + m);
          
            int index = bt.crossedWaves.indexOf(this);
            if(index == -1)
               bt.crossedWaves.add(this);
         
            if(bestBins != null)
               change = true;
         }
      }
      
      //return change;
   }
   public boolean clearShadow(double time, BulletTracker bt){
   
      Bullet b = bt.b;
      double t = 0;
      Point2D.Double currentbP = new Point2D.Double(b.getX(), b.getY());
      boolean change = false;
      if(binCleared == null){
         binCleared = new float[BINS];
         for(int k = 0; k < BINS; k++)
            binCleared[k] = 1;
      }
      
      while(true){
         t++;     
      
         Point2D.Double bP = project(currentbP,b.getHeadingRadians(),b.getVelocity()*(t+1));
         Point2D.Double lastbP = project(currentbP,b.getHeadingRadians(),b.getVelocity()*t);
      
         double en = bP.distance(fireLocation);
         double len = lastbP.distance(fireLocation);
            
         double actDistTrav = bulletVelocity*(time - fireTime + t);
         double lastDistTraveled = bulletVelocity*(time - fireTime + t-1);
         if( len < lastDistTraveled | len < en )
            return change;
            
         if( en < actDistTrav & len > lastDistTraveled & en < len ){
               //we have intersection!
               // 4 cases:
               //1: closer to me
               //2: closer to enemy
               //3: overlaps entire wave
               //4: contained within wave
            Point2D.Double p1, p2;
               
            if(en >= lastDistTraveled)
               p1 = bP;
            else{
               PreciseWave wv = new PreciseWave();
               wv.fireLocation = fireLocation;
               wv.distanceTraveled = lastDistTraveled;
               p1 = PreciseUtils.intersection(lastbP,bP,wv);      
            }
            if(len > actDistTrav){
               PreciseWave wv = new PreciseWave();
               wv.fireLocation = fireLocation;
               wv.distanceTraveled = actDistTrav;
               p2 = PreciseUtils.intersection(lastbP,bP,wv);
            }
            else
               p2 = lastbP;
            
            double i1 = getFactorIndex(p1);
            double i2 = getFactorIndex(p2);
            double lowIndex = limit(0,Math.min(i1,i2),BINS);
            double highIndex = limit(-1,Math.max(i1,i2),BINS-1);
            
            if(lowIndex == BINS || highIndex == -1)
               continue;
               
         
            int k = (int)Math.ceil(lowIndex), m = (int)Math.floor(highIndex);
            for(; k <= m; k++)
               binCleared[k] = 1;
            if(k > 0)
               binCleared[k-1] = (float)Math.min(1,binCleared[k-1] + k - lowIndex);
            if(m < BINS-1)
               binCleared[m+1] = (float)Math.min(1,binCleared[m+1] + highIndex - m);
            if(bt != null){
               int index = bt.crossedWaves.indexOf(this);
               if(index != -1){
                  bt.crossedWaves.remove(index);	
               }
            }
            
            if(bestBins != null)
               change = true;
         }
      }
      //return change;
   }
   
   public  double getFactorIndex(Point2D.Double targetLocation) {
      double absAngle = absoluteBearing(fireLocation, targetLocation);
      return getFactorIndex(absAngle);
   }
   public  double getFactorIndex(double absoluteBearing) {
      double offsetAngle = (absoluteBearing - directAngle);
      double factor = FastTrig.normalRelativeAngle(offsetAngle)
         / maxEscapeAngle() * direction;
   
      return limit(0,(0.9*factor * ((BINS - 1) / 2)) + ((BINS - 1) / 2),BINS-1);
   }
   
   public  double maxEscapeAngle() {
      return FastTrig.asin(8.0/bulletVelocity);
   }
   
   
   private static double absoluteBearing(Point2D source, Point2D target) {
      return FastTrig.atan2(target.getX() - source.getX(), target.getY() - source.getY());
   }
   private static Point2D.Double project(Point2D.Double sourceLocation, double angle, double length) {
      return new Point2D.Double(sourceLocation.x + FastTrig.sin(angle) * length,
         sourceLocation.y + FastTrig.cos(angle) * length);
   }
   public static double limit(double min, double value, double max) {
      if(value > max)
         return max;
      if(value < min)
         return min;
    
      return value;
   }
}