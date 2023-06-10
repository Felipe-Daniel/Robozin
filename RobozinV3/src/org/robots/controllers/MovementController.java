package org.robots.controllers;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class MovementController {
	private AdvancedRobot robot;
	
	public MovementController(AdvancedRobot rb) {
		this.robot = rb;
	}
	
	public void execute(ScannedRobotEvent enemyRobotEvent) {
		
		this.robot.setAhead(100);
		
	}
}
