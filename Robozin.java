package Robozin;

import robocode.*;
import robocode.util.*;
import java.awt.geom.*;
import java.awt.Color;
import java.lang.Math;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * Robozin - a robot by (Robozin)
 */

public class Robozin extends AdvancedRobot {
	// ******************* Variavel da Mira Trigonometrica *********************
	private double limit(double value, double min, double max) {
		return Math.min(max, Math.max(min, value));
	}
	// ******************************************************************
	static double enemyBulletSpeed = 3;
	static double direction;

	public void run() {


		setColors(Color.red, Color.blue, Color.green); // body,gun,radar
		setAdjustGunForRobotTurn(true); // Set gun to turn independent of the robot
		setAdjustRadarForGunTurn(true); // Set radar to turn independent of the gun
		turnRadarRightRadians(direction = Double.POSITIVE_INFINITY); // search for the enemy
		do {
			// Replace the next 4 lines with any behavior you would like
			// Check for new targets.
			// Only necessary for Narrow Lock because sometimes our radar is already
			// pointed at the enemy and our onScannedRobot code doesn't end up telling
			// it to turn, so the system doesn't automatically call scan() for us
			// [see the javadocs for scan()].
			scan();
		} while (true);
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		// ******************* Radar *********************
		double radarTurn =
				// Absolute bearing to target
				getHeadingRadians() + e.getBearingRadians()
				// Subtract current radar heading to get turn required
						- getRadarHeadingRadians();

		setTurnRadarRightRadians(Utils.normalRelativeAngle(radarTurn));


		// ******************* Mira Simples(Pitagoras) *********************
		// double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
		// setTurnGunRightRadians(Utils.normalRelativeAngle(absoluteBearing - 
		// 	getGunHeadingRadians() + (e.getVelocity() * Math.sin(e.getHeadingRadians() - 
		// 	absoluteBearing) / 13.0)));
		// setFire(3.0);

		// ******************* Mira Complicada(Trigonometria) *********************
		final double ROBOT_WIDTH =getWidth();
		final double ROBOT_HEIGHT = getHeight();
		final double FIREPOWER = 2;

		// Variables prefixed with e- refer to enemy, b- refer to bullet and r- refer to robot
		final double eAbsBearing = getHeadingRadians() + e.getBearingRadians();
		final double rX = getX();
		final double rY = getY();
		final double bV = Rules.getBulletSpeed(FIREPOWER);
		final double eX = rX + e.getDistance()*Math.sin(eAbsBearing);
		double eY = rY + e.getDistance()*Math.cos(eAbsBearing);
		double eV = e.getVelocity();
		double eHd = e.getHeadingRadians();
		// These constants make calculating the quadratic coefficients below easier
		final double A = (eX - rX)/bV;
		final double B = eV/bV*Math.sin(eHd);
		final double C = (eY - rY)/bV;
		final double D = eV/bV*Math.cos(eHd);
		// Quadratic coefficients: a*(1/t)^2 + b*(1/t) + c = 0
		final double a = A*A + C*C;
		final double b = 2*(A*B + C*D);
		final double c = (B*B + D*D - 1);
		final double discrim = b*b - 4*a*c;
		if (discrim >= 0) {
			// Reciprocal of quadratic formula
			final double t1 = 2*a/(-b - Math.sqrt(discrim));
			final double t2 = 2*a/(-b + Math.sqrt(discrim));
			final double t = Math.min(t1, t2) >= 0 ? Math.min(t1, t2) : Math.max(t1, t2);
			// Assume enemy stops at walls
			final double endX = limit(
				eX + eV*t*Math.sin(eHd),
				ROBOT_WIDTH/2, getBattleFieldWidth() - ROBOT_WIDTH/2);
			final double endY = limit(
				eY + eV*t*Math.cos(eHd),
				ROBOT_HEIGHT/2, getBattleFieldHeight() - ROBOT_HEIGHT/2);
			setTurnGunRightRadians(robocode.util.Utils.normalRelativeAngle(
				Math.atan2(endX - rX, endY - rY)
				- getGunHeadingRadians()));
			setFire(FIREPOWER);
		}
		// ******************* Mira Teorica *********************
		// double bulletPower = 3;
		// double headOnBearing = getHeadingRadians() + e.getBearingRadians();
		// double linearBearing = headOnBearing + Math.asin(e.getVelocity() / Rules.getBulletSpeed(bulletPower) * Math.sin(e.getHeadingRadians() - headOnBearing));
		// setTurnGunRightRadians(Utils.normalRelativeAngle(linearBearing - getGunHeadingRadians()));
		// setFire(bulletPower);


		// ******************* Movimentação *********************
		int integer = 30;
        double absoluteBearing;
		int antiRam;
		setTurnRightRadians(Math.cos(absoluteBearing = e.getBearingRadians())); // always perpendicular to the enemy
		setAhead(direction *= (Math.random() + (antiRam = (100 / (integer = (int)e.getDistance()))) - (0.6 * Math.sqrt(enemyBulletSpeed / integer) + 0.04)));
	}

	public void onHitByBullet(HitByBulletEvent e) {
		enemyBulletSpeed = e.getVelocity();

	}

	public void onHitWall(HitWallEvent e) {
		direction = -direction;
	}
}
