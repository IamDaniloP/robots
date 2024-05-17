package jk.mega.dMove;
import java.awt.geom.*;

public  class PredictionStatus{  
   public PredictionStatus(){}
   public PredictionStatus(Point2D.Double endPoint,double velocity,double heading,long time,double distanceRemaining){
      this.endPoint = endPoint;
      this.finalVelocity = velocity;
      this.finalHeading = heading;
      this.time = time;
      this.distanceRemaining = distanceRemaining;
   }
   public PredictionStatus(Point2D.Double endPoint,double velocity,double heading,long time,double distanceRemaining,double[] indices){
      this.endPoint = endPoint;
      this.finalVelocity = velocity;
      this.finalHeading = heading;
      this.time = time;
      this.distanceRemaining = distanceRemaining;
      this.intersectionIndices = indices;
   }
   double  finalHeading, finalVelocity, distanceRemaining;
   long time;
   Point2D.Double endPoint;
   double[] intersectionIndices;
   EnemyWave fwave;
   // boolean debug;
}
