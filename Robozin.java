package Robozin;

import robocode.*;
import robocode.util.*;
import java.awt.geom.*;
import java.awt.Color;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * Robozin - a robot by (your name here)
 */
public class Robozin extends AdvancedRobot
{
	/**
	 * run: NewRobot's default behavior
	 */
	public void run() {

		setColors(Color.red,Color.blue,Color.green); // body,gun,radar
		setAdjustGunForRobotTurn(true); // Set gun to turn independent of the robot
        setAdjustRadarForGunTurn(true); // Set radar to turn independent of the gun
		turnRadarRightRadians(Double.POSITIVE_INFINITY); // fastly search for a the enemy
		do {
			// Replace the next 4 lines with any behavior you would like
			ahead(100); //Go ahead 100 pixels
			turnGunRight(360); //scan
			back(75); //Go back 75 pixels
			turnGunRight(360); //scan
			// Check for new targets.
			// Only necessary for Narrow Lock because sometimes our radar is already
			// pointed at the enemy and our onScannedRobot code doesn't end up telling
			// it to turn, so the system doesn't automatically call scan() for us
			// [see the javadocs for scan()].
			scan();
		} while (true);


		// while(true) {

		// }
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
	// *******************   Radar *********************
		// Replace the next line with any behavior you would like
		double radarTurn =
        // Absolute bearing to target
        getHeadingRadians() + e.getBearingRadians()
        // Subtract current radar heading to get turn required
        - getRadarHeadingRadians();

    setTurnRadarRightRadians(Utils.normalRelativeAngle(radarTurn));
	// **************************************************




	double distance = e.getDistance(); //get the distance of the scanned robot
    if(distance > 800) //this conditions adjust the fire force according the distance of the scanned robot.
        fire(5);
    else if(distance > 600 && distance <= 800)
        fire(4);
    else if(distance > 400 && distance <= 600)
        fire(3);
    else if(distance > 200 && distance <= 400)
        fire(2);
    else if(distance < 200)
        fire(1);
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		// Replace the next line with any behavior you would like
		back(10);
		
	}
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		// Replace the next line with any behavior you would like
		double bearing = e.getBearing(); //get the bearing of the wall
  	  	turnRight(-bearing); //This isn't accurate but release your robot.
	    ahead(100); //The robot goes away from the wall.
	}	
}
