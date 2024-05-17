package jk.precise;
import robocode.*;
import robocode.util.*;
import java.awt.geom.*;
import jk.precise.util.*;
import java.util.*;

import jk.math.FastTrig;

public class EnergyDomeWorker
{
   final static int OPTIONS = 12;
   static int[] optionScores = new int[OPTIONS];
   static int[] offsetCounts = new int[OPTIONS];
   static double[] offsets = new double[OPTIONS];
   static double[] dirOffsets = new double[OPTIONS];

   double lastEnemyEnergy = 100;
   Point2D.Double lastEnemyLocation;
   ArrayList<Point2D.Double> myLocations = new ArrayList<Point2D.Double>();
   ArrayList<Point2D.Double> enemyLocations = new ArrayList<Point2D.Double>();
   ArrayList<Double> enemyHeadings = new ArrayList<Double>();
   ArrayList<Double> enemyVelocities = new ArrayList<Double>();
   double lastBearing;
   double firePower;
   double moveAmount;
   long nextFireTime;
   ArrayList<DetectWave> enemyWaves = new ArrayList<DetectWave>();
   double direction=1;
   static double unmatchedEnemyDamage;
   static double enemyDamage;
   static double myDamage;
 
 
   boolean move;
   boolean aim;
   boolean turn;
   boolean fire;
   double moveVal;
   double aimAngle;
   double turnAngle;
   
   boolean idealPosition;
   Point2D.Double goalPoint;
   static final double WALL_THRESHOLD = 200;
   Rectangle2D.Double safeField;
   
   AdvancedRobot bot;
   Point2D.Double myLocation;
   
   int bullets;
   double avgBulletPower;
   //double enemyEnergy = 100;
   
   public EnergyDomeWorker(AdvancedRobot bot){
   
      this.bot = bot;
   
      nextFireTime = bot.getTime() + (long)Math.ceil(bot.getGunHeat()/bot.getGunCoolingRate());
      //setAdjustRadarForGunTurn(true);
      //setAdjustRadarForRobotTurn(true); -- redundant
      //setAdjustGunForRobotTurn(true);
      if(bot.getRoundNum() > 0)
         System.out.println("Enemy Gun VG scores: \n" + Arrays.toString(optionScores));
      // while(true){
         // turnRadarRight(Double.POSITIVE_INFINITY);
      // }
      myLocation = new Point2D.Double(bot.getX(), bot.getY());
   
      safeField = new Rectangle2D.Double(WALL_THRESHOLD,WALL_THRESHOLD,
         bot.getBattleFieldWidth() - 2*WALL_THRESHOLD,bot.getBattleFieldHeight() - 2*WALL_THRESHOLD);
         
   //       int code = safeField.outcode(myLocation);
      goalPoint = (Point2D.Double)myLocation.clone();
      if(safeField.contains(myLocation) || bot.getRoundNum() == 0)
         idealPosition = true;
      else{
         goalPoint.x = limit(WALL_THRESHOLD,goalPoint.x,bot.getBattleFieldWidth()-WALL_THRESHOLD);
         goalPoint.y = limit(WALL_THRESHOLD,goalPoint.y,bot.getBattleFieldHeight()-WALL_THRESHOLD);
         goTo(goalPoint);
      }
     
   }

