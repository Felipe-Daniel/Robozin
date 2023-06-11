package org.robots.helpers;

import java.awt.geom.Point2D;

import robocode.util.Utils;


public class WaveBulletExt {
    private double startX, startY, startBearing, power;
    private long fireTime;
    private int direction;
    //TODO: implement more segments, in order to increase accuracy
    private int[] returnSegment;

    public WaveBulletExt(double x, double y, double bearing,
                      double power, int direction, long time, int[] segment) {
        startX = x;
        startY = y;
        startBearing = bearing;
        this.power = power;
        this.direction = direction;
        fireTime = time;
        returnSegment = segment;

    }


    /**
     * Calculates the speed of a bullet, given it's power.
     * @return The speed of the bullet.
     */
    public double getBulletSpeed() {
        return 20 - power * 3;
    }

    /**
     * Calculates the maximum escape angle of the target.
     *<p>
     * The maximum escape angle is determined by the speed of
     * the bullet. In worst case, it is asin( 8 / speed of bullet).
     *<br>
     * For more info, see: <br>
     *      <a href=http://robowiki.net/wiki/Maximum_Escape_Angle>
     *          http://robowiki.net/wiki/Maximum_Escape_Angle</a>
     *<br>
     *
     *
     * @return The maximum escape angle
     */

    //TODO: implement: learning the escape angle by use of reinforcement learning (or nearest neighbour)
    public double maxEscapeAngle() {
        return Math.asin( 8 / getBulletSpeed() );
    }


    /**
     * Calculates the GuessFactor for the bullet and stores the information
     * in the returnsegment.
     * @param enemyX The current x-coordinate of the enemy
     * @param enemyY The current y-coordinate of the enemy
     * @param currentTime The current time
     * @return true if the wave has hit the enemy.
     */
    public boolean checkHit(double enemyX, double enemyY, long currentTime) {

        // The wave is the circle for which the radius is the distance that the
        // bullet has traveled. So, if this distance is greater than the distance from the
        // starting point to the current position of the enemy, the wave has hit the
        // enemy.
        if (Point2D.distance(startX, startY, enemyX, enemyY) <=
                (currentTime - fireTime) * getBulletSpeed()) {

            // the desired direction is the direction we should have shot at
            // in order to hit the enemy.
            double desiredDirection = Math.atan2(enemyX - startX, enemyY - startY);

            // the angle-offset is the difference between the heading that we shot at
            // and the heading we should have shot at. But then normalized in [-pi,pi)
            double angleOffset = Utils.normalRelativeAngle(desiredDirection - startBearing);

            // A GuessFactor of 1.0 represents the bearing offset that the enemy would reach
            // if it maximized its escape angle while moving in its current direction relative
            // to the firing bot.
            // A GuessFactor of -1.0 represents the bearing offset if it maximized its
            // escape angle after reversing its direction.
            // A GuessFactor of 0.0 represents the bearing offset that points directly
            // at the enemy.
            // see: http://robowiki.net/wiki/GuessFactor
            double guessFactor = Math.max(
                    -1,Math.min(
                            1, angleOffset / maxEscapeAngle()
                        )
                    ) * direction;

            int index = (int) Math.round((returnSegment.length - 1) / 2 * (guessFactor + 1));
            returnSegment[index]++;
            return true;
        }

        return false;

    }

    public double getStartX() {return startX;}

    public double getStartY() {return startY;}

    public long getFireTime() {return fireTime;}
}
