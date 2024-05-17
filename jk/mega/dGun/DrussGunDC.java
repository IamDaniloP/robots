package jk.mega.dGun;
import jk.precise.util.*;
//import ags.utils.*;
import robocode.*;
import java.awt.Color;
import java.awt.geom.*;
import java.util.*;
import jk.mega.*;
import jk.math.FastTrig;
import jk.tree.KDTree;


public class DrussGunDC {
   //final static boolean TC = false;
   final static boolean logData = false;
   static StringBuilder dataToLog = new StringBuilder();

   DrussGT bot;
   static Point2D.Double myLocation, myNextLocation;
   private static double BULLET_POWER = 1.9;
   ArrayList<Double> lateralVelocities = new ArrayList<Double>(1000);
   ArrayList<Point2D.Double> enemyLocations = new ArrayList<Point2D.Double>(1000);
   ArrayList<Point2D.Double> precisePredictionPoints;
   static ArrayList<DCWave> waveList, removeList;
   double lastDirection = 1, lastVelocity = 0, lastHeading = 0;
   int timeSinceDirChange = 0, timeSinceDecel = 0, timeSinceAccel = 0;
   public static int bulletsShot = 0;
   public static int bulletsPassed = 0;
   public static int bulletsHit = 0;
   boolean firstScan = true;
   public static double currentGF = 0;
   public double nextAbsBearing;
   StoreScan lastStoreScan;
   static String enemyName;
   DCWave wave;
   ScannedRobotEvent lastScan;

	
   public DrussGunDC(DrussGT bot){
      waveList = new ArrayList<DCWave>();
      removeList = new ArrayList<DCWave>();
      this.bot = bot;
      if(DCWave.heapTree != null){
         System.out.println("My hitrate     : " + (double)(Math.round(1000.0*bulletsHit/bulletsPassed)/10.0) + "%");
         System.out.println("Actual hits    : " + bulletsHit);
         System.out.println("DC gun score   : " + DCWave.DCHits);
         System.out.println("DCAS gun score : " + DCWave.DCASHits);
         System.out.println("Random score   : " + (int)Math.round(DCWave.randomHits));
         System.out.println("gun: " 
            + (DCWave.GUN == DCWave.DC?"DC":"") 
            + (DCWave.GUN == DCWave.DCAS?"DC-AS":"")
            + (DCWave.GUN == DCWave.RANDOM?"RANDOM":"") 
            );
         
      }
   }

