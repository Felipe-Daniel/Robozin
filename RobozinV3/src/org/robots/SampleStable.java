package org.robots;

import java.awt.Graphics2D;

import org.robots.controllers.BulletController;
import org.robots.controllers.MovementController;

import robocode.AdvancedRobot;
import robocode.BulletHitBulletEvent;
import robocode.HitByBulletEvent;
import robocode.ScannedRobotEvent;

public class SampleStable extends AdvancedRobot{
	
	private BulletController bulletController; 
	private MovementController movementController;
	
	@Override
	public void run() {
		bulletController = new BulletController(this);
		movementController = new MovementController(this);
		setAdjustGunForRobotTurn(true); // Set gun to turn independent of the robot
		setAdjustRadarForGunTurn(true); // Set radar to turn independent of the gun
		turnRadarRightRadians(Double.POSITIVE_INFINITY); // search for the enemy
		do {
			 turnRadarRightRadians(Double.POSITIVE_INFINITY);
		}while(true);
		
	}
	
	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		// TODO Auto-generated method stub
		this.movementController.execute(event);
		this.bulletController.execute(event);
	}
	
	@Override
	public void onHitByBullet(HitByBulletEvent event) {
		this.movementController.collectData(event);
	}
	
	@Override
	public void onBulletHitBullet(BulletHitBulletEvent event) {
		this.movementController.collectData(event);
	}
	
	@Override
	public void onPaint(Graphics2D g) {
		movementController.paint(g);
	}
}
