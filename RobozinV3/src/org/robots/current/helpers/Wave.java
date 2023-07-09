package org.robots.current.helpers;

import robocode.AdvancedRobot;
import robocode.Condition;
import robocode.util.Utils;
import java.awt.geom.Point2D;

public class Wave extends Condition {
    private final AdvancedRobot robot;
    public double wBulletPower;
    public Point2D wGunLocation;
    public double wBearing;
    public double wBearingDirection;
    public int[] wAimFactors;
    public double wDistance;
    public Point2D enemyLocation;

    public Wave(AdvancedRobot robot){
        this.robot = robot;
    }

    public boolean test() {
        if ((wDistance += CustomUtils.bulletVelocity(wBulletPower)) > wGunLocation.distance(enemyLocation) - 18) {
            try {
                wAimFactors[(int)Math.round(((Utils.normalRelativeAngle(CustomUtils.absoluteBearing(wGunLocation, enemyLocation) - wBearing)) /
                        wBearingDirection) + CustomUtils.MIDDLE_FACTOR)]++;
            }
            catch (Exception e) {
            }
            this.robot.removeCustomEvent(this);
        }
        return false;
    }
}