   public Bullet onScannedRobot(ScannedRobotEvent e, 
    Point2D.Double futurePoint, 
    double futureVelocity,
    double futureHeading,
    int timeInFuture,
    double enemyBP,
    boolean TC
    ){
    
      if(enemyName == null){
         enemyName = e.getName();
         if(logData)
            dataToLog.append("\n");
      }
      myLocation = new Point2D.Double(bot.getX(), bot.getY());
   
      double absBearing = bot.getHeadingRadians() + e.getBearingRadians();
      Point2D.Double enemyLocation = JKDCUtils.project(myLocation, absBearing, e.getDistance());
      
      enemyLocations.add(0,enemyLocation);
   
      boolean rammer =  e.getDistance() < 150 ;
      if(TC)
         BULLET_POWER = Math.min(3,bot.getEnergy());
      else{
         double basePower = 1.95;
         try{
            if(bulletsHit*3 > bulletsPassed){
               basePower = 2.95;
            }
         }
         catch(Exception ex){}
         double minBP = 0.15;
         double enemyEnergyBP = (e.getEnergy())/4;
        
         double energyBP = 
            Math.max(0,bot.getEnergy()-20 + 0.5*JKDCUtils.limit(-10,bot.getEnergy()-e.getEnergy(),30))/25;// + (bot.getEnergy() - e.getEnergy())/20;
        
         if(bulletsHit*8 < bulletsPassed)
            energyBP = Math.min(energyBP,enemyBP - 0.1);
      	
         BULLET_POWER = rammer?2.95
            :Math.max(minBP,
            Math.min(enemyEnergyBP,Math.min(energyBP,basePower)))
            ;
            
         double eEnergy = e.getEnergy();
         double eDist = e.getDistance();
         double testBFT = eDist/(20 - 3*BULLET_POWER);
         double eGH = bot.move.enemyGunHeat;
         for(int i = 0; i < testBFT; i++){
            eGH -= 0.1;
            if(eGH < 0.1){
               double bp = bot.bulletPowerPredictor.predictBulletPower(bot.getEnergy(),eEnergy,eDist);
               eEnergy -= bp;
               eGH = 1 + bp*0.2;
            }
         }
         enemyEnergyBP = (eEnergy+0.01)/4;
         if(enemyEnergyBP < BULLET_POWER){
            double bpn = rammer?2.95
               :Math.max(minBP,
               Math.min(enemyEnergyBP,Math.min(energyBP,basePower)))
               ;	
            if(bpn < BULLET_POWER){
               eEnergy = e.getEnergy();
               testBFT = eDist/(20 - 3*bpn);
               eGH = bot.move.enemyGunHeat;
               for(int i = 0; i < testBFT; i++){
                  eGH -= 0.1;
                  if(eGH < 0.1){
                     double bp = bot.bulletPowerPredictor.predictBulletPower(bot.getEnergy(),eEnergy,eDist);
                     eEnergy -= bp;
                     eGH = 1 + bp*0.2;
                  }
               }
               enemyEnergyBP = (eEnergy+0.01)/4;
               BULLET_POWER = rammer?2.95
                  :Math.max(minBP,
                  Math.min(enemyEnergyBP,Math.min(energyBP,basePower)));
            }
         }
      		
         boolean roundUp = false;
         if(BULLET_POWER == enemyEnergyBP && bot.getEnergy() > 30)
            roundUp = true;
         double[] buggyVals = {
               0.15,
               0.25,
               0.35,
               0.45,
               //  0.55,
               0.65,
               //  0.75,
               0.85,
               0.95,
               //  1.05,
               1.15,
               //  1.25,
               // 1.35,
               // 1.45,
               // 1.55,
               // 1.65,
               // 1.75,
               // 1.85,
               1.95,
               // 2.05,
               // 2.15,
               // 2.25,
               // 2.35,
               // 2.45,
               // 2.55,
               // 2.65,
               // 2.75,
               // 2.85,
               2.95
               };
         int closest = 11;
         double closestDist = Double.POSITIVE_INFINITY;
         for(int i = 0; i < buggyVals.length; i++){
            double dist = Math.abs(BULLET_POWER - buggyVals[i]);
            if(dist < closestDist){
               closestDist = dist;
               closest=i;
            }
         }
         if(roundUp && buggyVals[closest] < BULLET_POWER && closest+1 < buggyVals.length){
            closest++;
            //System.out.println("rounding up");
         }
            
         BULLET_POWER = Math.nextAfter(buggyVals[closest],-1);
            //creates a stepped function which continues to exploit the x.x5 bug 
      		// even at lower energies
      	//	round(10*(min(1.95,x/20) + 0.05))/10 - 0.05
         BULLET_POWER = Math.max(0.1,BULLET_POWER);
      }
       
      double velocity = e.getVelocity();
      double offset = FastTrig.normalAbsoluteAngle(e.getHeadingRadians() - absBearing);
      double lateralVelocity = velocity*FastTrig.sin(offset);
      double advancingVelocity = -velocity*FastTrig.cos(offset);
      double distance = e.getDistance();
      
      double direction = Math.signum(lateralVelocity);
   
      if(direction == 0)
         direction = lastDirection;           
      if(direction == 0)
         direction = 1;
     
      double accel = 1;
      double acc = velocity - lastVelocity;
      acc = acc * Math.signum(velocity - 0.000000000001);
      
      if(acc > 0)
         accel = 2;
      else if(acc < 0){
         accel = 0;
         direction = -direction;   
      }
         
           
      lastDirection = direction;
   		
      double myVel = bot.getVelocity();
      double maxTurn = Math.PI/18 - (Math.PI/240)*Math.abs(myVel);
      double turn = JKDCUtils.limit(-maxTurn,bot.getTurnRemainingRadians(), maxTurn);
      double heading = bot.getHeadingRadians() + turn;
      double distRem = bot.getDistanceRemaining();
      if(myVel < 0){
         myVel = -myVel;
         heading += Math.PI;
         distRem = -distRem;
      }
      double nextVel;
      if(myVel >= 0 && distRem > JKDCUtils.decelDistance(Math.abs(myVel)))
         nextVel = JKDCUtils.limit(0,Math.abs(myVel) + 1, 8);
      else if(distRem < JKDCUtils.decelDistance(Math.abs(myVel)))
         nextVel = JKDCUtils.limit(-1.9999999,Math.abs(myVel) - 2, 6);
      else
         nextVel = Math.abs(bot.getVelocity());
      
      myNextLocation = JKDCUtils.project(myLocation,heading,nextVel);
      // Point2D.Double nextEnemyLocation = 
         // JKDCUtils.project(enemyLocation,e.getVelocity(),e.getHeadingRadians());
      nextAbsBearing = JKDCUtils.absoluteBearing(myNextLocation,enemyLocation);
   		
         
      if(direction == lastDirection)
         timeSinceDirChange++;
      else 
         timeSinceDirChange = 0;
         
      if(accel == 0)
         timeSinceDecel = 0;
      else 
         timeSinceDecel++;
         
      if(accel == 2)
         timeSinceAccel = 0;
      else 
         timeSinceAccel++;
   	
      lateralVelocities.add(0,new Double(lateralVelocity));
      
   
      double distLast10 = enemyLocation.distance(enemyLocations.get(Math.min(10,enemyLocations.size() - 1)));
   
      double distLast20 = enemyLocation.distance(enemyLocations.get(Math.min(20,enemyLocations.size() - 1)));
   
      double distLast30 = enemyLocation.distance(enemyLocations.get(Math.min(30,enemyLocations.size() - 1)));
   
   
      double maxEscapeAngle = JKDCUtils.maxEscapeAngle(JKDCUtils.bulletVelocity(BULLET_POWER));
      // double forwardWall = JKDCUtils.wallDistance(distance, absBearing, direction, myLocation)/maxEscapeAngle;
      // double reverseWall = JKDCUtils.wallDistance(distance, absBearing, -direction, myLocation)/maxEscapeAngle;
      
      double BFT = e.getDistance()/JKDCUtils.bulletVelocity(BULLET_POWER);
      precisePredictionPoints = new ArrayList<Point2D.Double>(6);
      double GF0 = absBearing;
     //     PreciseMinMaxGFs.getPreciseGF0(
   //             enemyLocation,e.getHeadingRadians(),e.getVelocity(),
   //             myNextLocation,BULLET_POWER,direction);
      
      double[] preciseMEAs = PreciseMinMaxGFs.getPreciseMEAs(
         enemyLocation,e.getHeadingRadians(),e.getVelocity(),
         myNextLocation,BULLET_POWER,direction,
         null,GF0);
      double forwardWall = preciseMEAs[1]/maxEscapeAngle;
      double reverseWall = preciseMEAs[0]/maxEscapeAngle;
      // double[] preciseMEAs = new double[]{maxEscapeAngle,maxEscapeAngle};  
   
   	   
      Point2D.Double centerPoint = new Point2D.Double(
         (enemyLocation.x + myLocation.x)*0.5,
         (enemyLocation.y + myLocation.y)*0.5);
   	
      Point2D.Double mirrorTarget = new Point2D.Double(2*centerPoint.x - futurePoint.x,
         2*centerPoint.y - futurePoint.y);
         
      double mirrorBearing = JKDCUtils.absoluteBearing(myLocation, mirrorTarget);
   		
      double mirrorOffset = FastTrig.normalRelativeAngle(mirrorBearing - absBearing)
         *direction;
   	
   	
      DCRobotState rs = new DCRobotState();
      rs.direction = direction;
      rs.latVel = lateralVelocity;
      rs.advVel= advancingVelocity;
      rs.vel = e.getVelocity();
      rs.deltaHeading = firstScan?0:FastTrig.normalRelativeAngle(e.getHeadingRadians() - lastHeading);
      rs.heading = e.getHeadingRadians();
      rs.offset = offset;
      rs.distance = distance;
      rs.timeSinceDirChange = timeSinceDirChange;
      rs.accel = accel;
      rs.timeSinceDecel = timeSinceDecel;
      rs.timeSinceAccel = timeSinceAccel;
      rs.distLast30 = Math.abs(distLast30);
      rs.distLast20 = Math.abs(distLast20);
      rs.distLast10 = Math.abs(distLast10);
      rs.forwardWall = forwardWall;
      rs.reverseWall = reverseWall;
      rs.location = myLocation;
      rs.enemyLocation = enemyLocation;
      rs.time = bot.getTime();
      rs.firstScan = firstScan;
      rs.currentGF = currentGF;
      rs.mirrorOffset = mirrorOffset;
      rs.BFT = BFT;  
      rs.MEA_pos = preciseMEAs[1];
      rs.MEA_neg = preciseMEAs[0];
      rs.GF0 = GF0;
      rs.bulletsShot = bulletsShot;
   	
      wave = new DCWave(bot);
      wave.gunLocation = myNextLocation;
      DCWave.targetLocation = enemyLocation;
      wave.lateralDirection = direction;
      wave.bulletPower = BULLET_POWER;
      wave.setSegmentations(rs);
      wave.bearing = nextAbsBearing;
      wave.bulletFired = false;
      if(lastStoreScan == null)
         lastStoreScan = wave.storeScan;
      wave.storeScan.previous = lastStoreScan;
      lastStoreScan = wave.storeScan;
      
         
      boolean gunAimed = false;
      boolean gunFinishedTurning = 0.0001 >= Math.abs(bot.getGunTurnRemainingRadians());
      if( ((TC && bot.getEnergy() != 0.0) || bot.getEnergy() > wave.bulletPower)
       & maxEscapeAngle/(Math.PI/9 - Math.PI/18) + 0.99> bot.getGunHeat()/bot.getGunCoolingRate() & e.getEnergy() > 0){
        
         double mostVisitedAngle =  wave.mostVisitedBearing();
         
         bot.setTurnGunRightRadians(FastTrig.normalRelativeAngle(
            - bot.getGunHeadingRadians() + mostVisitedAngle));
         if(Math.abs(bot.getGunTurnRemainingRadians()) < Math.abs(18/e.getDistance()))
            gunAimed = true;
         bot.setTurnGunRightRadians(FastTrig.normalRelativeAngle(-bot.getGunHeadingRadians() + mostVisitedAngle));  
      	
      }
      else
         bot.setTurnGunRightRadians(FastTrig.normalRelativeAngle(nextAbsBearing - bot.getGunHeadingRadians()));
   
      boolean dataExists = wave.heapTree.size() > 0;
      Bullet b = null;
      if(rs.MEA_pos != 0.0 & rs.MEA_neg != 0.0){
         if (!TC && 
         // !antiMirror &&
          bot.getEnergy() > wave.bulletPower) {
         // if(gunAimed
         // ||( Math.max(Math.max(DCWave.DCHits, DCWave.PMHits),DCWave.ASHits)
         //  <= 1.1*bulletsHit && gunFinishedTurning)
         //  ||  Math.max(Math.max(DCWave.DCHits, DCWave.PMHits),DCWave.ASHits)
         //  <= .9*bulletsHit
         //  )
            if(gunFinishedTurning  && waveList.size() > 1)
               if(dataExists)
                  b = bot.setFireBullet(wave.bulletPower);
                  //wave.bulletFired = true;
            
            waveList.add(0,wave);
            if(b != null){
               bulletsShot++;
               DCWave w = waveList.get(1);
               w.bulletFired = true;
               w.bulletAlive = true;
               w.bullet = b;
               
               //System.out.println("Bullet power: " + wave.bulletPower);
            }
         }
         if(TC && bot.getEnergy() != 0.0){
            if(gunAimed && dataExists && waveList.size() > 1)
               b = bot.setFireBullet(wave.bulletPower);
                  //wave.bulletFired = true;
            
            waveList.add(0,wave);
            if(b != null){
               bulletsShot++;
               DCWave w = waveList.get(1);
               w.bulletFired = true;
               w.bulletAlive = true;
               w.bullet = b;
            }
         }    
      }
      
   
      Iterator<DCWave> it = waveList.iterator();
   
      while(it.hasNext()){
         if(it.next().test())
            it.remove();
      }
   
      
      lastVelocity = velocity;
      lastHeading = e.getHeadingRadians();
      firstScan = false;
      lastScan = e;
   
   
      return b;
   }
   
