package jk.mega;

import robocode.*;
import jk.mega.dGun.*;
import jk.mega.dMove.*;
import jk.melee.*;
import jk.precise.*;
import java.io.PrintStream;
import java.io.IOException;
import java.awt.*;

public class DrussGT extends AdvancedRobot {

    public static final boolean TC = false;
    public static final boolean MC = false;
    public static final double SHIELD_THRESHOLD = 98;
    //train it in the movement, available for the gun to use too
    public static BulletPowerPredictor bulletPowerPredictor = new BulletPowerPredictor();
    static boolean shieldEnabled = !MC && !TC;
    public DrussMoveGT move;
    public MeleeRadar radar;
    public DrussGunDC gun;
    public EnergyDomeWorker shield;

    public void run() {

        setAdjustRadarForGunTurn(true);
        setAdjustRadarForRobotTurn(true);
        setAdjustGunForRobotTurn(true);
        setColors(new Color(40, 40, 0), new Color(0, 40, 0), Color.GRAY);
        if (!TC) {
            setTurnRightRadians(Double.POSITIVE_INFINITY);
            setTurnGunRightRadians(Double.POSITIVE_INFINITY);
        }
        setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
        if (!TC)
            try {
                move = new DrussMoveGT(this);
            } catch (Exception ex) {
                contain(ex);
            }
        try {
            radar = new MeleeRadar(this);
        } catch (Exception ex) {
            contain(ex);
        }
        if (!MC)
            try {
                gun = new DrussGunDC(this);
            } catch (Exception ex) {
                contain(ex);
            }

        if (shieldEnabled)
            try {
                shield = new EnergyDomeWorker(this);
            } catch (Exception ex) {
                contain(ex);
            }

        while (true) {
            if (!TC)
                try {
                    move.onTick(!shieldEnabled);
                } catch (Exception ex) {
                    contain(ex);
                }
            try {
                radar.onTick();
            } catch (Exception ex) {
                contain(ex);
            }
            if (!MC)
                try {
                } catch (Exception ex) {
                    contain(ex);
                }
            try {
                Thread.sleep(0,1000);
            } catch (Exception e){}
            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        if (!TC && !MC && shieldEnabled) {
            shieldEnabled = shield.aboveScore(SHIELD_THRESHOLD);
            if (shieldEnabled) {
                try {
                    shield.onScannedRobot(e);
                    shield.applyActions();
                } catch (Exception ex) {
                    contain(ex);
                }
            }
        }


        DataToGun dtg = new DataToGun();
        if (!TC)
            try {
                move.onScannedRobot(e);
                if (!shieldEnabled)
                    move.doSurfing();
                dtg = move.getGunInfo();
            } catch (Exception ex) {
                contain(ex);
            }

        try {
            radar.onScannedRobot(e);
        } catch (Exception ex) {
            contain(ex);
        }


        if (!shieldEnabled || TC) {
            Bullet b = null;
            if (!MC)
                try {
                    b = gun.onScannedRobot(e, dtg.futurePoint, dtg.futureHeading, dtg.futureVelocity, dtg.timeInFuture, dtg.enemyBP, TC);
                } catch (Exception ex) {
                    contain(ex);
                }

            if (!TC)
                try {
                    move.logMyBullet(b);
                } catch (Exception ex) {
                    contain(ex);
                }
        }
    }

    public void onRobotDeath(RobotDeathEvent e) {
        if (!TC)
            try {
                move.onRobotDeath(e);
            } catch (Exception ex) {
                contain(ex);
            }

        try {
            radar.onRobotDeath(e);
        } catch (Exception ex) {
            contain(ex);
        }

        // try{
        // gun.onRobotDeath(e);
        // }
        // catch(Exception ex){contain(ex);}

    }

    public void onHitByBullet(HitByBulletEvent e) {
        if (!TC)
            try {
                move.onHitByBullet(e);
            } catch (Exception ex) {
                contain(ex);
            }

        if (shieldEnabled)
            try {
                shield.onHitByBullet(e);
            } catch (Exception ex) {
                contain(ex);
            }
    }

    public void onHitRobot(HitRobotEvent e) {
        if (!TC)
            try {
                move.onHitRobot(e);
            } catch (Exception ex) {
                contain(ex);
            }
    }

    public void onBulletHitBullet(BulletHitBulletEvent e) {
        if (!TC)
            try {
                move.onBulletHitBullet(e);
            } catch (Exception ex) {
                contain(ex);
            }


        if (!shieldEnabled)
            try {
                gun.onBulletHitBullet(e);
            } catch (Exception ex) {
                contain(ex);
            }

        if (shieldEnabled)
            try {
                shield.onBulletHitBullet(e);
            } catch (Exception ex) {
                contain(ex);
            }
    }

    public void onBulletHit(BulletHitEvent e) {
        if (!TC)
            try {
                move.onBulletHit(e);
            } catch (Exception ex) {
                contain(ex);
            }

        if (!shieldEnabled)
            try {
                gun.onBulletHit(e);
            } catch (Exception ex) {
                contain(ex);
            }

        if (shieldEnabled)
            try {
                shield.onBulletHit(e);
            } catch (Exception ex) {
                contain(ex);
            }
    }

    public void onPaint(java.awt.Graphics2D g) {
        if (!TC) move.onPaint(g);
        if (!shieldEnabled)
            gun.onPaint(g);
    }

    public void onDeath(DeathEvent e) {
        shieldEnabled = false;
        try {

            if (!MC) gun.onDeath(e);
            if (!TC) move.onDeath(e);
        } catch (Exception ex) {
            contain(ex);
        }
    }

    public void onWin(WinEvent e) {
        try {
            if (!TC) move.onWin(e);
            if (!MC) gun.onWin(e);
        } catch (Exception ex) {
            contain(ex);
        }
    }

    public void contain(Exception e) {

        e.printStackTrace();

        try {
            PrintStream out = new PrintStream(new RobocodeFileOutputStream(getDataFile((int) (Math.random() * 100) + ".error")));
            e.printStackTrace(out);
            out.flush();
            out.close();
        } catch (IOException ioex) {
        }
    }


}