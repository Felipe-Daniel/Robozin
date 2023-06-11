package org.robots.controllers;

import java.awt.geom.Point2D;

import net.sourceforge.jFuzzyLogic.FunctionBlock;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
/** IT USES:  */
public class BulletController {

	private AdvancedRobot robot;
	private FunctionBlock bulletControllerRules;
	private double oldEnemyHeading;
	public double bulletsHitted;
	public double bulletsMissed;
	public double accuracy;

	public BulletController(AdvancedRobot rb) {
		this.robot = rb;
	}


	public BulletController(AdvancedRobot rb, FunctionBlock bulletControllerRules) {
		this(rb);
		this.bulletControllerRules = bulletControllerRules;
		bulletsHitted = 0;
		bulletsMissed = 0;
		accuracy = 0;

	}

	public void execute(ScannedRobotEvent enemyRobotEvent) {

		double bulletPower;

		if(bulletControllerRules == null) {
			bulletPower = Math.min(3.0, this.robot.getEnergy());
		} else {
			bulletControllerRules.setVariable("accuracy_percentage", accuracy);
			bulletControllerRules.setVariable("my_energy", robot.getEnergy());
			bulletControllerRules.setVariable("enemy_energy", enemyRobotEvent.getEnergy());
			bulletControllerRules.setVariable("enemy_distance", enemyRobotEvent.getDistance());
			bulletControllerRules.evaluate();
			bulletPower = bulletControllerRules.getVariable("bullet_power").getValue();
		}

		double myX = this.robot.getX();
		double myY = this.robot.getY();
		double absoluteBearing = this.robot.getHeadingRadians() + enemyRobotEvent.getBearingRadians();
		double enemyX = this.robot.getX() + enemyRobotEvent.getDistance() * Math.sin(absoluteBearing);
		double enemyY = this.robot.getY() + enemyRobotEvent.getDistance() * Math.cos(absoluteBearing);
		double enemyHeading = enemyRobotEvent.getHeadingRadians();
		double enemyHeadingChange = enemyHeading - oldEnemyHeading;
		double enemyVelocity = enemyRobotEvent.getVelocity();
		oldEnemyHeading = enemyHeading;

		double deltaTime = 0;
		double battleFieldHeight = this.robot.getBattleFieldHeight(), battleFieldWidth = this.robot.getBattleFieldWidth();
		double predictedX = enemyX, predictedY = enemyY;
		while ((++deltaTime) * (20.0 - 3.0 * bulletPower) < Point2D.distance(myX, myY, predictedX, predictedY)) {
			predictedX += Math.sin(enemyHeading) * enemyVelocity;
			predictedY += Math.cos(enemyHeading) * enemyVelocity;
			enemyHeading += enemyHeadingChange;
			if (predictedX < 18.0 || predictedY < 18.0 || predictedX > battleFieldWidth - 18.0
					|| predictedY > battleFieldHeight - 18.0) {

				predictedX = Math.min(Math.max(18.0, predictedX), battleFieldWidth - 18.0);
				predictedY = Math.min(Math.max(18.0, predictedY), battleFieldHeight - 18.0);
				break;
			}
		}
		double theta = Utils.normalAbsoluteAngle(Math.atan2(predictedX - this.robot.getX(), predictedY - this.robot.getY()));

		this.robot.setTurnRadarRightRadians(Utils.normalRelativeAngle(absoluteBearing - this.robot.getRadarHeadingRadians()));
		this.robot.setTurnGunRightRadians(Utils.normalRelativeAngle(theta - this.robot.getGunHeadingRadians()));
		this.robot.fire(bulletPower);
		setAccuracy();

	}
	
	private void setAccuracy () {
		accuracy = bulletsHitted/(bulletsHitted+bulletsMissed);
	}
}
