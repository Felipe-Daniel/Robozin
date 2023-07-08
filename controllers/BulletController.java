package robozin.controllers;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;
import robozin.recommenders.jayes.BayesNet;
import robozin.recommenders.jayes.BayesNode;
import robozin.recommenders.jayes.inference.IBayesInferer;
import robozin.recommenders.jayes.inference.junctionTree.JunctionTreeAlgorithm;

/** IT USES: */
public class BulletController {
	private final AdvancedRobot robot;
	private final IBayesInferer inferer;
	private final BayesNode accuracyNode;
	private final BayesNode myEnergyNode;
	private final BayesNode enemyEnergyNode;
	private final BayesNode enemyDistanceNode;
	private final BayesNode bulletPowerNode;
	private double oldEnemyHeading;
	public double bulletsHit;
	public double bulletsMissed;
	public double accuracy;
	public double enemyEnergy;
	public double enemyDistance;

	public BulletController(AdvancedRobot rb) {
		this.robot = rb;
		BayesNet bulletControllerNet = new BayesNet();

		accuracyNode = bulletControllerNet.createNode("accuracy_percentage");
		accuracyNode.addOutcomes("low", "medium", "high");
		accuracyNode.setProbabilities(0.5, 0.3, 0.2); // Low accuracy (20%) and high accuracy (80%)

		myEnergyNode = bulletControllerNet.createNode("my_energy");
		myEnergyNode.addOutcomes("low", "medium", "high");
		myEnergyNode.setProbabilities(0.2, 0.5, 0.3); // Low energy (40%) and high energy (60%)

		enemyEnergyNode = bulletControllerNet.createNode("enemy_energy");
		enemyEnergyNode.addOutcomes("low", "medium", "high");
		enemyEnergyNode.setProbabilities(0.2, 0.6, 0.2); // Low enemy energy (30%) and high enemy
		// energy // (70%)

		enemyDistanceNode = bulletControllerNet.createNode("enemy_distance");
		enemyDistanceNode.addOutcomes("low", "medium", "high");
		enemyDistanceNode.setProbabilities(0.2, 0.4, 0.3); // Close distance (60%) and far distance

		bulletPowerNode = bulletControllerNet.createNode("bullet_power");
		bulletPowerNode.addOutcomes("low", "medium", "high");
		bulletPowerNode.setParents(Arrays.asList(accuracyNode, myEnergyNode, enemyEnergyNode, enemyDistanceNode));
		double[] bulletPowerProbabilities = new double[243]; // 3^4 = 243 possible combinations
		for (int i = 0; i < bulletPowerProbabilities.length; i++) {
			bulletPowerProbabilities[i] = Math.random();
		}

		bulletPowerNode.setProbabilities(bulletPowerProbabilities);
		inferer = new JunctionTreeAlgorithm();
		inferer.setNetwork(bulletControllerNet);
	}

	public void execute(ScannedRobotEvent enemyRobotEvent) {
		double bulletPower;

		enemyEnergy = enemyRobotEvent.getEnergy();

		enemyDistance = enemyRobotEvent.getDistance();

//		bulletPower = Math.min(3.0, this.robot.getEnergy());

//		System.out.println("My energy: " + this.robot.getEnergy());

		Map<BayesNode, String> evidence = new HashMap<>();
		evidence.put(accuracyNode, parseAccuracy());
		evidence.put(enemyDistanceNode, parseEnemyDistance());
		evidence.put(enemyEnergyNode, parseEnemyHp());
		evidence.put(myEnergyNode, parseMyHp());
		inferer.setEvidence(evidence);

		double[] beliefs = inferer.getBeliefs(bulletPowerNode);

		int index = 0;
		for (double belief : beliefs) {
			System.out.println("Belief[" + index + "]: " + belief);
			index++;
		}

//		double[] bulletPowerProbabilities = bulletPowerNode.marginalize(evidence);

		int maxProbIndex = 0;

//		double maxProb = bulletPowerProbabilities[0];

		double maxProb = beliefs[0];

		for (int i = 1; i < beliefs.length; i++) {
			if (beliefs[i] > maxProb) {
				maxProb = beliefs[i];
				maxProbIndex = i;
			}
		}

		System.out.println("MaxProbIndex: " + maxProbIndex);

//		for (int i = 1; i < bulletPowerProbabilities.length; i++) {
//			if (bulletPowerProbabilities[i] > maxProb) {
//				maxProb = bulletPowerProbabilities[i];
//				maxProbIndex = i;
//			}
//		}

		bulletPower = mapIndexToBulletPower(maxProbIndex);

		System.out.println("Bullet power: " + bulletPower);

		this.robot.setFire(bulletPower);

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

	private void setAccuracy() {
		accuracy = bulletsHit / (bulletsHit + bulletsMissed);
	}

	private String parseAccuracy() {
		if (accuracy >= 0.75) {
			return "high";
		} else if (accuracy >= 0.5) {
			return "medium";
		}
		return "low";
	}

	private String parseEnemyHp() {
		if (enemyEnergy >= 70) {
			return "high";
		} else if (enemyEnergy >= 50) {
			return "medium";
		}
		return "low";
	}

	private String parseMyHp() {
		if (this.robot.getEnergy() >= 70) {
			return "high";
		} else if (this.robot.getEnergy() >= 50 && this.robot.getEnergy() < 70) {
			return "medium";
		}
		return "low";
	}

	private String parseEnemyDistance() {
		if (enemyDistance >= 500) {
			return "high";
		} else if (enemyDistance >= 333) {
			return "medium";
		}
		return "low";
	}

	public void infer() {
		Map<BayesNode, String> evidence = new HashMap<>();
		evidence.put(accuracyNode, parseAccuracy());
		evidence.put(enemyDistanceNode, "high");
		evidence.put(enemyEnergyNode, parseEnemyHp());
		evidence.put(myEnergyNode, parseMyHp());
		inferer.setEvidence(evidence);
		double[] beliefs = inferer.getBeliefs(bulletPowerNode);
	}

	private double mapIndexToBulletPower(int index) {
		switch (index) {
			case 0:
				return 1.0;
			case 1:
				return 2.0;
			case 2:
				return 3.0;
			default:
				return 1.0;
		}
	}
}