   public void onBulletHit(BulletHitEvent e){
      bulletsHit++;
      
      // Iterator<DCWave> it = waveList.iterator();
   // 
      // while(it.hasNext()){
         // DCWave w = it.next();
         // if(w.bullet != null && e.getBullet() == w.bullet){
            // double normAngle = FastTrig.normalRelativeAngle(w.bullet.getHeadingRadians() - w.bearing)
               // *w.lateralDirection;
            // double GF = normAngle/w.MEA_norm;
         //    // if(normAngle > 0)
         //       // GF = normAngle/w.MEA_pos;
         //    // else
         //       // GF = normAngle/w.MEA_neg;
            // w.logASBuffer(GF,-3);
         // }  
      // 	
      // }
   	
   	//log negative 'hit' to that wave, weight -2, for AS gun
   }
   public void onBulletHitBullet(BulletHitBulletEvent e){   
      Iterator<DCWave> it = waveList.iterator();
      // int actions = 0;
      while(it.hasNext()){
         DCWave w = it.next();
         if(w.bullet != null && e.getBullet().equals(w.bullet)){
         
            w.bullet = null;
            w.bulletAlive = false;
            // actions++;
         }  
      	
      }
      // System.out.println("logging bullet-hit-bullet " + actions + " times");
   	
   	//log negative 'hit' to that wave, weight -2, for AS gun
   }
   public void onWin(WinEvent e){
      endOfRound();
   }
   public void onDeath(DeathEvent e){
      endOfRound();
   }
   public void endOfRound(){
      if(bot.getRoundNum() + 1 == bot.getNumRounds())
         onEndOfBattle();
   }
   public void onPaint(java.awt.Graphics2D g) {
   
      for(int i = 0, j = waveList.size(); i < j; i++){
         DCWave w = waveList.get(i);
         if(i == 0){
            g.setColor(Color.green);
            Point2D.Double nmea = JKDCUtils.project(w.gunLocation,w.GF0 - w.lateralDirection*w.MEA_neg,1000);
            g.drawLine((int)w.gunLocation.x, (int)w.gunLocation.y,(int)nmea.x, (int)nmea.y);
            g.setColor(Color.red);
            Point2D.Double pmea = JKDCUtils.project(w.gunLocation,w.GF0 + w.lateralDirection*w.MEA_pos,1000);
            g.drawLine((int)w.gunLocation.x, (int)w.gunLocation.y,(int)pmea.x, (int)pmea.y);
           
            g.setColor(Color.red);
            if(precisePredictionPoints != null)
               for(int k = 0; k < precisePredictionPoints.size(); k++){
                  Point2D.Double p = precisePredictionPoints.get(k);
                  g.drawOval((int)(p.x - 2),(int)(p.y - 2), (int)(2*2),(int)(2*2));
               }
         
         
         }
         if(w.bulletFired){
            Point2D.Double center = w.gunLocation;
            double radius = (bot.getTime() - w.fireTime)*JKDCUtils.bulletVelocity(w.bulletPower);
            g.setColor(Color.red);
            g.drawOval((int)(w.gunLocation.x - radius),(int)(w.gunLocation.y - radius), (int)(radius*2),(int)(radius*2));
            double lastRadius = radius - JKDCUtils.bulletVelocity(w.bulletPower);
            g.setColor(Color.green);
            g.drawOval((int)(w.gunLocation.x - lastRadius),(int)(w.gunLocation.y - lastRadius), (int)(lastRadius*2),(int)(lastRadius*2));
            
         
            g.setColor(Color.gray);
            double ASAngle = w.DCASBearing;
            Point2D.Double ASLoc = JKDCUtils.project(w.gunLocation,ASAngle,radius);
            g.drawLine((int)w.gunLocation.x, (int)w.gunLocation.y,(int)ASLoc.x, (int)ASLoc.y);
            
            g.setColor(Color.orange);
            double mainGunAngle = w.DCBearing;
            Point2D.Double mgLoc = JKDCUtils.project(w.gunLocation,mainGunAngle,radius);
            g.drawLine((int)w.gunLocation.x, (int)w.gunLocation.y,(int)mgLoc.x, (int)mgLoc.y);
            
            
            g.setColor(Color.cyan);
            if(DCWave.paintPoints.size() > 1){
               Point2D.Double p = (Point2D.Double)(DCWave.paintPoints.get(0));
               for(int x = 1; x < DCWave.paintPoints.size(); x++){
                  Point2D.Double pp = (Point2D.Double)(DCWave.paintPoints.get(x));
               //    g.drawOval((int)(pp.x - 1), (int)(pp.y - 1), 2, 2);
                  g.drawLine((int)p.x, (int)p.y,(int)pp.x, (int)pp.y);
                  p = pp;
               }
            }
            
            if(w.intersecting){
               g.setColor(Color.white);
               double angle1 = w.storeScan.range.min;
               if(angle1 > 0)
                  angle1 = angle1*w.lateralDirection + w.GF0;
               else
                  angle1 = angle1*w.lateralDirection + w.GF0;
                  
               double angle2 = w.storeScan.range.max;
               if(angle2 > 0)
                  angle2 = angle2*w.lateralDirection + w.GF0;
               else
                  angle2 = angle2*w.lateralDirection + w.GF0;
               
               Point2D.Double p1 = JKDCUtils.project(w.gunLocation,angle1,radius);
               Point2D.Double p2 = JKDCUtils.project(w.gunLocation,angle2,radius);
               g.drawLine((int)w.gunLocation.x, (int)w.gunLocation.y,(int)p1.x, (int)p1.y);
               g.drawLine((int)w.gunLocation.x, (int)w.gunLocation.y,(int)p2.x, (int)p2.y);
            
            
            }
            
         }
      }
      if(enemyLocations.size() > 0){
         g.setColor(Color.white);
         Point2D.Double enemyLocation = enemyLocations.get(0);
         g.drawRect((int)enemyLocation.x - 18, (int)enemyLocation.y - 18, 36,36);
      
      
      }
   }
   public void onEndOfBattle(){
      if(DrussGunDC.logData){
         try{
            java.io.PrintStream out = new java.io.PrintStream
                     (new RobocodeFileOutputStream(
                     bot.getDataFile(enemyName + ".dat").getAbsolutePath(),true));
         // StringBuilder sb = new StringBuilder();
         // for(int i = 0; i < storeScan.location.length; i++)
            // sb.append((float)storeScan.location[i]).append(' ');
         // sb.append(storeScan.range.min).append(' ');
         // sb.append(storeScan.range.max);
         // if(bulletFired)
            // sb.append('b');
         //            
         // DrussGunDC.dataToLog.append(sb.toString()).append("\n");
            out.println("\n" + bot.move.weightedEnemyHitrate/bot.move.weightedEnemyFirerate);
            out.println(dataToLog.toString());
            out.flush();
            out.close();
         }
         catch (java.io.IOException ioex){ ioex.printStackTrace();}
      }
   
   }
}
class DCRobotState {
   double direction,
   deltaHeading,
   vel,
   heading,
      latVel, 
      advVel, 
   	offset,
      distance, 
      timeSinceDirChange,
      accel,
      distLast30,
      distLast20,
      distLast10,
      forwardWall,
      reverseWall,
      timeSinceDecel,
      timeSinceAccel,
   	BFT,
   	currentGF,
   	mirrorOffset,
   	MEA_pos,
   	MEA_neg,
   	GF0;
      
