package robozin;
import robozin.controllers.BulletController;
import robozin.controllers.MovementController;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import robocode.*;

/**
 * Robozin - a robot by (Anderson ,Felipe, Rodrigo)
 */
public class Robozin extends AdvancedRobot {
	private FIS fis;
	private BulletController bulletController;
	private MovementController movementController;

	@Override
	public void run() {
		loadRulesFile();
		initControllers();
		setAdjustGunForRobotTurn(true); // Set gun to turn independent of the robot
		setAdjustRadarForGunTurn(true); // Set radar to turn independent of the gun
		turnRadarRightRadians(Double.POSITIVE_INFINITY); // search for the enemy
//		do {
//			 turnRadarRightRadians(Double.POSITIVE_INFINITY);
//		}while(true);

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
		movementController = new MovementController(this, movementFunctionBlock);
		bulletController = new BulletController(this);
	}

	private void loadRulesFile() {
		final String fclFilePath ="/Users/rodrigocsm/study/robocode/robots/Robozin/utils/rules.fcl";
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
		bulletController.bulletsHit++;
	}

	public void onRoundEnded(RoundEndedEvent event) {
		// TODO Auto-generated method stub
//		bulletController.infer();
		System.out.println("[last round accuracy]: " + bulletController.accuracy);
		System.out.println("[last round hits]: " + bulletController.bulletsHit);
		System.out.println("[last round misses]: " + bulletController.bulletsMissed);

//		System.out.println("Bullet power: ");
//		bulletController.bulletsMissed = 0;
//		bulletController.bulletsHitted = 0;
//		bulletController.accuracy = 0;
	}
}
