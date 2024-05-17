package jk.mega.dMove;
import java.awt.geom.*;
import jk.precise.util.*;
import jk.math.FastTrig;

public class MovePredictor{

   Point2D.Double fromLocation;
   Point2D.Double toLocation;
   double initialVelocity, initialHeading;
   long currentTime;
   EnemyWave wave;
   
   double bearing,velocity,distanceRemaining, heading ;
   int time;
      
   Point2D.Double endPoint;  
   int counter;
   double sinVal , cosVal;
   boolean inline;
   
   public MovePredictor(){}

   public MovePredictor(
   Point2D.Double fromLocation, 
   Point2D.Double toLocation, 
   double initialVelocity, 
   double initialHeading, 
   long currentTime, 
   EnemyWave wave){
   
      recycle(fromLocation,toLocation,initialVelocity,initialHeading,currentTime,wave);
   }
   
   public void recycle       (
   Point2D.Double fromLocation, 
   Point2D.Double toLocation, 
   double initialVelocity, 
   double initialHeading, 
   long currentTime, 
   EnemyWave wave){
   
      this.fromLocation = fromLocation;
      this.toLocation = toLocation;
      this.initialVelocity = initialVelocity;
      this.initialHeading = initialHeading;
      this.currentTime = currentTime;
      this.wave = wave;
   
   
      bearing = absoluteBearing(fromLocation,toLocation);
      velocity = initialVelocity;
      distanceRemaining = fromLocation.distance(toLocation);;
      time = (int)(currentTime - wave.fireTime);
      heading = initialHeading;
      
      endPoint = (Point2D.Double)fromLocation.clone();  
      counter = 5 + (int)Math.ceil(endPoint.distance(wave.fireLocation)/(wave.bulletVelocity-8)) - time;
      sinVal = 0;
      cosVal = 0;
      inline = false;
   }
   
   
   public PredictionStatus predictToIntersection(){
      
      while(notIntersectedEarlyExit()){
         predict();
      } 
      
      if(counter == 0)
         System.out.println("PREVENTED PREDICTION FREEZE!!");
         
      time = (int)(endPoint.distance(wave.fireLocation)/wave.bulletVelocity) + (int)wave.fireTime;
   
      return new PredictionStatus(endPoint,velocity,FastTrig.normalAbsoluteAngle(heading),time,distanceRemaining);
   }
   public PredictionStatus predictToPreciseIntersection(){
      
   
      while(notQuiteIntersected()){
         predict();
      } 
      double[] intersectionIndices = {(double)EnemyWave.BINS,0.0};
      
      PreciseWave pw = new PreciseWave();
      pw.fireLocation = wave.fireLocation;
      pw.bulletVelocity = wave.bulletVelocity;
      
      while(notIntersectedEarlyExit()){
         predict();
         updatePreciseRange(pw,intersectionIndices);
      }
      PredictionStatus hitStatus = 
         new PredictionStatus(endPoint,velocity,FastTrig.normalAbsoluteAngle(heading),
                                      time,distanceRemaining,intersectionIndices);
      
      do{
         predict();
      }while(--counter != 0 && updatePreciseRange(pw,intersectionIndices) != PreciseUtils.PASSED);
      
      if(counter == 0)
         System.out.println("PREVENTED PREDICTION FREEZE!!");
      time = (int)(endPoint.distance(wave.fireLocation)/wave.bulletVelocity) + (int)wave.fireTime;
   
      hitStatus.time = time;
      return hitStatus;
   }
   int updatePreciseRange(PreciseWave pw, double[] intersectionIndices){
      pw.distanceTraveled = wave.bulletVelocity*time;
      int code = PreciseUtils.intersects(endPoint,pw);
      if(code == PreciseUtils.INTERSECTION){
         double[] angleRange = PreciseUtils.getIntersectionRange(endPoint,pw);
         double[] indexes = {wave.getFactorIndex(angleRange[0]),wave.getFactorIndex(angleRange[1])};
         if(indexes[1] < indexes[0]){
            double temp = indexes[0];
            indexes[0] = indexes[1];
            indexes[1] = temp;
         }
         intersectionIndices[0] = Math.min(intersectionIndices[0],indexes[0]);
         intersectionIndices[1] = Math.max(intersectionIndices[1],indexes[1]);
      }
      return code;
   }
   
   
   