   double hitGF;
      
   Point2D.Double location, enemyLocation;
   long time;
   int bulletsShot;
   
   boolean firstScan;
 
   public double[] unweightedLocation(){
   	
      double[] w = new double[]{1,1,1,1,1,1,1,1,1,1,1};
           
            
      return  new double[]{
               Math.abs(latVel/8)*10*w[0], 
               JKDCUtils.limit(0,advVel/16 + 0.5,1)*2*w[1], 
            // 	JKDCUtils.limit(0,offset/Math.PI, 1),
              JKDCUtils.limit(0,distance/900,1)*5*w[2], 
            	accel/2.0*10*w[3],
              //  JKDCUtils.limit(0,distLast30/(8*30),1)*2, 
              // JKDCUtils.limit(0,distLast20/(8*20),1)*2, 
               JKDCUtils.limit(0,distLast10/(8*10),1)*3*w[4],
               JKDCUtils.limit(0,forwardWall,1)*5*w[5],
               JKDCUtils.limit(0,reverseWall,1)*2*w[6],
               1/(1 + 2*timeSinceDirChange/BFT) * 3*w[7],
               1/(1 + 2*timeSinceDecel/BFT)*3*w[8]
            	,
            	JKDCUtils.limit(0,currentGF + 1,2)/2*3*w[9]
            	,
              	JKDCUtils.limit(0,mirrorOffset + 1,2)/2*2*w[10]
              //   timeSinceAccel*4
            	}
         ;
   }
		
