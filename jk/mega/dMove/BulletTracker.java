package jk.mega.dMove;
import java.awt.geom.*;
import java.util.*;
import robocode.*;


public class BulletTracker{
   Bullet b;
   Point2D.Double startLocation;
   long fireTime;
   ArrayList<EnemyWave> crossedWaves = new ArrayList<EnemyWave>();
   boolean flattenerLogged = false;
}