package jk.mega.dGun;

import java.awt.geom.Point2D;
import jk.mega.*;
import java.util.ArrayList;
import robocode.util.Utils;
import jk.math.FastTrig;

public class PreciseMinMaxGFs{

   static double MARGIN = 18;
   static double WIDTH = 800;
   static double HEIGHT = 600;
   public static double[] getPreciseMEAs(
    Point2D.Double enemyLocation, 
    double enemyHeading, 
    double enemyVelocity,
    Point2D.Double myLocation,
    double bulletPower,
    double rotationDirection,
    ArrayList<Point2D.Double> pointsList,
    double GF0){
   
      ArrayList<Point2D.Double> endPoints = getEndPoints(
         enemyLocation,
         enemyHeading,
         enemyVelocity,
         myLocation,
         bulletPower,
         pointsList);
      // pointsList.addAll(endPoints);
      double negAngle = 1;
      double posAngle = -1;

      for(int i = endPoints.size()-1; i >= 0; i--){
         double offset = FastTrig.normalRelativeAngle(absoluteBearing(myLocation,endPoints.get(i))
            - GF0);
         double distance = myLocation.distance(endPoints.get(i));
         double halfWidth = 20/distance;

         if(offset - halfWidth < negAngle)
            negAngle = offset - halfWidth;

         if(offset+halfWidth > posAngle)
            posAngle = offset+halfWidth;      
      }
      if(rotationDirection == 1)
         return new double[]{-negAngle,posAngle};
      return new double[]{posAngle,-negAngle};   
   }
   public static double getPreciseGF0(
    Point2D.Double enemyLocation, 
    double enemyHeading, 
    double enemyVelocity,
    Point2D.Double myLocation,
    double bulletPower,
    double rotationDirection){
      double vel = enemyVelocity;
      Point2D.Double loc = enemyLocation;
      while(Math.abs(vel) > 0.01){
      
         vel = Math.signum(vel)*(Math.abs(vel) - Math.min(Math.abs(vel),2));
         loc = project(loc,enemyHeading,vel);
      
      }
      return absoluteBearing(myLocation,loc);
   }   
   static ArrayList<Point2D.Double> getEndPoints(
    Point2D.Double enemyLocation, 
    double enemyHeading, 
    double enemyVelocity,
    Point2D.Double myLocation,
    double bulletPower,
    ArrayList<Point2D.Double> pointsList
    ){
      ArrayList<Point2D.Double> endPoints = new ArrayList<Point2D.Double>();
      
      Point2D.Double[] elocs = getSmoothedEndPoints(enemyLocation,enemyHeading,enemyVelocity,myLocation,bulletPower, pointsList);
      for(int i = 0; i < elocs.length; i++)
         endPoints.add(elocs[i]);
         
      elocs = getDirectEndPoints(enemyLocation,enemyHeading,enemyVelocity,myLocation,bulletPower, endPoints, pointsList);
      for(int i = 0; i < elocs.length; i++)
         endPoints.add(elocs[i]);
         
      elocs = getStraightEndPoints(enemyLocation,enemyHeading,enemyVelocity,myLocation,bulletPower, pointsList);
      for(int i = 0; i < elocs.length; i++)
         endPoints.add(elocs[i]);
      
      return endPoints;
   }

