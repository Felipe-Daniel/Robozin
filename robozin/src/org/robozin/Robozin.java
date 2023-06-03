package org.robozin;

import robocode.*;
import robocode.util.*;
import java.awt.geom.*;
import java.awt.Color;
import java.lang.Math;
import java.awt.Graphics2D;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.plot.JFuzzyChart;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * Robozin - a robot by (Felipe,Rodrigo,Anderson)
 */

public class Robozin extends AdvancedRobot {
	// ******************* Mira *********************
	private MovementHelper movementHelper;
	final static String fclFileName = "/robozin/src/org/robozin/utils/rules/fire_power.hcl";
	
	FIS fis = null;
	
	private double limit(double value, double min, double max) {
		return Math.min(max, Math.max(min, value));
	}
	
	
	// ******************************************************************
	static double enemySpeed = 3;
	static double direction;

	int scannedX = Integer.MIN_VALUE;
	int scannedY = Integer.MIN_VALUE;

	public void run() {
		
		fis = FIS.load(fclFileName);

		if( fis == null ) { 
			System.err.println("Erro ao carregar arquivo: '" + fclFileName + "'");
			return;
		}
		
		movementHelper = new MovementHelper(); 

		setColors(Color.green, Color.blue, Color.red); // body,gun,radar
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
		// fonte: https://robowiki.net/wiki/Radar
		double radarTurn =
				// Absolute bearing to target
				getHeadingRadians() + e.getBearingRadians()
				// Subtract current radar heading to get turn required
						- getRadarHeadingRadians();

		setTurnRadarRightRadians(Utils.normalRelativeAngle(radarTurn));

		// ******************* Mira Trigonometrica *********************
		// fonte: https://robowiki.net/wiki/Linear_Targeting
		final double ROBOT_WIDTH = getWidth();
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

		// ******************* Movimentação *********************
		// inspirado em: https://robowiki.net/wiki/Random_Movement +  https://robowiki.net/wiki/Musashi_Trick
		int integer = 30;
        double absoluteBearing;
		int antiram = movementHelper.calculateAntiramFactor(e);
		setTurnRightRadians(Math.cos(absoluteBearing = e.getBearingRadians())); // always perpendicular to the enemy
		// Parte 1 rodar em volta oponente e inverter direção quando chegar na parede
		// setAhead(direction);
		// Parte 2 explicar movimentos aleatorios
		// parte 3 explicar antiRam: quando o inimigo tenta te atropelar, o valor estoura, a aleatoriedade para, e o robo se move o mais rapido possivel em apenas uma direção
		setAhead(direction *= (Math.random() + antiram) - (0.6 * Math.sqrt(enemySpeed / integer) + 0.04));
		
		
		// ******************* Graficos *********************
		double angle = Math.toRadians((getHeading() + e.getBearing()) % 360);

		// Calculate the coordinates of the robot
		scannedX = (int)(getX() + Math.sin(angle) * e.getDistance());
		scannedY = (int)(getY() + Math.cos(angle) * e.getDistance());
		
	}

	public void onHitByBullet(HitByBulletEvent e) {
		enemySpeed = e.getVelocity();

	}

	public void onHitWall(HitWallEvent e) {
		direction = -direction;
	}

	public void onPaint(Graphics2D g) {
		// Set the paint color to a red half transparent color
		g.setColor(new Color(0xff, 0x00, 0x00, 0x80));
	
		// Draw a line from our robot to the scanned robot
		g.drawLine(scannedX, scannedY, (int)getX(), (int)getY());
		// Draw a filled square on top of the scanned robot that covers it
		g.fillRect(scannedX - 20, scannedY - 20, 40, 40);
	}
}