   public void onScannedRobot(ScannedRobotEvent e) {
   
      double latVel = bot.getVelocity()*Math.sin(e.getBearingRadians());
      if(latVel < 0)
         direction = -1;
      if(latVel > 0)
         direction = 1;
      
      myLocation = new Point2D.Double(bot.getX(), bot.getY());
      double absBearing=e.getBearingRadians() + (bot.getHeadingRadians());
      double eDistance = e.getDistance();
      double deltaE = (lastEnemyEnergy - (lastEnemyEnergy = e.getEnergy()));
      Point2D.Double enemyLocation = project(myLocation, absBearing, eDistance);
      myLocations.add(0,myLocation);
      enemyLocations.add(0,enemyLocation);
      enemyHeadings.add(0,e.getHeadingRadians());
      enemyVelocities.add(0,e.getVelocity());
      
      if(!idealPosition){
         double safeStop = Math.abs(bot.getVelocity())/2 + 3;
      
         if(bot.getRoundNum() == 0)
            safeStop += 300;//build some history
      
         if( (nextFireTime - bot.getTime()) > safeStop){
            goalPoint = (Point2D.Double)myLocation.clone();
            goalPoint.x = limit(WALL_THRESHOLD,goalPoint.x,bot.getBattleFieldWidth()-WALL_THRESHOLD);
            goalPoint.y = limit(WALL_THRESHOLD,goalPoint.y,bot.getBattleFieldHeight()-WALL_THRESHOLD);
            goTo(goalPoint);
         } 
         else{
            //goTo(myLocation);
            bot.setAhead(0);
            //idealPosition = true;
         }
      }
        
      if(bot.getTime() >= nextFireTime && lastEnemyLocation != null && (Math.min(lastEnemyEnergy,0.0999)  < deltaE && deltaE < 3.001)){
         idealPosition = true;
         bullets++;
         if(bullets == 1)
            avgBulletPower = deltaE;
         else
            avgBulletPower = 0.1*deltaE + 0.9*avgBulletPower;
         //System.out.println("Bullet seen!");
         nextFireTime = bot.getTime() + (long)Math.ceil(Rules.getGunHeat(deltaE)/bot.getGunCoolingRate());
         double enemyBulletVelocity = Rules.getBulletSpeed(deltaE);
         
         double[] options = new double[OPTIONS];
         options[0] = absoluteBearing(enemyLocations.get(2),myLocations.get(2));
         options[1] = absoluteBearing(enemyLocations.get(2),myLocations.get(2))
            + enemyHeadings.get(1) - enemyHeadings.get(2);
         options[2] = absoluteBearing(enemyLocations.get(1),myLocations.get(2));
         Point2D.Double predictedNext = project(enemyLocations.get(2),enemyHeadings.get(2),enemyVelocities.get(2));
         options[3] = absoluteBearing(predictedNext,myLocations.get(2));
         //System.out.println("Predicted difference: " + predictedNext.distance(enemyLocations.get(1)));
         for(int i = 0; i < OPTIONS/3; i++){
            options[i + OPTIONS/3] = options[i] + offsets[i];
            options[i + 2*OPTIONS/3] = options[i] + direction*dirOffsets[i];
         }
         
         int maxIndex = 0;
         for(int i = 0; i < OPTIONS; i++)
            if(optionScores[i] > optionScores[maxIndex])
               maxIndex = i;
         // System.out.println("Max index: " + maxIndex);
         double bulletBearing = options[maxIndex];
         double[] moveOptions = {-0.4,0.0,0.4};
         double[] moveWeights;
         if(bullets%2 == 0)
            moveWeights = new double[]{0.5,0.2,1};
         else
            moveWeights = new double[]{1,0.2,0.5};
         if(Math.abs(enemyVelocities.get(1)*Math.sin(enemyHeadings.get(1) - bulletBearing)) > 0.1 && maxIndex%(OPTIONS/3) < 2)
            moveOptions = new double[]{0d};
         double maxDiff = 0;
         for(int j = 0; j < moveOptions.length; j++){
            Point2D.Double fireLoc = project(myLocation,bot.getHeadingRadians(),moveOptions[j]);
            double fireBearing = absoluteBearing(fireLoc,lastEnemyLocation);
            double diff = Math.abs(Utils.normalRelativeAngle(fireBearing + Math.PI - bulletBearing));
            if(diff*moveWeights[j] > maxDiff){
               moveAmount = moveOptions[j];
               maxDiff = diff*moveWeights[j];  
            }
         }
         //options[4] = absoluteBearing(enemyLocations.get(1),project(myLocations.get(2),getHeadingRadians(),moveAmount*0.5));
      
         Point2D.Double fireLoc = project(myLocation,bot.getHeadingRadians(),moveAmount);
        
         double maxWidth = 0;
         double bestAngle = absoluteBearing(fireLoc,enemyLocations.get(1));
         double bestPower = 0.1;
         long hitTime = 0;
         double maxPower = Math.min(deltaE-0.01,deltaE*(bot.getEnergy()-10)/e.getEnergy());
      //    enemyEnergy = e.getEnergy();
         
         for(int i = 7; i < 20; i++){
            double bulletPower = 0.1 + Math.max(0,(maxPower-0.1)*0.05*i);
            // if(deltaE > 0.1)
               // bulletPower += 0.01;
            double myBulletVelocity = Rules.getBulletSpeed(bulletPower);
            Point2D.Double bLoc = lastEnemyLocation;
            double mDist = 0;
            double eFireTime = -1;
            double mFireTime = 1;
            double t = 0;
            while(true){
               t+= 1;
               bLoc = project(lastEnemyLocation,bulletBearing,(t-eFireTime)*enemyBulletVelocity);
               double bDistSq = bLoc.distanceSq(fireLoc);
               mDist = (t - mFireTime)*myBulletVelocity;
               if(mDist*mDist >= bDistSq || t > 100)
                  break;
            }
            
            Point2D.Double bP = bLoc;
            Point2D.Double lastbP = project(bP,bulletBearing,-enemyBulletVelocity);
            
            double en = bP.distance(fireLoc);
            double len = lastbP.distance(fireLoc);
            
            double actDistTrav = mDist;
            double lastDistTraveled = actDistTrav - myBulletVelocity;
         
            if(!( en < actDistTrav && len > lastDistTraveled && en < len )){
               System.out.println("Prediction error!");
               continue;
            }
            Point2D.Double p1, p2;
            if(en >= lastDistTraveled)
               p1 = bP;
            else{
               PreciseWave wv = new PreciseWave();
               wv.fireLocation = fireLoc;
               wv.distanceTraveled = lastDistTraveled;
               p1 = PreciseUtils.intersection(lastbP,bP,wv);      
            }
            if(len > actDistTrav){
               PreciseWave wv = new PreciseWave();
               wv.fireLocation = fireLoc;
               wv.distanceTraveled = actDistTrav;
               p2 = PreciseUtils.intersection(lastbP,bP,wv);
            }
            else
               p2 = lastbP;
         
            
            double d1 = absoluteBearing(fireLoc,p1);
            double d2 = absoluteBearing(fireLoc,p2);
              
            //double pDist = p1.distance(p2);
         
            Point2D.Double midPoint = new Point2D.Double(0.5*(p1.x+p2.x) , 0.5*(p1.y+p2.y));
         
            Rectangle2D.Double me = new Rectangle2D.Double(myLocation.x - 18.1, myLocation.y - 18.1, 36.2, 36.2);
            
            double diff = Utils.normalRelativeAngle(d1-d2);
            double fireAngle = d1 - 0.5*diff;
            double width = Math.abs(diff);
            if(width > maxWidth && !me.contains(p2)){
               maxWidth = width;
               bestAngle = fireAngle;
               bestPower = bulletPower;
               hitTime = (long)t;
            }
         }
            
         aim = true;
         aimAngle = bestAngle;
         
         firePower = bestPower;
         move = true;
         moveVal = moveAmount;
         
         DetectWave dw = new DetectWave();
         dw.fireLocation = enemyLocations.get(1);
         dw.fireTime = bot.getTime() - 2;
         dw.bulletBearings = options;
         dw.bearingAttempts = new boolean[OPTIONS];
         dw.bearingAttempts[maxIndex] = true;
         dw.bulletVelocity = enemyBulletVelocity;
         dw.interceptTime = hitTime;
         dw.direction = direction;
         enemyWaves.add(dw);
      
      }
      
         
      if(!move && bot.getDistanceRemaining() == 0 && !aim && bot.getGunTurnRemaining() == 0 && !fire && firePower > 0){
      
         fire = true;
         move = true;
         moveVal = -moveAmount;
         
      }
      updateWaves();
      
      if(firePower == 0 && !aim){
         aim = true;
         aimAngle = absBearing;
      
         if(e.getEnergy() < 0.1 && e.getVelocity() == 0 &&enemyVelocities.size() > 1 && bot.getTime() > nextFireTime + 3 &&
          enemyVelocities.get(1) == 0 && enemyWaves.size() == 0 && bot.getEnergy() > 1 + e.getEnergy()
          ){
            fire = true;
            firePower = 0.1;
         }
         else
            if( bot.getDistanceRemaining() == 0 && !move && !turn){
               turn = true;
               turnAngle = absBearing + Math.PI/2;
               
            }
      }
      
      if(lastEnemyLocation != null)
         lastBearing = absoluteBearing(lastEnemyLocation,myLocation);
      lastEnemyLocation = enemyLocation;
      
      if(nextFireTime < bot.getTime() - 100 && lastEnemyEnergy >= bot.getEnergy()-1){
         enemyDamage ++;
         unmatchedEnemyDamage ++;
      }
   }
   
