package Robozin;

import org.jpl7.Query;
import org.jpl7.Term;
import robocode.*;
import robocode.util.*;
import java.awt.Color;
import java.lang.Math;
import java.awt.Graphics2D;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * Robozin - a robot by (Felipe,Rodrigo,Anderson)
 */
public class Robozin extends AdvancedRobot {
	private double limit(double value, double min, double max) {
		return Math.min(max, Math.max(min, value));
	}

	static double enemySpeed = 3;
	static double direction;

	int scannedX = Integer.MIN_VALUE;
	int scannedY = Integer.MIN_VALUE;

	public void run() {
		if (org.jpl7.JPL.init()) {
			out.println("Prolog engine initiated");

			if (!Query.hasSolution("consult('./src/Robozin/teste.pl').")) {
				out.println("Consult failed");
			}
		}

		setColors(Color.green, Color.blue, Color.red); // body,gun,radar
		setAdjustGunForRobotTurn(true); // Set gun to turn independent of the robot
		setAdjustRadarForGunTurn(true); // Set radar to turn independent of the gun
		turnRadarRightRadians(direction = Double.POSITIVE_INFINITY);

		// Robot main loop
		while (true) {
			scan();
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		// ******************* Radar *********************
		// Cálculo original do radar
		// double radarTurn =
		// // Absolute bearing to target
		// getHeadingRadians() + e.getBearingRadians()
		// // Subtract current radar heading to get turn required
		// - getRadarHeadingRadians();

		// Chamadas de Heading, Bearing e RadarHearing feitas no prolog (funciona, é só
		// passar as variaveis na Query, mas o robô pode ficar alguns turnos travado)
		// Valores retornados do prolog
		// final double rHeadingRadians = new Query(
		// new Compound(
		// "getHeadingRadians",
		// new Term[] {
		// new org.jpl7.JRef(this),
		// new org.jpl7.Variable("Return")
		// }))
		// .oneSolution().get("Return")
		// .doubleValue();

		// final double eBearingRadians = new Query(
		// new Compound(
		// "getBearingRadians",
		// new Term[] {
		// new org.jpl7.JRef(e),
		// new org.jpl7.Variable("Return")
		// }))
		// .oneSolution().get("Return")
		// .doubleValue();

		// final double rRadarHeading = new Query(
		// new Compound(
		// "getRadarHeadingRadians",
		// new Term[] {
		// new org.jpl7.JRef(this),
		// new org.jpl7.Variable("Return")
		// }))
		// .oneSolution().get("Return")
		// .doubleValue();

		// Radar completo no prolog (funciona, mas o robô pode ficar alguns turnos
		// travado)
		// Query turnRadar = new Query("turnRadar", new Term[] { new
		// org.jpl7.JRef(this), new org.jpl7.JRef(e) });

		// if (!turnRadar.hasSolution()) {
		// out.println("turnRadar query failed!");
		// }

		// Cálculo do Absolute Bearing - RadarHeading feito no prolog (funciona e o robô
		// não fica travado)
		final double turnAmount = new Query(
				"radarTurn",
				new Term[] {
						new org.jpl7.Float(getHeadingRadians()),
						new org.jpl7.Float(e.getBearingRadians()),
						new org.jpl7.Float(getRadarHeadingRadians()),
						new org.jpl7.Variable("Res")
				}).oneSolution().get("Res").doubleValue();

		setTurnRadarRightRadians(Utils.normalRelativeAngle(turnAmount));

		// ******************* Mira Complicada(Trigonometria) *********************
		final double ROBOT_WIDTH = getWidth();
		final double ROBOT_HEIGHT = getHeight();
		final double FIREPOWER = 2;

		// Variables prefixed with e- refer to enemy, b- refer to bullet and r- refer to
		// robot
		final double eAbsBearing = getHeadingRadians() + e.getBearingRadians();
		final double rX = getX();
		final double rY = getY();
		final double bV = Rules.getBulletSpeed(FIREPOWER);
		final double eX = rX + e.getDistance() * Math.sin(eAbsBearing);
		double eY = rY + e.getDistance() * Math.cos(eAbsBearing);
		double eV = e.getVelocity();
		double eHd = e.getHeadingRadians();

		// These constants make calculating the quadratic coefficients below easier
		final double A = (eX - rX) / bV;
		final double B = eV / bV * Math.sin(eHd);
		final double C = (eY - rY) / bV;
		final double D = eV / bV * Math.cos(eHd);

		// Quadratic coefficients: a*(1/t)^2 + b*(1/t) + c = 0
		final double a = A * A + C * C;
		final double b = 2 * (A * B + C * D);
		final double c = (B * B + D * D - 1);
		final double discrim = b * b - 4 * a * c;

		// Descriminante checado no prolog
		final Query getDiscrim = new Query("getDiscrim", new Term[] { new org.jpl7.Float(discrim) });

		if (getDiscrim.hasSolution()) {
			// Reciprocal of quadratic formula
			final double t1 = 2 * a / (-b - Math.sqrt(discrim));
			final double t2 = 2 * a / (-b + Math.sqrt(discrim));
			final double t = Math.min(t1, t2) >= 0 ? Math.min(t1, t2) : Math.max(t1, t2);
			// Assume enemy stops at walls
			final double endX = limit(
					eX + eV * t * Math.sin(eHd),
					ROBOT_WIDTH / 2, getBattleFieldWidth() - ROBOT_WIDTH / 2);
			final double endY = limit(
					eY + eV * t * Math.cos(eHd),
					ROBOT_HEIGHT / 2, getBattleFieldHeight() - ROBOT_HEIGHT / 2);
			setTurnGunRightRadians(robocode.util.Utils.normalRelativeAngle(
					Math.atan2(endX - rX, endY - rY)
							- getGunHeadingRadians()));
			setFire(FIREPOWER);
		}

		// ******************* Movimentação *********************
		int integer = 30;

		// O valor do de cos(e.getBearing) calculado no prolog difere do valor calculado
		// no java, o que causa o robô a não andar direito
		// Query moveRobot = new Query("moveRobot",
		// new Term[] {
		// new org.jpl7.JRef(e),
		// new org.jpl7.JRef(this),
		// new org.jpl7.Float(direction *= (Math.random() + ((100 / (integer = (int)
		// e.getDistance())))
		// - (0.6 * Math.sqrt(enemySpeed / integer) + 0.04)))
		// });

		// if (!moveRobot.hasSolution()) {
		// out.println("moveRobot query failed");
		// }

		setTurnRightRadians(Math.cos(e.getBearingRadians()));
		setAhead(direction *= (Math.random() + ((100 / (integer = (int) e.getDistance())))
				- (0.6 * Math.sqrt(enemySpeed / integer) + 0.04)));

		// ******************* Graficos *********************
		double angle = Math.toRadians((getHeading() + e.getBearing()) % 360);

		// Calculate the coordinates of the robot
		scannedX = (int) (getX() + Math.sin(angle) * e.getDistance());
		scannedY = (int) (getY() + Math.cos(angle) * e.getDistance());
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		// org.jpl7.JRef ref = new org.jpl7.JRef(e);

		// Term goal = new Compound("getName", new Term[] { ref, new
		// org.jpl7.Variable("Return") });

		// Query q = new Query(goal);

		// java.util.Map<String, Term> solution = q.oneSolution();

		// out.println("I took a hit from: " + solution.get("Return").toString());

		enemySpeed = e.getVelocity();
	}

	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		direction = -direction;
	}

	public void onPaint(Graphics2D g) {
		// Set the paint color to a red half transparent color
		g.setColor(new Color(0xff, 0x00, 0x00, 0x80));

		// Draw a line from our robot to the scanned robot
		g.drawLine(scannedX, scannedY, (int) getX(), (int) getY());
		// Draw a filled square on top of the scanned robot that covers it
		g.fillRect(scannedX - 20, scannedY - 20, 40, 40);
	}
}
