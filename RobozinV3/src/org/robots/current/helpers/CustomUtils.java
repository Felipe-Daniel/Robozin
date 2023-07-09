package org.robots.current.helpers;

import java.awt.geom.Point2D;

public class CustomUtils {
    public static double bulletVelocity(double power) {
        return 20 - 3 * power;
    }
   public static Point2D project(Point2D sourceLocation, double angle, double length) {
        return new Point2D.Double(sourceLocation.getX() + Math.sin(angle) * length,
                sourceLocation.getY() + Math.cos(angle) * length);
    }

    public static double absoluteBearing(Point2D source, Point2D target) {
        return Math.atan2(target.getX() - source.getX(), target.getY() - source.getY());
    }

    public static final double BATTLE_FIELD_WIDTH = 800;
    public static final double BATTLE_FIELD_HEIGHT = 600;

    public static final double MAX_DISTANCE = 900;
    public static final double MAX_BULLET_POWER = 3.0;
    public static final double BULLET_POWER = 1.9;
    public static final double WALL_MARGIN = 18;
    public static final double MAX_TRIES = 125;
    public static final double REVERSE_TUNER = 0.421075;
    public static final double WALL_BOUNCE_TUNER = 0.699484;

    public static final int DISTANCE_INDEXES = 5;
    public static final int VELOCITY_INDEXES = 5;
    public static final int LAST_VELOCITY_INDEXES = 5;
    public static final int WALL_INDEXES = 2;
    public static final int DECCEL_TIME_INDEXES = 6;
    public static final int AIM_FACTORS = 25;
    public static final int MIDDLE_FACTOR = (AIM_FACTORS - 1) / 2;

}
