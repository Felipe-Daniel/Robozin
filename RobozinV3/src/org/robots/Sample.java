package org.robots;

import org.robots.controllers.BulletController;
import org.robots.controllers.MovementController;

import robocode.AdvancedRobot;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitByBulletEvent;
import robocode.RoundEndedEvent;
import robocode.ScannedRobotEvent;
import robocode.control.events.TurnEndedEvent;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
//import net.sourceforge.jFuzzyLogic.plot.JFuzzyChart;

/** WIP: THE ROBOT WHERE WE WILL PUT THE FUZZY LOGIG INTO THE CONTROLLERS */
public class Sample extends AdvancedRobot{
	
	private FIS fis;
	
	private BulletController bulletController; 
	private MovementController movementController;
	
	// SOME METRICS TO BE USED DURING FUZZY
	public double bulletsMissed;
	
	@Override
	public void run() {
		loadRulesFile();
		initControllers();
		setAdjustGunForRobotTurn(true); // Set gun to turn independent of the robot
		setAdjustRadarForGunTurn(true); // Set radar to turn independent of the gun
		turnRadarRightRadians(Double.POSITIVE_INFINITY); // search for the enemy
		do {
			 turnRadarRightRadians(Double.POSITIVE_INFINITY);
		}while(true);
		
	}
	
	private void initControllers() {
		// MAYBE TRY SINGLETON???
		if (movementController != null || bulletController != null) { return; }
		if (fis == null) {
			bulletController = new BulletController(this);
			movementController = new MovementController(this);
			return;
		}
		FunctionBlock movementFunctionBlock = fis.getFunctionBlock("movement_controller");
		FunctionBlock bulletFunctionBlock = fis.getFunctionBlock("bullet_controller");
		movementController = new MovementController(this, movementFunctionBlock);
		bulletController = new BulletController(this, bulletFunctionBlock);
	}
	
	private void loadRulesFile() {
		final String fclFilePath ="./src/utils/rules.fcl";
		fis = FIS.load(fclFilePath);
		if (fis == null) {
			System.out.println("[EXCEPTION]: Failed to loading fcl file");
		}
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
	public void onBulletMissed(BulletMissedEvent event) {
		// TODO Auto-generated method stub
		bulletController.bulletsMissed++;
	}
	
	@Override
	public void onBulletHit(BulletHitEvent event) {
		bulletController.bulletsHitted++;
	}
	
	@Override
	public void onRoundEnded(RoundEndedEvent event) {
		// TODO Auto-generated method stub
		System.out.println("[last round accuracy]: " + bulletController.bulletsHitted/bulletController.bulletsMissed);
		System.out.println("[last round hits]: " + bulletController.bulletsHitted);
		System.out.println("[last round misses]: " + bulletController.bulletsMissed);
		
		bulletController.bulletsMissed = 0;
		bulletController.bulletsHitted = 0;
		bulletController.accuracy = 0;
	}
	
	public void onTurnEnded(TurnEndedEvent event) {
		System.out.println("[ROUND EVENT]:");
	}
	
}