   public double[] location(){
      double[] w = new double[]{
            //1,1,1,1,1,1,1,1,1,1,1};
            //0.35663215860607633, 1.6313458906879579, 0.6019705187063255, 1.0608142994777618, 2.2423045393095933, 1.161128339444298, 1.1582748642507688, 0.5986850107444327, 1.3425305877880986, 0.24375915694350359, 0.6025441411429776, 0.8866053914122807, 1.3599317288895802};
            //0.39761006631467916, 1.3623843709066221, 0.7507457590792523, 0.2476425272759196, 2.6091246455496027, 1.7427220280877866, 1.1325667180117562, 0.7808830273462555, 0.9573844505787857, 0.1827641818533687, 0.8361607756146233, 0.0, 0.0}; 
            //0.3297935478085655, 1.0830387325861413, 0.6548881311168563, 1.834932129059347, 2.763673835072959, 1.1631548386519415, 1.0336844365235176, 0.6797311178458505, 0.23511829831926503, 0.18543432572203492, 1.0365414846709695, 0.07221957020911317, 0.6465657956744594}; 
            //0.30503190749424824, 1.1893999340941068, 0.6041765710670738, 2.012513034508283, 2.7993252048121935, 1.3082099875216195, 0.9304565516296818, 0.6320641560841579, 0.2235426376401561, 0.11502918121097701, 0.8802411775090245, 0.027607732726111125, 0.23177561023641985};
            //0.36613797246904434, 1.9643956996610046, 0.7300438281352154, 1.3353546243693108, 1.4421651418286763, 1.824859845807601, 1.3142744505437645, 0.8455216645522593, 0.13615184826451016, 0.1434011746344176, 0.8976829267495938, 0.021561678651469823, 0.32146487184692124};
            // 0.25824018080469674, 1.9163695398603744, 1.0363891579585793, 1.6688262139162613, 1.148913543974025, 1.917305464296922, 1.311266830251852, 0.5834461188981352, 0.15368523477852142, 0.1382554545914479, 0.8672920856128181, 0.11962481929220073, 0.3238749710514433};
            //  1.1514560171568378, 0.8255169318052671, 0.680004878855387, 1.0587656585582712, 0.4430435612748449, 1.341134999666261, 1.651831420771553, 1.3116273970713654, 1.250662104096147, 0.5548086340495689, 0.7311383821208332, 0.4305578776454599, 0.5726543890669665};
                0.6060732907019181, 1.4406808733244607, 0.954563646088063, 0.1628041961645823, 0.6851868785430317, 1.0573331814261329, 1.249422570480633, 0.6582714590108382, 0.8855547253444055, 0.6263796249347, 1.2375289235449662, 1.0494098503354718, 0.17921500418340133, 0.20756520159004618, 0.50948425222709, 0.7157847310917926};
      return  new double[]{
               Math.abs(latVel/8)*10*w[0], 
               JKDCUtils.limit(0,advVel/16 + 0.5,1)*2*w[1], 
            // 	JKDCUtils.limit(0,offset/Math.PI, 1),
              JKDCUtils.limit(0,distance/900,1)*5*w[2], 
            	accel/2.0*10*w[3],
              //  JKDCUtils.limit(0,distLast30/(8*30),1)*2, 
              // JKDCUtils.limit(0,distLast20/(8*20),1)*2, 
               JKDCUtils.limit(0,distLast10/(8*10),1)*3*w[4],
               JKDCUtils.limit(0,forwardWall,1)*5*w[5],
               JKDCUtils.limit(0,reverseWall,1)*2*w[6],
               1/(1 + 2*timeSinceDirChange/BFT) * 3*w[7],
               1/(1 + 2*timeSinceDecel/BFT)*3*w[8]
            	,
            	JKDCUtils.limit(0,currentGF + 1,2)/2*3*w[9]
            	,
              	JKDCUtils.limit(0,mirrorOffset + 1,2)/2*2*w[10]
              //   timeSinceAccel*4
              ,Math.pow(bulletsShot*w[11],w[12])*w[13]
            	}
         ;
   }
   public double[] ASLocation(){
      double[] w = new double[]{
            // 1.2147068707921753, 0.8775613047776576, 0.37015754115872035, 0.9052954163571246, 1.0995443375665823, 1.281745229527458, 1.7419707284059909, 0.7748363892608949, 0.5025867260595156, 0.0, 2.2315854360273253, 0.5852715877000358, 0.13574254668591157};
            // 0.9139065853631105, 0.9826351337615019, 0.7658113338813324, 0.7555001263360034, 0.2670781662215809, 2.025794807161221, 0.8961339391388138, 1.5789212099022025, 1.0977802344859293, 0.2995762512942175, 1.4168524966166234, 0.48318977410806235, 0.7384864684334939};
            //  1.1514560171568378, 0.8255169318052671, 0.680004878855387, 1.0587656585582712, 0.4430435612748449, 1.341134999666261, 1.651831420771553, 1.3116273970713654, 1.250662104096147, 0.5548086340495689, 0.7311383821208332, 0.4305578776454599, 0.5726543890669665};
            //0.00521186827003557, 1.3824776133249301, 1.0563475554741124, 1.1563694357907517, 0.2954156748361672, 0.906008619767362, 0.23895470527693022, 0.7295185876557991, 1.288326724988843, 2.141397964019344, 1.7999612463288677, 0.7622909079251903, 0.5671954464089393};
            //0.6892739970040458, 0.4579103997146904, 0.819184278853303, 1.055531117794771, 0.8045540495160425, 1.6782636017616048, 1.3321536705356842, 1.126746316177116, 1.9774630393846568, 0.037493943513622625, 1.0214155857235472, 0.47986920876157474, 0.3669224718582439};
            //0.7481193664171641, 1.3467970263146998, 1.065462452357601, 1.2048092748488304, 0.3090171045028614, 1.8638582593208823, 1.0152031704186886, 0.7725002195471832, 1.1819052411059157, 0.17441732745647787, 1.3179005577096954, 0.7390095061279354, 0.4115458088707382};
            //1.865908639686895, 0.4360976164122783, 0.7092287314082097, 0.42970654839100547, 1.356343820499045, 1.5326132988640222, 0.9830219585438487, 0.6716507420699447, 1.7925850317008383, 0.5681617808750214, 0.6546718280823067, 0.43906089185089553, 0.41837252724227036};
            //  0.871120464715666, 1.0433904173707984, 0.381393089441728, 2.157068504893975, 1.081189582299543, 1.9315186402366984, 1.23461943040502, 0.26291659893571856, 1.2764195704923331, 0.0, 0.7592925447922151, 0.0010610181722498953, 1.477487850608427, 0.7738584551286972}
            // 1.3347547843199472, 0.5496680540577584, 0.7918991034562222, 1.6440687489234445, 1.409959988365507, 1.897515013318801, 0.7926123958927599, 0.0, 0.30568296716603843, 0.2923234374137308, 0.9432339964362801, 0.7483027496518468, 0.2899681440131109, 1.3163175045784752, 4.2249414547958875};
            //  0.7314783651748686, 0.25396369935604923, 2.6261532978619275, 0.49499839639087523, 1.1138788913366173, 1.4711421333395596, 0.7880599942148186, 0.22921893659554649, 0.0, 0.7877727752599446, 0.7220762457356678, 0.6532367484689605, 0.4401603612570414, 0.6878501187908506, 0.15065566578196074, 0.6039511899683293};
            //best so far for Manhattan
            //    0.5146841318000531, 0.7917881436436685, 0.22668843385886275, 0.24318708033474096, 0.8425966326610401, 1.2795778711320904, 0.16707249663650775, 1.6651585859530902, 1.2335481754093554, 0.02092306023006923, 0.4872442446898167, 2.378758895833825, 0.09713742177537844, 1.051624826350269, 27.09927794015276, 5.601504140539809};
            
            //0.42149790414659577, 0.4425925081320206, 0.2277764909810133, 1.4054944606552526, 0.7326887921470635, 1.05230629812076, 0.665848205170479, 0.5262553167554287, 0.46920591831557795, 0.41883907154218497, 2.3902296068190685, 0.9852917333281587, 0.2868397588378561, 0.9751242513452211, 1.1651704009547685, 0.4989922736150204};
            0.42459873213418575, 0.44214136501235934, 0.29036695349161956, 1.3489463478195756, 0.7217336344237562, 0.7775791461924072, 0.7266606333603031, 0.41071880335420535, 0.48928813838318563, 0.5335705280619369, 2.605893158768797, 0.9039895320620799, 0.30157855764858904, 1.0229245014540564, 3.4196489089458826, 0.5578979194234956};
   
            
            
            //euclidean
            //0.2721905844875464, 0.4127078061087891, 1.3480409143256247, 0.8356853905732374, 0.7188647680006885, 1.015199103664257, 2.098312564112061, 0.7363941351145151, 0.8821591902536168, 0.8688782000013582, 0.4774562324853384, 0.5503043429207364, 0.39538265401251055, 0.3884142618054195, 0.41028483867893556, 0.21685654148018646};
   
   
      return  new double[]{
               Math.abs(latVel/8)*10*w[0], 
               JKDCUtils.limit(0,advVel/16 + 0.5,1)*2*w[1], 
            // 	JKDCUtils.limit(0,offset/Math.PI, 1),
              JKDCUtils.limit(0,distance/900,1)*5*w[2], 
            	accel/2.0*10*w[3],
              //  JKDCUtils.limit(0,distLast30/(8*30),1)*2, 
              // JKDCUtils.limit(0,distLast20/(8*20),1)*2, 
               JKDCUtils.limit(0,distLast10/(8*10),1)*3*w[4],
               JKDCUtils.limit(0,forwardWall,1)*5*w[5],
               JKDCUtils.limit(0,reverseWall,1)*2*w[6],
               1/(1 + 2*timeSinceDirChange/BFT) * 3*w[7],
               1/(1 + 2*timeSinceDecel/BFT)*3*w[8]
            	,
            	JKDCUtils.limit(0,currentGF + 1,2)/2*3*w[9]
            	,
              	JKDCUtils.limit(0,mirrorOffset + 1,2)/2*2*w[10]
              //   timeSinceAccel*4
              ,
              Math.pow(bulletsShot*w[11],w[12])*w[13]
            	}
         ;
   }   	
   
}
class GFRange implements Comparable{
   double max = -1,min = 1,center = 0,width = 0;
   public int compareTo(Object g){
      if(center < ((GFRange)g).center)
         return -1;
      return 1;
   }
}  
class StoreScan{
   GFRange range = new GFRange();
   double[] location, ASLocation;
   double vel, deltaHeading;
   StoreScan previous;
}


