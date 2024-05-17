package jk.mega;

import jk.mega.dGun.*;
import jk.precise.util.*;
import robocode.*;
import robocode.util.Utils;
import java.awt.geom.*;     // for Point2D's
import java.lang.*;         // for Double and Integer objects
import java.util.*; // for collection of waves
import java.awt.Color;
//import ags.utils.*;
import java.io.*;
import jk.math.FastTrig;
import jk.tree.KDTree;

public class EnemyMoves{

   int predictTime;
   double dirFlip = 1;
   PointChain predictHead;
   double predictHeading, 
   predictStartHeading;
   Point2D.Double predictLocation, predictStartLocation;
   Locator predictLoc;
   int maxTime;
   
   ArrayList<Point2D.Double> futureLocs;// = new ArrayList<Point2D.Double>();
	
   PointChain currentPoint = null;
   Locator currentLoc;
	
   AdvancedRobot bot;
	
   double timeSinceDecel;
   double timeSinceDirChange;
   
   double lastDirection;
   double lastHeading;
   double lastVelocity = 0;
   Point2D.Double enemyLocation;
   
   ArrayList<Locator> addQueue = new ArrayList<Locator>();
	
   ArrayList<Point2D.Double> enemyLocations = new ArrayList<Point2D.Double>();
   
   static KDTree<PointChain> tree = new KDTree.Manhattan<PointChain>(new Locator().getLocation().length);
   
	
   public void onScannedRobot(ScannedRobotEvent e){
   
      Point2D.Double myLocation = new Point2D.Double(bot.getX(), bot.getY());
      double absBearing = bot.getHeadingRadians() + e.getBearingRadians();
      enemyLocation = project(myLocation, absBearing, e.getDistance());
      enemyLocations.add(enemyLocation);
      
      double velocity = e.getVelocity();
      double heading = e.getHeadingRadians();
      double offset = Utils.normalAbsoluteAngle(heading - absBearing);
      double lateralVelocity = velocity*FastTrig.sin(offset);
      double advancingVelocity = -velocity*FastTrig.cos(offset);
      double distance = e.getDistance();
   
   
      double accel = 1;
      double acc = velocity - lastVelocity;
      acc = acc * Math.signum(velocity);
      
      double direction = Math.signum(lateralVelocity);
      if(direction == 0)
         direction = lastDirection;           
      if(direction == 0)
         direction = 1;
   	   
      if(acc > 0)
         accel = 2;
      else if(acc < 0){
         accel = 0;
      }
      
      if(accel == 0)
         timeSinceDecel = 0;
      else 
         timeSinceDecel++;
         
      if(direction == lastDirection)
         timeSinceDirChange++;
      else 
         timeSinceDirChange = 0;
         
   		
      double distLast10 = enemyLocation.distance(enemyLocations.get(Math.min(10,enemyLocations.size() - 1)));
      
      double bulletPower = 2.95;
      double[] preciseMEAs = PreciseMinMaxGFs.getPreciseMEAs(
         enemyLocation,e.getHeadingRadians(),e.getVelocity(),
         myLocation,bulletPower,direction,
         new ArrayList(),absBearing);
         
      double mea=FastTrig.asin(8.0/(20 - 3*bulletPower));
      double forwardWall = preciseMEAs[1]/mea;
      double reverseWall = preciseMEAs[0]/mea;
      double BFT = distance/(20 - 3*bulletPower);
      
      Locator l = new Locator();
      l.vel = velocity;
      l.latVel = lateralVelocity;
      l.advVel = advancingVelocity;
      l.distance = distance;
      l.accel = accel;
      l.direction = direction;
      l.timeSinceDecel = timeSinceDecel;
      l.timeSinceDirChange = timeSinceDirChange;
      l.distLast10 = distLast10;
      l.forwardWall = forwardWall;
      l.reverseWall = reverseWall;
      l.BFT = BFT;
   	
   	
      PointChain npc = new PointChain();
      if(currentPoint != null)
         currentPoint.next = npc;
      npc.velocity = velocity;
      npc.deltaHeading = Utils.normalRelativeAngle(heading - lastHeading);
      currentPoint = npc;
      l.value = npc;
      
      addQueue.add(l);
      
      while(addQueue.size() > BFT){
         Locator arrived = addQueue.remove(0);
         tree.addPoint(arrived.getLocation(),arrived.value);
      }
      //tree.addPoint(l.getLocation(),npc);   
      currentLoc = l;
   		
      lastDirection = direction;
      lastVelocity = velocity;
      lastHeading = heading;
   }

