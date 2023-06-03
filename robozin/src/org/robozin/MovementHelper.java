package org.robozin;

import robocode.ScannedRobotEvent;

public class MovementHelper {
	/* initial declaration was: (antiRam = (100 / (integer = (int)e.getDistance()) */
	public int calculateAntiramFactor(ScannedRobotEvent e) {
		return  (100 /(int)e.getDistance());
	}
}
