package quartetofantastico;

import robocode.AdvancedRobot;
import robocode.Condition;
import robocode.util.Utils;

import java.awt.geom.Point2D;

public class GFTWave extends Condition {
    static Point2D targetLocation;

    double bulletPower;
    Point2D gunLocation;
    double bearing;
    double lateralDirection;

    private static final double MAX_DISTANCE = 900;
    private static final int DISTANCE_INDEXES = 5;
    private static final int VELOCITY_INDEXES = 5;
    private static final int BINS = 25;
    private static final int MIDDLE_BIN = (BINS - 1) / 2;
    private static final double MAX_ESCAPE_ANGLE = 0.7;
    private static final double BIN_WIDTH = MAX_ESCAPE_ANGLE / (double)MIDDLE_BIN;

    private static int[][][][] statBuffers = new int[DISTANCE_INDEXES][VELOCITY_INDEXES][VELOCITY_INDEXES][BINS];

    private int[] buffer;
    private AdvancedRobot robot;
    private double distanceTraveled;

    GFTWave(AdvancedRobot _robot) {
        this.robot = _robot;
    }

    public boolean test() {
        advance();
        if (hasArrived()) {
            buffer[currentBin()]++;
            robot.removeCustomEvent(this);
        }
        return false;
    }

    double mostVisitedBearingOffset() {
        return (lateralDirection * BIN_WIDTH) * (mostVisitedBin() - MIDDLE_BIN);
    }

    void setSegmentations(double distance, double velocity, double lastVelocity) {
        int distanceIndex = Math.min(DISTANCE_INDEXES-1, (int)(distance / (MAX_DISTANCE / DISTANCE_INDEXES)));
        int velocityIndex = (int)Math.abs(velocity / 2);
        int lastVelocityIndex = (int)Math.abs(lastVelocity / 2);
        buffer = statBuffers[distanceIndex][velocityIndex][lastVelocityIndex];
    }

    private void advance() {
        distanceTraveled += UtilitariosGFT.velocidadeDoProjetil(bulletPower);
    }

    private boolean hasArrived() {
        return distanceTraveled > gunLocation.distance(targetLocation) - 18;
    }

    private int currentBin() {
        int bin = (int)Math.round(((Utils.normalRelativeAngle(UtilitariosGFT.anguloAbsoluto(gunLocation, targetLocation) - bearing)) /
                (lateralDirection * BIN_WIDTH)) + MIDDLE_BIN);
        return UtilitariosGFT.minMax(bin, 0, BINS - 1);
    }

    private int mostVisitedBin() {
        int mostVisited = MIDDLE_BIN;
        for (int i = 0; i < BINS; i++) {
            if (buffer[i] > buffer[mostVisited]) {
                mostVisited = i;
            }
        }
        return mostVisited;
    }
}
