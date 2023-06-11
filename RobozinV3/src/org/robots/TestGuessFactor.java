package org.robots;




import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.robots.helpers.WaveBulletExt;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

/**
 *<p>
 * This is my implementation of the GuessFactor Targeting tutorial
 * by Kawigi. Actually, I copied (typed) the code from the wiki, so
 * I understand better what is going on exactly.</p>
 * <p>
 * For more info, see:
 *      http://robowiki.net/wiki/GuessFactor_Targeting_Tutorial
 *</p>
 * <br>
 * This robot uses Turn Multiplier Lock in order to track the enemy.
 *
 */
public class TestGuessFactor extends AdvancedRobot {


    static final double PI = Math.PI;

    // A list to keep track of the waves
    List<WaveBulletExt> waves = new ArrayList<>();

    // this holds the GuessFactors...
    // Note: this must be odd, so we can get GuessFactor 0 in the middle
    static int segmentPixelSize = 60;
    static int segmentDimension = 1200 / segmentPixelSize + 1;
    static int[][] stats = new int[segmentDimension][31];


    int direction = 1;

    Point2D centerTarget;

    @Override
	public void run() {

        setAdjustRadarForGunTurn(true);
        setAdjustGunForRobotTurn(true);

        setColors(
                Color.RED.darker(), // body
                Color.ORANGE, // gun
                Color.WHITE); // radar

        setBulletColor(Color.RED.darker());
        setRadarColor(Color.WHITE);

        setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
        execute();

        moveTowardsCenter();

        while (true) {
            //check for new targets.
            // Only necessary for Narrow Lock because sometimes our radar is already
            // pointed at the enemy and our onScannedRobot code doesn't end up telling
            // it to turn, so the system doesn't automatically call scan() for  us
            // [see the javadocs for scan()].
            // scan();
            moveTowardsCenter();
            execute();
        }
    }


    @Override
    public void onScannedRobot(ScannedRobotEvent e) {


        // Calculate the radar adjustment.
        double absBearing = getHeadingRadians() + e.getBearingRadians();

        adjustRadar(e, absBearing);

        // GuessFactor calculations

        // find out enemy's location
        double ex = getX() + Math.sin(absBearing) * e.getDistance();
        double ey = getY() + Math.cos(absBearing) * e.getDistance();

        // Process the current waves
        for (int i = 0; i < waves.size(); i++) {

            WaveBulletExt wave = waves.get(i);

            if (wave.checkHit(ex, ey, getTime())){
                waves.remove(wave);
                i--;
            }
        }

        // Determine the power of the bullet.
        // This should probably be more sophisticated...
        double power = 1.72;

        // figure out the direction the enemy is moving in.
        // if it is not moving, use the direction we had before...
        if (e.getVelocity() != 0) {
            if (Math.sin(e.getHeadingRadians() - absBearing) * e.getVelocity() < 0)
                direction = -1;
            else
                direction = 1;
        }

        int[] currentStats = stats[(int) (e.getDistance() / 100)];

        WaveBulletExt newWave = new WaveBulletExt(getX(), getY(), absBearing, power,
                direction, getTime(), currentStats);

        int bestIndex = 15; // initialize it to be in the middle, GuessFactor 0

        for (int i = 0; i < 31; i++) {
            if (currentStats[bestIndex] < currentStats[i]) {
                bestIndex = i;
            }
        }

        // calculate the GuessFactor. This is the index of the stat with the
        // highest score, normalized in the range [-1,1]
        double guessFactor = (double) (bestIndex - (currentStats.length - 1) / 2) /
                ((currentStats.length - 1) / 2);

        double angleOffset = direction * guessFactor * newWave.maxEscapeAngle();
        double gunAdjust = Utils.normalRelativeAngle(
                absBearing - getGunHeadingRadians() + angleOffset);
        setTurnGunRightRadians(gunAdjust);

        // When we fire, add the wave to the list
        // but we have to wait for the gun to be ready to fire
        if ((getGunHeat() == 0
                && gunAdjust < Math.atan2(9, e.getDistance())
                && setFireBullet(power) != null)) {
            waves.add(newWave);
        }
    }

    private void adjustRadar(ScannedRobotEvent e, double absBearing) {

        // Absolute bearing to target - current radar heading
        double radarTurn = absBearing - getRadarHeadingRadians();

        double factor = 2.0;
        setTurnRadarRightRadians(factor * Utils.normalRelativeAngle(radarTurn));

    }

    private void moveTowardsCenter() {


        if (centerTarget == null)
            centerTarget = new Point2D.Double(getBattleFieldWidth() / 2, getBattleFieldHeight() / 2);


        System.out.println(getBearingToPoint(centerTarget));
        double robotAdjustment = getBearingToPoint(centerTarget);

        setTurnRightRadians(robotAdjustment);
        setAhead(calculateDistance(centerTarget.getX(),centerTarget.getY()));

    }

    private double getBearingToPoint(Point2D target) {

        return Math.atan2(getY() - target.getY(), getX() - target.getX()) - getHeadingRadians();
    }

    private double calculateDistance(double x, double y) {

        return new Point2D.Double(getX(), getY()).distance(x,y);
    }

    @Override
    public void onPaint(Graphics2D g) {

        g.setColor(Color.green);

        for (WaveBulletExt wave : waves) {

            double timePassed = getTime() - wave.getFireTime();
            double width = 2 * wave.getBulletSpeed() * (timePassed);
            double height = width;

            g.drawOval(
                    (int) (wave.getStartX() - (width / 2)),
                    (int) (wave.getStartY() - (width / 2)),
                    (int) width,
                    (int) height);

        }

        if (centerTarget != null) {
            g.setColor(Color.GREEN);
            int x1 = (int) getX();
            int y1 = (int) getY();
            int x2 = (int) centerTarget.getX();
            int y2 = (int) centerTarget.getY();

            g.drawLine(x1,y1,x2,y2);
        }

    }
}