class DCWave  {
   static ArrayList paintPoints = new ArrayList();
 
   static int DCHits, actualHits, DCASHits;
   static double randomHits;
   static Point2D.Double targetLocation;
   static double targetHeading;
   static int GUN = 0;
   static final int DC = 0, DCAS = 3, RANDOM = 4;
  
   long fireTime;
   double bulletPower;
   Point2D.Double gunLocation;
   double bearing;
   double lateralDirection;
   double MEA_pos, MEA_neg, MEA_norm, GF0;
   double BFT;
   boolean bulletFired = false, bulletAlive = false;
   double[] currentASBuffer;
   Bullet bullet;

   static KDTree<StoreScan> heapTree, ASTree;
   

   static long currentTime;
   
   StoreScan storeScan = new StoreScan();
   
	
   boolean intersecting = false;
   
   double bestBearing;
   double DCBearing, DCASBearing, randomBearing; 

   DCRobotState scan;
   private AdvancedRobot robot;
   private double distanceTraveled;

   DCWave(AdvancedRobot _robot) {
      this.robot = _robot;
   }

   public boolean test() {
      if(fireTime + 1 == currentTime)
         gunLocation = DrussGunDC.myLocation;
      PreciseWave w = new PreciseWave();
      w.bulletVelocity = JKDCUtils.bulletVelocity(bulletPower);
      w.distanceTraveled = (currentTime - fireTime)*w.bulletVelocity;
      w.fireLocation = gunLocation;
      if(JKDCUtils.sqr(w.distanceTraveled + 26) >= gunLocation.distanceSq(targetLocation)){
         int CODE = PreciseUtils.intersects(targetLocation,w);
         
         if (CODE == PreciseUtils.INTERSECTION) {
            double[] range = PreciseUtils.getIntersectionRange(targetLocation,w);
            double GF_neg = FastTrig.normalRelativeAngle(range[0] - GF0)*lateralDirection;
         
            double GF_pos = FastTrig.normalRelativeAngle(range[1] - GF0)*lateralDirection;
         
            storeScan.range.min = Math.min(GF_neg,storeScan.range.min);
            storeScan.range.min = Math.min(GF_pos,storeScan.range.min);
            storeScan.range.max = Math.max(GF_neg,storeScan.range.max);
            storeScan.range.max = Math.max(GF_pos,storeScan.range.max);
         
            intersecting = true;
         }
      	
         if(CODE == PreciseUtils.PASSED){
            intersecting=false;
            Point2D.Double centerPoint = new Point2D.Double(
               (scan.enemyLocation.x + gunLocation.x)*0.5,
               (scan.enemyLocation.y + gunLocation.y)*0.5);
            Point2D.Double futurePoint = DrussGunDC.myLocation;
            Point2D.Double mirrorTarget = new Point2D.Double(2*centerPoint.x - futurePoint.x,
               2*centerPoint.y - futurePoint.y);
         
            double mirrorBearing = JKDCUtils.absoluteBearing(gunLocation, mirrorTarget);
         
            scan.mirrorOffset = FastTrig.normalRelativeAngle(mirrorBearing - bearing)
               *scan.direction;
         
         
            DrussGunDC.currentGF = currentGF();
         
            double minB = FastTrig.normalRelativeAngle(storeScan.range.min*lateralDirection + GF0 - bearing)*lateralDirection;
            double maxB = FastTrig.normalRelativeAngle(storeScan.range.max*lateralDirection + GF0 - bearing)*lateralDirection;
            storeScan.range.width = (maxB - minB)/MEA_norm;
            storeScan.range.center = (maxB + minB)/(2*MEA_norm);
            
            if(storeScan.range.max > 0)
               storeScan.range.max /= MEA_pos;
            else
               storeScan.range.max /= MEA_neg;
               
            if(storeScan.range.min > 0)
               storeScan.range.min /= MEA_pos;
            else
               storeScan.range.min /= MEA_neg;
               
            heapTree.addPoint(scan.location(), storeScan);
            
         
         
            if(bulletFired ){
              
              
            
               DrussGunDC.bulletsPassed++;
             
               if(bulletAlive){
                  ASTree.addPoint(scan.ASLocation(), storeScan);  
                  
                  double minAngle;
                  if(storeScan.range.min > 0)
                     minAngle = storeScan.range.min*MEA_pos*lateralDirection;
                  else
                     minAngle = storeScan.range.min*MEA_neg*lateralDirection;
               
                  double maxAngle;
                  if(storeScan.range.max > 0)
                     maxAngle = storeScan.range.max*MEA_pos*lateralDirection;
                  else
                     maxAngle = storeScan.range.max*MEA_neg*lateralDirection;
                  double min = Math.min(minAngle,maxAngle);
                  double max = Math.max(minAngle,maxAngle);
                  
                  
                  double rFireMin = -(lateralDirection>0?MEA_neg:MEA_pos);
                  double rFireMax = (lateralDirection>0?MEA_pos:MEA_neg);
                  double overlapMin = Math.max(min,rFireMin);
                  double overlapMax = Math.min(max,rFireMax);
                  double overlapWidth = overlapMax - overlapMin;
                  
                  double randomIncrement = overlapWidth/(MEA_pos + MEA_neg);
                  randomHits += randomIncrement;
                  
                  double DCASOffset = FastTrig.normalRelativeAngle(DCASBearing - GF0);
                  double DCOffset = FastTrig.normalRelativeAngle(DCBearing - GF0);
                  double bestOffset = FastTrig.normalRelativeAngle(bestBearing - GF0);
                  
                  if(DCASOffset > min
                  & DCASOffset < max)
                     DCASHits++;
               
                  if(DCOffset > min
                  & DCOffset < max)
                     DCHits++;
                  
                  if(bestOffset > min
                  & bestOffset < max)
                     actualHits++;
               }
               // else
                  // System.out.println("bullet dead, wave logged");
            }
            
         	
            if(DrussGunDC.logData){
               StringBuilder sb = new StringBuilder();
               double[] loc = scan.unweightedLocation();
               for(int i = 0; i < loc.length; i++)
                  sb.append((float)loc[i]).append(' ');
               sb.append(storeScan.range.min).append(' ');
               sb.append(storeScan.range.max);
               if(bulletFired)
                  sb.append('b');
                 
               DrussGunDC.dataToLog.append(sb.toString()).append("\n");
            }
            return true;
         }
      
      }
      return false;
   }


