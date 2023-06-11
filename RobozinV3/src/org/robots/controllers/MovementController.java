package org.robots.controllers;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.robots.helpers.EnemyWave;

import net.sourceforge.jFuzzyLogic.FunctionBlock;
import robocode.AdvancedRobot;
import robocode.Bullet;
import robocode.BulletHitBulletEvent;
import robocode.HitByBulletEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class MovementController {
	// TODO: maybe apply singleton
	private AdvancedRobot robot;
	private FunctionBlock movementBlock;
	private final double WALL_STICK = 160;
	public static int BINS = 47;
	public static double _surfStats[] = new double[BINS];
	public Point2D.Double _myLocation; // our bot's location
	public Point2D.Double _enemyLocation; // enemy bot's location
	public double enemyEnergy;

	public ArrayList<EnemyWave> _enemyWaves;
	public ArrayList<Integer> _surfDirections;
	public ArrayList<Double> _surfAbsBearings;

	public MovementController(AdvancedRobot robot) {
		this.robot = robot;
		_enemyWaves = new ArrayList<>();
		_surfDirections = new ArrayList<>();
		_surfAbsBearings = new ArrayList<>();
		enemyEnergy = 100.0;
	}

	public MovementController(AdvancedRobot robot, FunctionBlock movementBlock) {
		this(robot);
		this.movementBlock = movementBlock;
	}

	private void setBackAsFront(double goAngle) {
		double angle = Utils.normalRelativeAngle(goAngle - robot.getHeadingRadians());
		if (Math.abs(angle) > (Math.PI / 2)) {
			if (angle < 0) {
				robot.setTurnRightRadians(Math.PI + angle);
			} else {
				robot.setTurnLeftRadians(Math.PI - angle);
			}
			robot.setBack(100);
		} else {
			if (angle < 0) {
				robot.setTurnLeftRadians(-1 * angle);
			} else {
				robot.setTurnRightRadians(angle);
			}
			robot.setAhead(100);
		}
	}

	public void collectData(BulletHitBulletEvent hit) {
		Bullet enemyBullet = hit.getHitBullet();
		this.parseCollectedData(enemyBullet.getX(), enemyBullet.getY(), enemyBullet.getPower());
	}

	public void collectData(HitByBulletEvent hit) {
		Bullet enemyBullet = hit.getBullet();
		this.parseCollectedData(enemyBullet.getX(), enemyBullet.getY(), enemyBullet.getPower());
	}

	public void execute(ScannedRobotEvent enemyEvent) {
		// TODO: apply the fuzzy logic to get the bullet power
		double bulletPower = enemyEnergy - enemyEvent.getEnergy();
		double enemyDistance = enemyEvent.getDistance();
		double lateralVelocity = robot.getVelocity() * Math.sin(enemyEvent.getBearingRadians());
		double absBearing = enemyEvent.getBearingRadians() + robot.getHeadingRadians();
		_myLocation = new Point2D.Double(robot.getX(), robot.getY());

		robot.setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing - robot.getRadarHeadingRadians()) * 2);

		_surfDirections.add(0, new Integer((lateralVelocity >= 0) ? 1 : -1));
		_surfAbsBearings.add(0, new Double(absBearing + Math.PI));
		if (bulletPower < 3.01 && bulletPower > 0.09 && _surfDirections.size() > 2) {
			EnemyWave ew = new EnemyWave();
			ew.fireTime = robot.getTime() - 1;
			ew.bulletVelocity = EnemyWave.bulletVelocity(bulletPower);
			ew.distanceTraveled = EnemyWave.bulletVelocity(bulletPower);
			ew.direction = _surfDirections.get(2).intValue();
			ew.directAngle = _surfAbsBearings.get(2).doubleValue();
			ew.fireLocation = (Point2D.Double) _enemyLocation.clone(); // last tick
			_enemyWaves.add(ew);
		}
		enemyEnergy = enemyEvent.getEnergy();
		_enemyLocation = EnemyWave.project(_myLocation, absBearing, enemyDistance);
		if (movementBlock != null) {
			loadFuzzyVariables(bulletPower, absBearing, enemyDistance, lateralVelocity);
		}
		updateWaves();
		doSurfing();

	}

	private void updateWaves() {
		final int EXTRA_SPACE = 50;
		for (int x = 0; x < _enemyWaves.size(); x++) {
			EnemyWave ew = _enemyWaves.get(x);
			ew.distanceTraveled = (robot.getTime() - ew.fireTime) * ew.bulletVelocity;
			if (ew.distanceTraveled > _myLocation.distance(ew.fireLocation) + EXTRA_SPACE) {
				_enemyWaves.remove(x);
				x--;
			}
		}
	}

	private void parseCollectedData(Double bulletX, Double bulletY, Double bulletPower) {
		if (!_enemyWaves.isEmpty()) {
			Point2D.Double hitBulletLocation = new Point2D.Double(bulletX, bulletY);
			EnemyWave hitWave = null;

			// look through the EnemyWaves, and find one that could've hit us.
			for (EnemyWave element : _enemyWaves) {
				EnemyWave ew = element;

				if (Math.abs(ew.distanceTraveled - _myLocation.distance(ew.fireLocation)) < 50
						&& Math.abs(EnemyWave.bulletVelocity(bulletPower) - ew.bulletVelocity) < 0.001) {
					hitWave = ew;
					break;
				}
			}

			if (hitWave != null) {
				logHit(hitWave, hitBulletLocation);

				// We can remove this wave now, of course.
				_enemyWaves.remove(_enemyWaves.lastIndexOf(hitWave));
			}
		}
	}

	private void doSurfing() {
		EnemyWave surfWave = getClosestSurfableWave();

		if (surfWave == null) {
			return;
		}

		double goAngle;
		if (movementBlock != null) {
			goAngle = parseEscapeAngle();
		} else {
			goAngle = parseEscapeAngle(surfWave);
		}

		setBackAsFront(goAngle);

	}

	public Point2D.Double predictPosition(EnemyWave surfWave, int direction) {
		Point2D.Double predictedPosition = (Point2D.Double) _myLocation.clone();
		double predictedVelocity = robot.getVelocity();
		double predictedHeading = robot.getHeadingRadians();
		double maxTurning, moveAngle, moveDir;
		int counter = 0; // number of ticks in the future
		boolean intercepted = false;

		do { // the rest of these code comments are rozu's
			moveAngle = EnemyWave.wallSmoothing(predictedPosition,
					EnemyWave.absoluteBearing(surfWave.fireLocation, predictedPosition) + (direction * (Math.PI / 2)),
					direction, WALL_STICK) - predictedHeading;
			moveDir = 1;

			if (Math.cos(moveAngle) < 0) {
				moveAngle += Math.PI;
				moveDir = -1;
			}

			moveAngle = Utils.normalRelativeAngle(moveAngle);

			// maxTurning is built in like this, you can't turn more then this in one tick
			maxTurning = Math.PI / 720d * (40d - 3d * Math.abs(predictedVelocity));
			predictedHeading = Utils
					.normalRelativeAngle(predictedHeading + EnemyWave.limit(-maxTurning, moveAngle, maxTurning));

			// this one is nice ;). if predictedVelocity and moveDir have
			// different signs you want to breack down
			// otherwise you want to accelerate (look at the factor "2")
			predictedVelocity += (predictedVelocity * moveDir < 0 ? 2 * moveDir : moveDir);
			predictedVelocity = EnemyWave.limit(-8, predictedVelocity, 8);

			// calculate the new predicted position
			predictedPosition = EnemyWave.project(predictedPosition, predictedHeading, predictedVelocity);

			counter++;

			if (predictedPosition.distance(surfWave.fireLocation) < surfWave.distanceTraveled
					+ (counter * surfWave.bulletVelocity) + surfWave.bulletVelocity) {
				intercepted = true;
			}
		} while (!intercepted && counter < 500);

		return predictedPosition;
	}

	// TODO: fuzzify the closest distance mayble?
	public EnemyWave getClosestSurfableWave() {
		double closestDistance = 50000; // I juse use some very big number here
		EnemyWave surfWave = null;

		for (EnemyWave element : _enemyWaves) {
			EnemyWave ew = element;
			double distance = _myLocation.distance(ew.fireLocation) - ew.distanceTraveled;

			if (distance > ew.bulletVelocity && distance < closestDistance) {
				surfWave = ew;
				closestDistance = distance;
			}
		}
		return surfWave;
	}

	public double checkDanger(EnemyWave surfWave, int direction) {
		int index = getFactorIndex(surfWave, predictPosition(surfWave, direction));

		return _surfStats[index];
	}

	// Given the EnemyWave that the bullet was on, and the point where we
	// were hit, calculate the index into our stat array for that factor.
	public static int getFactorIndex(EnemyWave ew, Point2D.Double targetLocation) {
		double offsetAngle = (EnemyWave.absoluteBearing(ew.fireLocation, targetLocation) - ew.directAngle);
		double factor = Utils.normalRelativeAngle(offsetAngle) / EnemyWave.maxEscapeAngle(ew.bulletVelocity)
				* ew.direction;

		return (int) EnemyWave.limit(0, (factor * ((BINS - 1) / 2)) + ((BINS - 1) / 2), BINS - 1);
	}

	public void logHit(EnemyWave ew, Point2D.Double targetLocation) {
		int index = getFactorIndex(ew, targetLocation);

		for (int x = 0; x < BINS; x++) {
			// for the spot bin that we were hit on, add 1;
			// for the bins next to it, add 1 / 2;
			// the next one, add 1 / 5; and so on...
			_surfStats[x] += 1.0 / (Math.pow(index - x, 2) + 1);
		}
	}

	/**
	 * lateral_velocity : REAL; // Lateral velocity of the enemy robot abs_bearing :
	 * REAL; // Absolute bearing of the enemy robot bullet_power : REAL; // Power of
	 * the enemy bullet distance : REAL; // Distance between our bot and enemy bot
	 */

	private void loadFuzzyVariables(Double enemyBulletPower, Double enemyAbsBearing, Double distance,
			Double lateralVelocity) {
		movementBlock.setVariable("lateral_velocity", lateralVelocity);
		movementBlock.setVariable("distance", distance);
		movementBlock.setVariable("abs_bearing", enemyAbsBearing);
		movementBlock.setVariable("bullet_power", enemyBulletPower);
	}

	private double parseEscapeAngle(EnemyWave surfWave) {
		double dangerLeft = checkDanger(surfWave, -1);
		double dangerRight = checkDanger(surfWave, 1);
		Double goAngle = EnemyWave.absoluteBearing(surfWave.fireLocation, _myLocation);
		if (dangerLeft < dangerRight) {
			return EnemyWave.wallSmoothing(_myLocation, goAngle - (Math.PI / 2), -1, WALL_STICK);
		}
		return EnemyWave.wallSmoothing(_myLocation, goAngle + (Math.PI / 2), 1, WALL_STICK);

	}

	private double parseEscapeAngle() {
		return movementBlock.getVariable("go_angle").getValue();
	}

	public void paint(Graphics2D g) {
		g.setColor(java.awt.Color.red);
		for (EnemyWave element : _enemyWaves) {
			EnemyWave w = (element);
			Point2D.Double center = w.fireLocation;

			// int radius = (int)(w.distanceTraveled + w.bulletVelocity);
			// hack to make waves line up visually, due to execution sequence in robocode
			// engine
			// use this only if you advance waves in the event handlers (eg. in
			// onScannedRobot())
			// NB! above hack is now only necessary for robocode versions before 1.4.2
			// otherwise use:s
			int radius = (int) w.distanceTraveled;

			// Point2D.Double center = w.fireLocation;
			if (radius - 40 < center.distance(_myLocation))
				g.drawOval((int) (center.x - radius), (int) (center.y - radius), radius * 2, radius * 2);
		}
	}
}
