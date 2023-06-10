package org.robots.helpers;


import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class EnemyWave { 
	public double fireTime;
	public double bulletVelocity;
	public double distanceTraveled;
	public double direction;
	public double directAngle;
	public Point2D.Double fireLocation;
	
	
	public static double wallSmoothing(Point2D.Double botLocation, double angle, int orientation, double wallStick) {
		Rectangle2D.Double _fieldRect = new java.awt.geom.Rectangle2D.Double(18, 18, 764, 564);
		while (!_fieldRect.contains(project(botLocation, angle, wallStick))) {
			angle += orientation * 0.05;
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
		return (20.0 - (3.0 * power));
	}

	public static double maxEscapeAngle(double velocity) {
		return Math.asin(8.0 / velocity);
	}

	
}
