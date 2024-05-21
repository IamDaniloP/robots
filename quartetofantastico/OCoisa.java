package quartetofantastico;

import robocode.*;
import robocode.util.Utils;
import java.awt.geom.*;
import java.lang.*;
import java.util.ArrayList;
import java.awt.Color;

// O Coisa - a class by (Quarteto Fantastico)

public class OCoisa extends AdvancedRobot {
    public static int BINS = 47;
    public static double[] _surfStats = new double[BINS];
    public Point2D.Double _myLocation;
    public Point2D.Double _enemyLocation;

    public ArrayList _enemyWaves;
    public ArrayList _surfDirections;
    public ArrayList _surfAbsBearings;
	private static final double BULLET_POWER = 1.9;
	
	private static double lateralDirection;
	private static double lastEnemyVelocity;

    public static double _oppEnergy = 100.0;

    public static Rectangle2D.Double battlefield;

    public void run() {
        //setColor
        setColors(Color.orange, Color.white, Color.orange, Color.black, Color.orange);

        battlefield = new java.awt.geom.Rectangle2D.Double(18, 18, getBattleFieldWidth() - 36, getBattleFieldHeight() - 36);

        lateralDirection = 1;
		lastEnemyVelocity = 0;
		
        _enemyWaves = new ArrayList();
        _surfDirections = new ArrayList();
        _surfAbsBearings = new ArrayList();

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);


        do {
            turnRadarRightRadians(Double.POSITIVE_INFINITY);
        } while (true);
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        _myLocation = new Point2D.Double(getX(), getY());

        double lateralVelocity = getVelocity()*Math.sin(e.getBearingRadians());
        double absBearing = e.getBearingRadians() + getHeadingRadians();

        setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians()) * 2);

        _surfDirections.add(0, (lateralVelocity >= 0) ? 1 : -1);
        _surfAbsBearings.add(0, absBearing + Math.PI);


        double bulletPower = _oppEnergy - e.getEnergy();
        if (bulletPower < 3.01 && bulletPower > 0.09
            && _surfDirections.size() > 2) {
            OndaDoOponente ondaDoOponente = new OndaDoOponente();
            ondaDoOponente.tempoDisparo = getTime() - 1;
            ondaDoOponente.velocidadeProjetil = bulletVelocity(bulletPower);
            ondaDoOponente.distanciaPercorrida = bulletVelocity(bulletPower);
            ondaDoOponente.direcao = ((Integer)_surfDirections.get(2));
            ondaDoOponente.anguloDireto = ((Double)_surfAbsBearings.get(2));
            ondaDoOponente.localDisparo = (Point2D.Double)_enemyLocation.clone(); // last tick

            _enemyWaves.add(ondaDoOponente);
        }

        _oppEnergy = e.getEnergy();

        _enemyLocation = project(_myLocation, absBearing, e.getDistance());

        updateWaves();
        doSurfing();

		double enemyAbsoluteBearing = getHeadingRadians() + e.getBearingRadians();
		double enemyDistance = e.getDistance();
		double enemyVelocity = e.getVelocity();
		if (enemyVelocity != 0) {
			lateralDirection = UtilitariosGFT.sinal(enemyVelocity * Math.sin(e.getHeadingRadians() - enemyAbsoluteBearing));
		}
		GFTWave wave = new GFTWave(this);
		wave.gunLocation = new Point2D.Double(getX(), getY());
		GFTWave.targetLocation = UtilitariosGFT.projetarMov(wave.gunLocation, enemyAbsoluteBearing, enemyDistance);
		wave.lateralDirection = lateralDirection;
		wave.bulletPower = BULLET_POWER;
		wave.setSegmentations(enemyDistance, enemyVelocity, lastEnemyVelocity);
		lastEnemyVelocity = enemyVelocity;
		wave.bearing = enemyAbsoluteBearing;
		setTurnGunRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing - getGunHeadingRadians() + wave.mostVisitedBearingOffset()));
		setFire(wave.bulletPower);
		if (getEnergy() >= BULLET_POWER) {
			addCustomEvent(wave);
		}
		setTurnRadarRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing - getRadarHeadingRadians()) * 2);


        //testando impl regressao logistica
        double distance = e.getDistance();
        double velocity = getVelocity();

        if (LogisticRegression.getCoefficient(distance, velocity) >= 0.5) {
            System.out.println("Distância: " + distance);
            System.out.println(LogisticRegression.getCoefficient(distance, velocity));
            System.out.println("Perdeu");

            double gunToHeading = Utils.normalRelativeAngle(getGunHeadingRadians() - getHeadingRadians());
            setTurnRightRadians(gunToHeading);
            setAhead(500);
        }
    }

    public void updateWaves() {
        for (int x = 0; x < _enemyWaves.size(); x++) {
            OndaDoOponente ondaDoOponente = (OndaDoOponente)_enemyWaves.get(x);

            ondaDoOponente.distanciaPercorrida = (getTime() - ondaDoOponente.tempoDisparo) * ondaDoOponente.velocidadeProjetil;
            if (ondaDoOponente.distanciaPercorrida >
                _myLocation.distance(ondaDoOponente.localDisparo) + 50) {
                _enemyWaves.remove(x);
                x--;
            }
        }
    }

    public OndaDoOponente getClosestSurfableWave() {
        double closestDistance = 10000;
        OndaDoOponente surfWave = null;

        for (Object enemyWave : _enemyWaves) {
            OndaDoOponente ondaDoOponente = (OndaDoOponente) enemyWave;
            double distance = _myLocation.distance(ondaDoOponente.localDisparo)
                    - ondaDoOponente.distanciaPercorrida;

            if (distance > ondaDoOponente.velocidadeProjetil && distance < closestDistance) {
                surfWave = ondaDoOponente;
                closestDistance = distance;
            }
        }

        return surfWave;
    }

    public static int getFactorIndex(OndaDoOponente ondaDoOponente, Point2D.Double targetLocation) {
        double offsetAngle = (absoluteBearing(ondaDoOponente.localDisparo, targetLocation)
            - ondaDoOponente.anguloDireto);
        double factor = Utils.normalRelativeAngle(offsetAngle)
            / maxEscapeAngle(ondaDoOponente.velocidadeProjetil) * ondaDoOponente.direcao;

        return (int)limit(0,
            (factor * ((double) (BINS - 1) / 2)) + ((double) (BINS - 1) / 2),
            BINS - 1);
    }

    public void logHit(OndaDoOponente ondaDoOponente, Point2D.Double targetLocation) {
        int index = getFactorIndex(ondaDoOponente, targetLocation);

        for (int x = 0; x < BINS; x++) {
            _surfStats[x] += 1.0 / (Math.pow(index - x, 2) + 1);
        }
    }

    public void onHitByBullet(HitByBulletEvent e) {
        if (!_enemyWaves.isEmpty()) {
            Point2D.Double hitBulletLocation = new Point2D.Double(
                e.getBullet().getX(), e.getBullet().getY());
            OndaDoOponente hitWave = null;

            for (Object enemyWave : _enemyWaves) {
                OndaDoOponente ondaDoOponente = (OndaDoOponente) enemyWave;

                if (Math.abs(ondaDoOponente.distanciaPercorrida -
                        _myLocation.distance(ondaDoOponente.localDisparo)) < 50
                        && Math.abs(bulletVelocity(e.getBullet().getPower())
                        - ondaDoOponente.velocidadeProjetil) < 0.001) {
                    hitWave = ondaDoOponente;
                    break;
                }
            }

            if (hitWave != null) {
                logHit(hitWave, hitBulletLocation);

                _enemyWaves.remove(_enemyWaves.lastIndexOf(hitWave));
            }
        }
    }

    public Point2D.Double predictPosition(OndaDoOponente surfWave, int direction) {
    	Point2D.Double predictedPosition = (Point2D.Double)_myLocation.clone();
    	double predictedVelocity = getVelocity();
    	double predictedHeading = getHeadingRadians();
    	double maxTurning, moveAngle, moveDir;

        int counter = 0;
        boolean intercepted = false;

    	do {
    		moveAngle =
                wallSmoothing(predictedPosition, absoluteBearing(surfWave.localDisparo,
                predictedPosition) + (direction * (Math.PI/2)), direction)
                - predictedHeading;
    		moveDir = 1;

    		if(Math.cos(moveAngle) < 0) {
    			moveAngle += Math.PI;
    			moveDir = -1;
    		}

    		moveAngle = Utils.normalRelativeAngle(moveAngle);

    		maxTurning = Math.PI/720d*(40d - 3d*Math.abs(predictedVelocity));
    		predictedHeading = Utils.normalRelativeAngle(predictedHeading
                + limit(-maxTurning, moveAngle, maxTurning));

    		predictedVelocity += (predictedVelocity * moveDir < 0 ? 2*moveDir : moveDir);
    		predictedVelocity = limit(-8, predictedVelocity, 8);

    		predictedPosition = project(predictedPosition, predictedHeading, predictedVelocity);

            counter++;

            if (predictedPosition.distance(surfWave.localDisparo) <
                surfWave.distanciaPercorrida + (counter * surfWave.velocidadeProjetil)
                + surfWave.velocidadeProjetil) {
                intercepted = true;
            }
    	} while(!intercepted && counter < 500);

    	return predictedPosition;
    }

    public double checkDanger(OndaDoOponente surfWave, int direction) {
        int index = getFactorIndex(surfWave,
            predictPosition(surfWave, direction));

        return _surfStats[index];
    }

    public void doSurfing() {
        OndaDoOponente surfWave = getClosestSurfableWave();

        if (surfWave == null) { return; }

        double dangerLeft = checkDanger(surfWave, -1);
        double dangerRight = checkDanger(surfWave, 1);

        double goAngle = absoluteBearing(surfWave.localDisparo, _myLocation);
        if (dangerLeft < dangerRight) {
            goAngle = wallSmoothing(_myLocation, goAngle - (Math.PI/2), -1);
        } else {
            goAngle = wallSmoothing(_myLocation, goAngle + (Math.PI/2), 1);
        }

        setBackAsFront(this, goAngle);
    }

    public double wallSmoothing(Point2D.Double botLocation, double angle, int orientation) {
        while (!battlefield.contains(project(botLocation, angle, 160))) {
            angle += orientation*0.05;
        }
        return angle;
    }

    public static Point2D.Double project(Point2D.Double sourceLocation, double angle, double length) {
        return new Point2D.Double(sourceLocation.x + Math.sin(angle) * length,
            sourceLocation.y + Math.cos(angle) * length);
    }

    public static double absoluteBearing(Point2D.Double source, Point2D.Double target) {
        return Math.atan2(target.x - source.x, target.y - source.y);
    }

    public static double limit(double min, double value, double max) {
        return Math.max(min, Math.min(value, max));
    }

    public static double bulletVelocity(double power) {
        return (20D - (3D*power));
    }

    public static double maxEscapeAngle(double velocity) {
        return Math.asin(8.0/velocity);
    }

    public static void setBackAsFront(AdvancedRobot robot, double goAngle) {
        double angle =
            Utils.normalRelativeAngle(goAngle - robot.getHeadingRadians());
        if (Math.abs(angle) > (Math.PI/2)) {
            if (angle < 0) {
                robot.setTurnRightRadians(Math.PI + angle);
            } else {
                robot.setTurnLeftRadians(Math.PI - angle);
            }
            robot.setBack(100);
        } else {
            if (angle < 0) {
                robot.setTurnLeftRadians(-1*angle);
           } else {
                robot.setTurnRightRadians(angle);
           }
            robot.setAhead(100);
        }
    }
}