   void predict(){
      time++;
      if(!inline & (distanceRemaining > 1 | Math.abs(velocity) > 0.1))
      {
         double maxTurn = Math.PI/18 - Math.PI/240*Math.abs(velocity);
         bearing = absoluteBearing(endPoint,toLocation);
         double offset = FastTrig.normalRelativeAngle(bearing - heading);
         if(-Math.PI/2 > offset | offset > Math.PI/2){
            offset = FastTrig.normalRelativeAngle(offset + Math.PI);
            velocity = -velocity;
            heading += Math.PI;
         }
         offset = limit(-maxTurn,offset,maxTurn);
         heading += offset;
         sinVal = FastTrig.sin(heading);
         cosVal = FastTrig.cos(heading);
         if(-0.0001 < offset & offset < 0.0001)
            inline = true;
      }
         
      velocity = getNewVelocity(velocity, distanceRemaining);
         		
      endPoint.x += sinVal*velocity;
      endPoint.y += cosVal*velocity;
         
         /*if(endPoint.x < W
         | endPoint.x > E
         | endPoint.y < S
         | endPoint.y > N){
         
            velocity = 0;
         
            endPoint.x -= sinVal*velocity;
            endPoint.y -= cosVal*velocity;
         
         }*/
          
      if(velocity > distanceRemaining)
         inline = false;
      if(inline)
         distanceRemaining = Math.abs(distanceRemaining - velocity);
      else
         distanceRemaining = endPoint.distance(toLocation);
   }
   boolean notIntersectedEarlyExit(){
      return endPoint.distanceSq(wave.fireLocation) > sqr(wave.bulletVelocity*(time+1))
         & (Math.abs(distanceRemaining) > 0.1 || Math.abs(velocity) > 0.1)
         & --counter != 0;
   }
   boolean notQuiteIntersected(){
      return endPoint.distanceSq(wave.fireLocation) > sqr(wave.bulletVelocity*(time+1) + 25.4 + 8)
         & --counter != 0;
   }

            	//3 optimized methods from the new robocode engine
   private static double getNewVelocity(double velocity, double distance) {
      if(distance < 0)
         return -getNewVelocity(-velocity,-distance);
         
      final double goalVel = Math.min(getMaxVelocity(distance), 8);
   
      if(velocity >= 0)
         return limit(velocity - 2,
            goalVel, velocity + 1);
     
      return limit(velocity - 1,
         goalVel, velocity + maxDecel(-velocity));
   }

   static final double getMaxVelocity(double distance) {
      final double decelTime =  Math.round(
         //sum of 0... decelTime, solving for decelTime 
         //using quadratic formula, then simplified a lot
         Math.sqrt(distance + 1));
   
      final double decelDist = Math.max(0, (decelTime) * (decelTime-1) );
         // sum of 0..(decelTime-1)
         // * Rules.DECELERATION*0.5;
   
      return Math.max(0,((decelTime - 1) * 2) + ((distance - decelDist) / decelTime));
   }
 
   private static final double maxDecel(double speed) {
      return limit(1,speed*0.5 + 1, 2);
   }



   private static double limit(double  min, double val, double max){
      if(val < min)
         return min;
      if (val > max)
         return max;
      return val;
   }
   private static double absoluteBearing(Point2D source, Point2D target) {
      return FastTrig.atan2(target.getX() - source.getX(), target.getY() - source.getY());
   }
   private static double sqr(double d){
      return d*d;}

}