   public void goTo(Point2D.Double destination){
   
      double distance = myLocation.distance(destination);
      double angle;
      if(-1 < distance && distance < 1 ){
         angle = 0;
         distance = 0;
      }
      else
         angle = FastTrig.normalRelativeAngle(absoluteBearing(myLocation, destination) - bot.getHeadingRadians());
         
      if (Math.abs(angle) > Math.PI/2) {
         distance = -distance;
         if (angle > 0) {
            angle -= Math.PI;
         }
         else {
            angle += Math.PI;
         }
      }
      
   	
      bot.setTurnRightRadians(angle);
      bot.setAhead(distance);
   }
   public void applyActions(){
      if(fire){
         bot.setFire(firePower);
         firePower = 0;
         fire = false;
      }
      
      if(aim){
         bot.setTurnGunRightRadians(Utils.normalRelativeAngle(aimAngle - bot.getGunHeadingRadians()));
         aim = false;
      }
      
      if(move){
         bot.setAhead(moveVal);
         move = false;
      }
   
      if(turn){
         bot.setTurnRightRadians(Math.tan(turnAngle - bot.getHeadingRadians()));
         turn = false;
      }
   
   }
   
   void updateWaves(){
      Iterator<DetectWave> it = enemyWaves.iterator();
      long time = bot.getTime();
      while(it.hasNext()){
         DetectWave dw = it.next();
         dw.distanceTraveled = dw.bulletVelocity*(time - dw.fireTime);
         if(dw.distanceTraveled - 18 > dw.fireLocation.distance(myLocations.get(0)))
            it.remove();
      }
   }
   boolean logBullet(Bullet b){
      double heading = b.getHeadingRadians();
      Iterator<DetectWave> it = enemyWaves.iterator();
      long time = bot.getTime();
      while(it.hasNext()){
         DetectWave dw = it.next();
         dw.distanceTraveled = dw.bulletVelocity*(time - dw.fireTime);
         Point2D.Double bloc = new Point2D.Double(b.getX(),b.getY());
         if(Math.abs(dw.distanceTraveled -  dw.fireLocation.distance(bloc) - dw.bulletVelocity) < 1.1*dw.bulletVelocity){
            boolean matched = false;
            
            for(int i = 0; i < OPTIONS; i++){
               double diff = (Utils.normalRelativeAngle(heading - dw.bulletBearings[i]));
               if(Math.abs(diff) < 0.00001){
                  optionScores[i]++;
                  matched = true;
               }
               if( i < OPTIONS/3){
                  offsets[i] = (offsets[i]*offsetCounts[i] + diff)/(offsetCounts[i]+1);
                  dirOffsets[i] = (dirOffsets[i]*offsetCounts[i] + dw.direction*diff)/(offsetCounts[i]+1);
                  offsetCounts[i]++;
               }
            }
            it.remove();
            return matched;
         }     
      }
   
      System.out.println("No bullet detected");
      return false;
   }
   public void onHitByBullet(HitByBulletEvent e){
      
      lastEnemyEnergy += 20 - (e.getVelocity());	
      boolean matched = logBullet(e.getBullet());
      double damage = Rules.getBulletDamage(e.getBullet().getPower());
      enemyDamage += damage;
      if(!matched ){
         unmatchedEnemyDamage += damage;
      }
   }
   