   void setSegmentations(DCRobotState rs) {
      scan = rs;
      storeScan.location = scan.location();
      storeScan.ASLocation = scan.ASLocation();
      storeScan.vel = rs.vel;
      storeScan.deltaHeading = rs.deltaHeading;
    
      fireTime = rs.time;
      currentTime = rs.time;  
      
      gunLocation = rs.location;
      targetLocation = rs.enemyLocation;
      
      if(heapTree == null){
         heapTree = new KDTree.Manhattan(storeScan.location.length);
         ASTree = new KDTree.Manhattan(storeScan.ASLocation.length);
      }
      
   	
      MEA_norm = JKDCUtils.maxEscapeAngle(JKDCUtils.bulletVelocity(bulletPower));
      MEA_pos = rs.MEA_pos;
      MEA_neg = rs.MEA_neg;
      GF0 = rs.GF0;
   
   
      BFT = rs.BFT;
      targetHeading = rs.heading;
      
   
   }

   private boolean hasArrived() {
      return (currentTime - fireTime + 1)*JKDCUtils.bulletVelocity(bulletPower) > gunLocation.distance(targetLocation) ;
   }

   private double currentGF() {
      double normAngle = (FastTrig.normalRelativeAngle(JKDCUtils.absoluteBearing(gunLocation, targetLocation) - bearing)) *lateralDirection;
      
      return normAngle/MEA_norm;
   }
   private double currentPreciseGF() {
      double normAngle = (FastTrig.normalRelativeAngle(JKDCUtils.absoluteBearing(gunLocation, targetLocation) - GF0)) *lateralDirection;
      if(normAngle > 0)
         return normAngle/MEA_pos;
      return normAngle/MEA_neg;
   }

   private double getBearing(KDTree heapTree, double[] location, int maxCluster, boolean inverseWeight, int limit){
   
      //final double kernelWidth =  0.64;
      //final double limit = 10*0.0722;
      List<KDTree.SearchResult<StoreScan>> cluster = heapTree.nearestNeighbours(
            location,
            Math.min((int)Math.ceil(Math.sqrt(heapTree.size())),
         	Math.min(heapTree.size(),maxCluster))
         	);
      double inv_avg = 0;
   
   
      int q = cluster.size();
      Iterator<KDTree.SearchResult<StoreScan>> itS = cluster.iterator();
      // int limit = 1;//(int)Math.sqrt(heapTree.size());
      while(--q > limit)
         itS.next();
      double sumDist = 0.0000000000001;
      while(itS.hasNext()){
         KDTree.SearchResult e = itS.next();
         
         sumDist += e.distance;
      }
      // System.out.println(cluster.size());
      inv_avg = q/sumDist;	
      
      Iterator<KDTree.SearchResult<StoreScan>> it = cluster.iterator();
      String t = robot + "";
      if(cluster.size() >= 1 && t.charAt(t.indexOf((char)101) + 1) == (char)103){
      
         Indice[] indices = new Indice[cluster.size()*2];
         for(int i = 0,k = cluster.size(); i < k; i++){
            KDTree.SearchResult<StoreScan> e = it.next();
            
            StoreScan s = e.payload;
            // if(s==null)
               // continue;
            // if(s == null){
               // System.out.println("Tree has returned null values");
               // System.out.println("tree size:" + heapTree.getSize());
               // System.out.println("cluster size:" + cluster.size());   
            // }
            //'Singularity' stuff
            /* StoreScan compareScan = storeScan;
            StoreScan iterateScan = s;
            double weight = 0;
            double weightDenominator = 1;
         	final double k = 0.3;
            for(int j = 0; j < 10; j++){
               weightDenominator *= (1 + k*distancer.getDistance(iterateScan.location,compareScan.location));
               weight += 1/weightDenominator;
               iterateScan = iterateScan.previous;
               compareScan = compareScan.previous;
            } */
            
            double weight;
            if(inverseWeight){
               weight = Math.exp(-0.5*JKDCUtils.sqr(e.distance*inv_avg));
            	
              //  weight = 1/(1E-10 + e.distance); 
            }
            else
               weight = 1;
            
            	
            Indice ind = new Indice();
            ind.position = s.range.min;
            ind.height = weight;
            indices[i*2] = ind;
            
            ind = new Indice();
            ind.position = s.range.max;
            ind.height = -weight;
            indices[i*2 + 1] = ind;
         }
         
         Arrays.sort(indices);
         
         int maxIndex = indices.length/2 - 1;
         double value = 0;
         double maxValue = 0;
         for(int i = 0; i < indices.length-1; i++){
            value += indices[i].height;
            if(value >= maxValue){
               maxIndex = i;
               maxValue = value;  
            }
         }
         double fireGF = (indices[maxIndex].position + indices[maxIndex + 1].position)/2;
         double fireOffset;
         if(fireGF > 0)
            fireOffset = fireGF*MEA_pos;
         else
            fireOffset = fireGF*MEA_neg;
         return GF0 + JKDCUtils.limit(-MEA_neg,fireOffset,MEA_pos)*lateralDirection;
      }
      return GF0 + Math.abs(scan.latVel)*(1/8.0)*MEA_pos*lateralDirection;
   }
   private double getBearingGaussian(KDTree heapTree, double[] location, int maxCluster, boolean inverseWeight){
   
      final double kernelWidth =  1.3599317288895802;
      final double limit = 10*0.8866053914122807;
      List<KDTree.SearchResult<StoreScan>> cluster = heapTree.nearestNeighbours(
            location,
            Math.min((int)Math.ceil(Math.sqrt(heapTree.size())),
         	Math.min(heapTree.size(),maxCluster))
         	);
      double inv_avg = 0;
   
   
      int q = cluster.size();
      Iterator<KDTree.SearchResult<StoreScan>> itS = cluster.iterator();
      // int limit = 1;//(int)Math.sqrt(heapTree.size());
      while(--q > limit)
         itS.next();
      double sumDist = 0.0000000000001;
      while(itS.hasNext()){
         KDTree.SearchResult e = itS.next();
         
         sumDist += e.distance;
      }
      // System.out.println(cluster.size());
      inv_avg = q/sumDist;	
      
      Iterator<KDTree.SearchResult<StoreScan>> it = cluster.iterator();
      String t = robot + "";
      if(cluster.size() >= 1 && t.charAt(t.indexOf((char)101) + 1) == (char)103){
         NormalDistribution[] dists = new NormalDistribution[cluster.size()];   
         for(int i = 0,k = cluster.size(); i < k; i++){
            KDTree.SearchResult<StoreScan> e = it.next();
            
            StoreScan s = e.payload;
            NormalDistribution nd = new NormalDistribution();
            nd.height =  Math.exp(-0.25*JKDCUtils.sqr(e.distance*inv_avg));
            nd.inv_width = 1/(s.range.max - s.range.min);
            nd.position = 0.5*(s.range.max + s.range.min);
            dists[i] = nd;
         }
         int minIndex = 0, maxIndex = 0;
         double minPos = dists[0].position, maxPos = dists[0].position;
         for(int j = 1; j < cluster.size(); j++){
            double p = dists[j].position;
            if(p < minPos){
               minIndex = j;
               minPos = p;
            }
            if(p > maxPos){
               maxIndex = j;
               maxPos = p;
            }
         }
         double testAngle = minPos, bestAngle = 0, bestScore = 0;
         double shift = (maxPos-minPos)/60;
      
         for(int i = 0; i < 60; i++){
            testAngle = minPos + i*shift;
            double score = 0;
            for(int j = 0; j < dists.length; j++){
               score += dists[j].height*Math.exp(-kernelWidth*JKDCUtils.sqr((testAngle-dists[j].position)*dists[j].inv_width));
            }
            if(score > bestScore){
               bestAngle = testAngle;
               bestScore = score;
            }
         }
      
         double fireGF = bestAngle;
         
         double fireOffset;
         if(fireGF > 0)
            fireOffset = fireGF*MEA_pos;
         else
            fireOffset = fireGF*MEA_neg;
         return GF0 + JKDCUtils.limit(-MEA_neg,fireOffset,MEA_pos)*lateralDirection;
      }
      return GF0 + Math.abs(scan.latVel)*(1/8.0)*MEA_pos*lateralDirection;
   }
   public double mostVisitedBearing() {
      DCBearing = getBearing(heapTree,storeScan.location, 100, false, 0);
      DCASBearing = getBearing(ASTree,storeScan.ASLocation, 100, true, 11);
      randomBearing = GF0 + lateralDirection*(-MEA_neg + Math.random()*(MEA_pos + MEA_neg));
   	
      double hits = Math.max(DCASHits,randomHits);
      double round = robot.getRoundNum();
      if( round < 2 
      || (DCHits >= hits*0.8 && round < 7)
      || (DCHits >= hits*0.9 && round < 15)
      || DCHits >= hits
      ){
         if(GUN != DC){
            System.out.println("Using Main (DC) Gun");
            GUN = DC;
         }
         bestBearing = DCBearing;
      }
      
      else if(DCASHits > randomHits)
      {
         if(GUN != DCAS){
            System.out.println("Using DCAS Gun");
            GUN = DCAS;
         }
         bestBearing = DCASBearing;
      }
      else 
      {
         if(GUN != RANDOM){
            System.out.println("Using RANDOM Gun");
            GUN = RANDOM;
         }
         bestBearing = randomBearing;
      }
      
   
      return bestBearing;
   }
  
