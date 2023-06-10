package org.robots;

import robocode.*;
import robocode.control.events.TurnEndedEvent;
import robocode.util.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.lang.Math;
import java.awt.Graphics2D;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
//import net.sourceforge.jFuzzyLogic.plot.JFuzzyChart;
import java.util.List;

import org.robots.helpers.WaveBullet;

import java.util.ArrayList;


/**
 * Robozin - a robot by (Felipe,Rodrigo,Anderson)
 */

public class Robozin extends AdvancedRobot {
	
	final static String fclFileName = "C:/Users/loyol/Documents/Robozin/RobozinV3/src/utils/rules.fcl";
	
	private FunctionBlock firePower;
	
	private FIS fis;
	private double bulletsHited = 1;
	private double bulletsShot = 1;
	private int bestIndex = 15;
	int[][] stats; 
	int[] currentStats;
	double guessFactor;
	double movementDirection;
	int direction = 1;
	int enemyDirection = 1;
	private List<WaveBullet> waves = new ArrayList<WaveBullet>();
	
	private double enemyBulletSpeed = 100;
	private double enemyVelocity = 0;
	private double robotWidth, robotHeight;
	private int scannedX = Integer.MIN_VALUE;
	private int scannedY = Integer.MIN_VALUE;
	

	public void run() {
		fis = FIS.load(fclFileName, true);
		firePower = fis.getFunctionBlock("bullet_controller");
		robotHeight = getHeight();
		robotWidth = getWidth();
		stats = new int[(int)getBattleFieldHeight()][(int)getBattleFieldWidth()];
		if( fis == null ) { 
			System.err.println("Erro ao carregar arquivo: '" + fclFileName + "'");
			return;
		}

		setColors(Color.green, Color.blue, Color.red); // body,gun,radar
		setAdjustGunForRobotTurn(true); // Set gun to turn independent of the robot
		setAdjustRadarForGunTurn(true); // Set radar to turn independent of the gun
		turnRadarRightRadians(movementDirection = Double.POSITIVE_INFINITY); // search for the enemy
		do {
			scan();
		} while (true);
	}


	public void onScannedRobot(ScannedRobotEvent enemyEvent) {
	    // Radar movement
	    double radarTurn = getHeadingRadians() + enemyEvent.getBearingRadians() - getRadarHeadingRadians();
	    setTurnRadarRightRadians(Utils.normalRelativeAngle(radarTurn));


	    double enemyDistance = enemyEvent.getDistance();
	    double enemyEnergy = enemyEvent.getEnergy();
	    enemyVelocity =  enemyEvent.getVelocity();
	    
	    double myEnergy = getEnergy();
	    
	    double accuracyPercentage = bulletsHited/bulletsShot;
	    firePower.setVariable("enemy_distance", enemyDistance);
	    firePower.setVariable("enemy_energy", enemyEnergy);
	    firePower.setVariable("my_energy", myEnergy);
	    firePower.setVariable("accuracy_percentage", accuracyPercentage);
	    firePower.evaluate();
	    
	    double bulletPower = firePower.getVariable("bullet_power").getValue();
	    
	    System.out.println("accuracy_percentage => " + accuracyPercentage);
	    System.out.println("fire power => " + bulletPower);
	    
	    double absBearing = getHeadingRadians() + enemyEvent.getBearingRadians();

		double ex = getX() + Math.sin(absBearing) * enemyEvent.getDistance();
		double ey = getY() + Math.cos(absBearing) * enemyEvent.getDistance();
		
		if (enemyVelocity != 0)
		{
			if (Math.sin(enemyEvent.getHeadingRadians()-absBearing)*enemyVelocity < 0)
				enemyDirection = -1;
			else
				enemyDirection = 1;
		}
		
		 currentStats = stats[(int)((enemyEvent.getDistance() / 100))]; 
		
		WaveBullet newWave = new WaveBullet(getX(), getY(), absBearing, bulletPower,
                        enemyDirection, getTime(), currentStats);
	
	 
		for (int i=0; i<31; i++)
			if (currentStats[bestIndex] < currentStats[i])
				bestIndex = i;
		
	
		setGuessFactorAngle(absBearing, newWave);
		
        setTurnGunRightRadians(guessFactor);
        if ((getGunHeat() == 0
                && guessFactor < Math.atan2(9, enemyEvent.getDistance())
                && setFireBullet(bulletPower) != null)) {
            waves.add(newWave);
            this.bulletsShot++;
        }
		
	    
        // Movement logic
	    double enemyBearing = enemyEvent.getBearingRadians();
	    int antiRamFactor = 100 / (int) enemyDistance;
	    setTurnRightRadians(Math.cos(enemyBearing));
	    double randomMovement = Math.random();
	    double movementDistance = direction * (randomMovement + antiRamFactor - (0.6 * Math.sqrt(enemyVelocity / 30) + 0.04));
	    setAhead(movementDistance);
	    // Graphics rendering
	    double angle = Math.toRadians((getHeading() + enemyEvent.getBearing()) % 360);
	    scannedX = (int) (getX() + Math.sin(angle) * enemyEvent.getDistance());
	    scannedY = (int) (getY() + Math.cos(angle) * enemyEvent.getDistance());
	}

		
	public void onHitByBullet(HitByBulletEvent e) {
		enemyBulletSpeed = e.getVelocity();
		System.out.println("enemy bullet speed =>" + enemyBulletSpeed);
	}
	

	public void onBulletHit(BulletHitEvent event) {
		this.bulletsHited++;
	}

	

	public void onHitWall(HitWallEvent e) {
		direction = -direction;
	}
	

	public void onRoundEnded(RoundEndedEvent event) {
		bulletsHited = 0;
		bulletsShot = 1;
	}
	
	public void setGuessFactorAngle(double absBearing, WaveBullet newWave) {
		double guessfactor = (double)(bestIndex - (stats.length - 1) / 2)
		        / ((stats.length - 1) / 2);;
		double angleOffset = enemyDirection * guessfactor * newWave.maxEscapeAngle();
		guessfactor =  Utils.normalRelativeAngle(
				absBearing 
				- getGunHeadingRadians() 
				+ angleOffset
		); 
	}
	
	


	
	public void onPaint(Graphics2D g) {
        // Set the paint color and stroke for drawing
        g.setColor(Color.red);
        g.setStroke(new BasicStroke(2));
        
        // Loop through your guess factor statistics array
        for (int i = 0; i < stats.length; i++) {
            for (int j = 0; j < stats[i].length; j++) {
                // Calculate the X and Y coordinates for drawing
                int x = (int) (getX() + i * 100 * Math.sin(j * guessFactor));
                int y = (int) (getY() + i * 100 * Math.cos(j * guessFactor));
                
                // Draw a dot at the calculated position
                g.drawOval(x, y, 2, 2);
            }
        }
    }
	    
}