   static Point2D.Double[] getSmoothedEndPoints(
    Point2D.Double enemyLocation, 
    double enemyHeading, 
    double enemyVelocity,
    Point2D.Double myLocation,
    double bulletPower,
    ArrayList<Point2D.Double> pointsList){
      Point2D.Double[] locs = new Point2D.Double[2];
      double bulletVelocity = 20 - 3*bulletPower;
      for(int i = -1; i < 2; i+=2){
         double angle = enemyHeading + (Math.PI/2)*(i-1);
         double vel = i*enemyVelocity;
         Point2D.Double eloc = (Point2D.Double)enemyLocation.clone();
         double bulletDistance = 0;
         double goalAngle = absoluteBearing(myLocation,eloc) + Math.PI/2;
         if(FastTrig.cos(goalAngle - angle) < 0)
            goalAngle += Math.PI;
         while(bulletDistance < eloc.distance(myLocation)){
            Point2D.Double testPoint = project(eloc,goalAngle,120);
            double testBearing = absoluteBearing(myLocation,testPoint);
            double testDistance = testPoint.distance(myLocation);
            int limit = 20;
            while((MARGIN > testPoint.x || testPoint.x > WIDTH - MARGIN ||
                  MARGIN > testPoint.y || testPoint.y > HEIGHT - MARGIN)
            		&& --limit > 0){
               testDistance *= 0.95;
               testPoint = project(myLocation,testBearing,testDistance);
            }
            double maxTurn = Math.PI/18 - Math.PI/240*Math.abs(vel);
            double smoothAngle = absoluteBearing(eloc,testPoint);
            double wantTurn = FastTrig.normalRelativeAngle(smoothAngle - angle);
            angle = limit(angle - maxTurn, angle + wantTurn, angle + maxTurn);
            if(vel < 0)
               vel += 2;
            else
               vel = Math.min(8,vel+1);
           
            //Point2D.Double nextLoc = project(eloc,angle,vel);
            double dx = FastTrig.sin(angle)*vel;
            double dy = FastTrig.cos(angle)*vel;
            eloc.x += dx;
            eloc.y += dy;
            if(eloc.x > WIDTH - MARGIN || eloc.x < MARGIN
            || eloc.y > HEIGHT - MARGIN || eloc.y < MARGIN){
               eloc.x -= dx;
               eloc.y -= dy;
               break;
            }
            //eloc = nextLoc;
            if(pointsList != null)
               pointsList.add((Point2D.Double)eloc.clone());
            bulletDistance += bulletVelocity;
         }
         locs[(i+1)/2] = eloc;
      }
      return locs;
   }
   static Point2D.Double[] getDirectEndPoints(
    Point2D.Double enemyLocation, 
    double enemyHeading, 
    double enemyVelocity,
    Point2D.Double myLocation,
    double bulletPower,
    ArrayList<Point2D.Double> prevLocs,
    ArrayList<Point2D.Double> pointsList){
      Point2D.Double[] locs = new Point2D.Double[2];
      double bulletVelocity = 20 - 3*bulletPower;
      for(int i = -1; i < 2; i+=2){
         double angle = enemyHeading + (Math.PI/2)*(i-1);
         double vel = i*enemyVelocity;
         Point2D.Double eloc = (Point2D.Double)enemyLocation.clone();
         double bulletDistance = 0;
         double goalAngle = absoluteBearing(eloc,prevLocs.get((i+1)/2));
         //if(FastTrig.cos(goalAngle - angle) < 0)
            //goalAngle += Math.PI;
         while(bulletDistance < eloc.distance(myLocation)){
            Point2D.Double testPoint = project(eloc,goalAngle,90);
            double testBearing = absoluteBearing(myLocation,testPoint);
            double testDistance = testPoint.distance(myLocation);
            int limit = 20;
            while((MARGIN > testPoint.x || testPoint.x > WIDTH - MARGIN ||
                  MARGIN > testPoint.y || testPoint.y > HEIGHT - MARGIN)
            		&& --limit > 0){
               testDistance *= 0.95;
               testPoint = project(myLocation,testBearing,testDistance);
            }
            double maxTurn = Math.PI/18 - Math.PI/240*Math.abs(vel);
            double smoothAngle = absoluteBearing(eloc,testPoint);
            double wantTurn = FastTrig.normalRelativeAngle(smoothAngle - angle);
            angle = limit(angle - maxTurn, angle + wantTurn, angle + maxTurn);
            if(vel < 0)
               vel += 2;
            else
               vel = Math.min(8,vel+1);
           
            double dx = FastTrig.sin(angle)*vel;
            double dy = FastTrig.cos(angle)*vel;
            eloc.x += dx;
            eloc.y += dy;
            if(eloc.x > WIDTH - MARGIN || eloc.x < MARGIN
            || eloc.y > HEIGHT - MARGIN || eloc.y < MARGIN){
               eloc.x -= dx;
               eloc.y -= dy;
               break;
            }
            //eloc = nextLoc;
            if(pointsList != null)
               pointsList.add((Point2D.Double)eloc.clone());
            bulletDistance += bulletVelocity;
         }
         locs[(i+1)/2] = eloc;
      }
      return locs;
   }
   static Point2D.Double[] getStraightEndPoints(
    Point2D.Double enemyLocation, 
    double enemyHeading, 
    double enemyVelocity,
    Point2D.Double myLocation,
    double bulletPower,
    ArrayList<Point2D.Double> pointsList){
      Point2D.Double[] locs = new Point2D.Double[2];
      double bulletVelocity = 20 - 3*bulletPower;
      for(int i = -1; i < 2; i+=2){
         double angle = enemyHeading + (Math.PI/2)*(i-1);
         double vel = i*enemyVelocity;
         Point2D.Double eloc = (Point2D.Double)enemyLocation.clone();
         double bulletDistance = 0;
         double goalAngle = absoluteBearing(myLocation,eloc) + Math.PI/2;
         if(FastTrig.cos(goalAngle - angle) < 0)
            goalAngle += Math.PI;
         while(bulletDistance < eloc.distance(myLocation)){
            double maxTurn = Math.PI/18 - Math.PI/240*Math.abs(vel);
            double wantTurn = FastTrig.normalRelativeAngle(goalAngle - angle);
            angle = limit(angle - maxTurn, angle + wantTurn, angle + maxTurn);
            if(vel < 0)
               vel += 2;
            else
               vel = Math.min(8,vel+1);
           
            double dx = FastTrig.sin(angle)*vel;
            double dy = FastTrig.cos(angle)*vel;
            eloc.x += dx;
            eloc.y += dy;
            if(eloc.x > WIDTH - MARGIN || eloc.x < MARGIN
            || eloc.y > HEIGHT - MARGIN || eloc.y < MARGIN){
               eloc.x -= dx;
               eloc.y -= dy;
               break;
            }
            //eloc = nextLoc;
            if(pointsList != null)
               pointsList.add((Point2D.Double)eloc.clone());
               
            bulletDistance += bulletVelocity;
         }
         locs[(i+1)/2] = eloc;
      }
      return locs;
   }
   static Point2D.Double project(Point2D.Double p, double angle, double distance){
      return new Point2D.Double(p.x + distance*FastTrig.sin(angle), p.y + distance*FastTrig.cos(angle));
   }
   public static double absoluteBearing(Point2D.Double source, Point2D.Double target) {
      return FastTrig.atan2(target.x - source.x, target.y - source.y);
   }
   public static double limit(double min, double value, double max) {
      if(value > max)
         return max;
      if(value < min)
         return min;
      
      return value;
   }
}