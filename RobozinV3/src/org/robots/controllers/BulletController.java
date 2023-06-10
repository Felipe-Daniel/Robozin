package org.robots.controllers;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.util.*;
import java.awt.geom.*;
/** IT USES:  */
public class BulletController {
	
	private AdvancedRobot robot;
	private double oldEnemyHeading;
	
	public BulletController(AdvancedRobot rb) {
		this.robot = rb;
	}

	public void execute(ScannedRobotEvent enemyRobotEvent) {

		double bulletPower = Math.min(3.0, this.robot.getEnergy());
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
		while ((++deltaTime) * (20.0 - 3.0 * bulletPower) < Point2D.Double.distance(myX, myY, predictedX, predictedY)) {
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
		this.robot.fire(3);

	}
}
