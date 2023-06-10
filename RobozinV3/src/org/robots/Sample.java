package org.robots;

import org.robots.controllers.MovementController;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class Sample extends AdvancedRobot{
	
	private MovementController movementController; 
	
	@Override
	public void run() {
		MovementController movementController = new MovementController(this);
	}
	
	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		// TODO Auto-generated method stub
		this.movementController.execute(event);
	}
}