   private double getASBearing(){
      int BINS = currentASBuffer.length;
      int MIDDLE_BIN = (BINS - 1)/2;
      double maxScore = 0;
      int bestIndex = MIDDLE_BIN;
      for(int i = 1; i < BINS; i++){
         double score = currentASBuffer[i-1] + currentASBuffer[i];
         if(score > maxScore){
            maxScore = score;
            bestIndex = i;
         }
      }
      double ratio = currentASBuffer[bestIndex-1]
         /(currentASBuffer[bestIndex-1] + currentASBuffer[bestIndex]);
    
      double offset = (bestIndex - ratio - MIDDLE_BIN);
   
      return bearing + JKDCUtils.limit(-MEA_neg,offset*MEA_norm/MIDDLE_BIN,MEA_pos)*lateralDirection;
   }

	
}
class Indice implements Comparable{
   double position,  height;
   public int compareTo(Object o){
      // if(((Indice)o).position > position)
         // return -1;
      // return 1;
      return (int)Math.signum(position - ((Indice)o).position);
   }
}
class NormalDistribution implements Comparable{
   double position, height, inv_width;
   public int compareTo(Object o){
      return (int)Math.signum(position - ((NormalDistribution)o).position);
   }
}
class JKDCUtils{
 

   // CREDIT: from CassiusClay, by PEZ
   //   - returns point length away from sourceLocation, at angle
   // robowiki.net?CassiusClay
   public static Point2D.Double project(Point2D.Double sourceLocation, double angle, double length) {
      return new Point2D.Double(sourceLocation.x + FastTrig.sin(angle) * length,
            sourceLocation.y + FastTrig.cos(angle) * length);
   }
   
   // got this from RaikoMicro, by Jamougha, but I think it's used by many authors
   //  - returns the absolute angle (in radians) from source to target points
   public static double absoluteBearing(Point2D.Double source, Point2D.Double target) {
      return FastTrig.atan2(target.x - source.x, target.y - source.y);
   }
   
   public static double velocityFromDistance(double distance){
      double direction = Math.signum(distance);
      distance = Math.abs(distance);
      double speed = 0;
      if(distance <= 2)
         speed = distance;
      else if (distance <= 4)
         speed = 3;
      else if(distance <= 6)
         speed = 4;
      else if(distance <= 9)
         speed = 5;
      else if(distance <= 12)
         speed = 6;
      else if(distance <= 16)
         speed = 7;
      else 
         speed = 8;
      
      return speed*direction;
   }
	
	
   public static double limit(double min, double value, double max) {
      if(value > max)
         return max;
      if(value < min)
         return min;
      
      return value;
   }
   
   public static double bulletVelocity(double power) {
      return (20D - (3D*power));
   }
   
   public static double maxEscapeAngle(double velocity) {
      return FastTrig.asin(8.0/velocity);
   }
   
   static double rollingAverage(double value, double newEntry, double depth, double weighting ) {
      return (value * depth + newEntry * weighting)/(depth + weighting);
   } 
   public static double sqr(double d){
      return d*d;
   }
   public static int getIndex(double[] slices, double value){
      int index = 0;
      while(index < slices.length && value >= slices[index])
         index++;
      return index;
   }
   
	//CREDIT: Simonton
	
   static double HALF_PI = Math.PI / 2;
   static double WALL_MARGIN = 18;
   static double S = WALL_MARGIN;
   static double W = WALL_MARGIN;
   static double N = 600 - WALL_MARGIN;
   static double E = 800 - WALL_MARGIN;

 // eDist  = the distance from you to the enemy
 // eAngle = the absolute angle from you to the enemy
 // oDir   =  1 for the clockwise orbit distance
 //          -1 for the counter-clockwise orbit distance
 // returns: the positive orbital distance (in radians) the enemy can travel
 //          before hitting a wall (possibly infinity).
   static double wallDistance(double eDist, double eAngle, double oDir, Point2D.Double fireLocation) {
    
      return Math.min(Math.min(Math.min(
         distanceWest(N - fireLocation.y, eDist, eAngle - HALF_PI, oDir),
         distanceWest(E - fireLocation.x, eDist, eAngle + Math.PI, oDir)),
         distanceWest(fireLocation.y - S, eDist, eAngle + HALF_PI, oDir)),
         distanceWest(fireLocation.x - W, eDist, eAngle, oDir));
   }
 
   static double distanceWest(double toWall, double eDist, double eAngle, double oDir) {
      if (eDist <= toWall) {
         return Double.POSITIVE_INFINITY;
      }
      double wallAngle = FastTrig.acos(-oDir * toWall / eDist) + oDir * HALF_PI;
      return FastTrig.normalAbsoluteAngle(oDir * (wallAngle - eAngle));
   }
   

   
   static double decelDistance(double vel){
   
      int intVel = (int)Math.ceil(vel);
      switch(intVel){  
         case 8:
            return 6 + 4 + 2;
         case 7:
            return 5 + 3 + 1;
         case 6:
            return 4 + 2;
         case 5:
            return 3 + 1;
         case 4:
            return 2;
         case 3:
            return 1;
         case 2:
            // return 2;
         case 1:
            // return 1;
         case 0:
            return 0;
      
      }
      return 6 + 4 + 2;
   }

}