   public boolean aboveScore(double goal){
      double Q = goal*0.01;
      double x = (2100 + myDamage)*(1-Q)/Q;
   
      int maxIndex = 0;
      for(int i = 0; i < OPTIONS; i++)
         if(optionScores[i] > optionScores[maxIndex])
            maxIndex = i;
            
      if((bot.getRoundNum() > 0 && optionScores[maxIndex] < bullets*0.6)
      ||
      (bullets > 10 && lastEnemyEnergy/avgBulletPower > bot.getEnergy()/0.11)
      ||
      (enemyLocations.size() > 0 && nextFireTime < bot.getTime() + 5 && enemyLocations.get(0).distance(myLocations.get(0)) < 80)
      )
         x -= 100;
            
      return (enemyDamage <= x);
   }
   public void onBulletHitBullet(BulletHitBulletEvent e){
      logBullet(e.getHitBullet());
   }
   public void onBulletHit(BulletHitEvent e){
      lastEnemyEnergy -= Rules.getBulletDamage(e.getBullet().getPower());   
      myDamage += Rules.getBulletDamage(e.getBullet().getPower());
   }
   static Point2D.Double project(Point2D.Double location, double angle, double distance){
      return new Point2D.Double(location.x + distance*Math.sin(angle), location.y + distance*Math.cos(angle));
   }
   private double absoluteBearing(Point2D source, Point2D target) {
      return Math.atan2(target.getX() - source.getX(), target.getY() - source.getY());
   }
   public static double limit(double min, double value, double max) {
      if(value > max)
         return max;
      if(value < min)
         return min;
    
      return value;
   }
   
   class DetectWave extends PreciseWave{
   
      long fireTime;
      double direction;
      
      double[] bulletBearings;
      boolean[] bearingAttempts;
   
      long interceptTime;
   
   }
}