   public EnemyMoves(AdvancedRobot b){
      bot = b;
   }
   
   public Point2D.Double get(int time){
      if(time > maxTime)
         extend(time);
      return futureLocs.get(Math.max(0,time - predictTime));
      // if(rp == null)
         // throw new IllegalArgumentException("null in futureLocs somehow...");
         //System.out.println("Returning null from " + (time - predictTime) + " out of " + futureLocs.size());
      // return rp;
   }
   
   public void extend(int time){
      int addSize = time - maxTime;
      int getIndex = 0;
      for(int i = 0; i < addSize; i++){
         // if(predictHead == null){
            // getIndex = getIndex + 1;
            // if(getIndex > tree.size()){
               // for(int j = i; j < addSize; j++)
                  // futureLocs.add(predictLocation);
               // i = addSize;  
            // }
            // else{
               // predict();
               // addSize = time - predictTime - futureLocs.size() + 1;
               // i = 0;
            // }  
         // }
         if(predictHead != null){
            predictHeading += predictHead.deltaHeading;
            predictLocation = project(predictLocation,predictHeading,predictHead.velocity*dirFlip);
            predictHead = predictHead.next;
         }
        // 
         // if(predictLocation == null)
            // throw new IllegalArgumentException("adding null");
      
         futureLocs.add(predictLocation);
         
      }
      maxTime += addSize;
   
   }
   public boolean initialised(){
      return futureLocs != null;
   }
   public void predict(){
      predictLoc = currentLoc;
      predictTime = (int)bot.getTime();
      predictStartHeading = lastHeading;
      predictStartLocation = enemyLocation;
      
      predict(0);
   }
   void predict(int nearIndex){
      List<KDTree.SearchResult<PointChain>> cluster = tree.nearestNeighbours(
            predictLoc.getLocation(),
            Math.min(nearIndex+1,tree.size()));
   
      if(cluster.size() > 0){
      
         predictHead = cluster.get(nearIndex).payload;
         dirFlip = Math.signum(predictHead.velocity) == Math.signum(predictLoc.vel)?1:-1;
      }
      futureLocs = new ArrayList<Point2D.Double>();
      predictHeading = predictStartHeading;
      predictLocation = predictStartLocation;
      maxTime = predictTime-1;
   }
	
	
	
   
	
	
   static class PointChain{
      PointChain next;
      double velocity, deltaHeading;
   }
   static class Locator{
      PointChain value;
      double vel,
      latVel,
      advVel,
      //offset,
      distance,
      accel,
      direction,
      timeSinceDecel,
      timeSinceDirChange,
      distLast10,
      forwardWall,
      reverseWall,
      BFT;
      double[] getLocation(){
         final double[] w = { 0.6060732907019181, 1.4406808733244607, 0.954563646088063, 0.1628041961645823, 0.6851868785430317, 1.0573331814261329, 1.249422570480633, 0.6582714590108382, 0.8855547253444055, 0.6263796249347, 1.2375289235449662, 1.0494098503354718, 0.17921500418340133, 0.20756520159004618, 0.50948425222709, 0.7157847310917926};
         return new double[]{
               Math.abs(latVel/8)*10*w[0], 
               limit(0,advVel/16 + 0.5,1)*2*w[1], 
               limit(0,distance/900,1)*5*w[2], 
               accel/2.0*10*w[3],
               limit(0,distLast10/(8*10),1)*3*w[4],
               limit(0,forwardWall,1)*5*w[5],
               limit(0,reverseWall,1)*2*w[6],
               1/(1 + 2*timeSinceDirChange/BFT) * 3*w[7],
               1/(1 + 2*timeSinceDecel/BFT)*3*w[8]
               };
      
      }
   
   
   }

   public static Point2D.Double project(Point2D.Double sourceLocation, double angle, double length) {
      return new Point2D.Double(sourceLocation.x + FastTrig.sin(angle) * length,
            sourceLocation.y + FastTrig.cos(angle) * length);
   }
   
   public static double limit(double min, double val, double max){
      return Math.max(min,Math.min(val,max));
   }
}