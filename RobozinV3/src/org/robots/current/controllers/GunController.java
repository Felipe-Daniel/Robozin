package org.robots.current.controllers;

import org.robots.helpers.Logger;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class GunController {
    private static final String BULLET_CONTROLLER_LOGS = "bullet_controller_logs";
    private final AdvancedRobot robot;
    private final Logger logger;

    private double enemyDistance;
    private double enemyBearing;
    private double enemyEnergy;
    private double enemyVelocity;
    private double enemyHeading;
    private double enemyX;
    private double enemyY;
    private double enemyAcceleration;
    private double enemyDeceleration;
    // TODO: Implement GuessFactor Targeting
    // TODO: Implement Encog usage
    // TODO: Implement Neural Network
    // TODO: Validate if the captured data is enough to train the neural network
    public GunController(AdvancedRobot rb, String path) {
        this.robot = rb;
        final String headers = new StringBuilder()
                .append("time,")
                .append("myEnergy,")
                .append("bulletPower,")
                .append("enemyDistance,")
                .append("enemyBearing,")
                .append("enemyEnergy,")
                .append("enemyVelocity,")
                .append("enemyHeading,")
                .append("enemyX,")
                .append("enemyY,")
                .append("enemyAcceleration,")
                .append("enemyDeceleration,")
                .append("\n")
                .toString();

        this.logger = new Logger(path.concat(BULLET_CONTROLLER_LOGS), headers);
    }

    public void execute(ScannedRobotEvent enemyRobotEvent) {

    }

    public void onBulletHit(robocode.BulletHitEvent event) {
        logger.log(new Object[]{
                robot.getTime(),
                robot.getEnergy(),
                event.getBullet().getPower(),
                enemyDistance,
                enemyBearing,
                enemyEnergy,
                enemyVelocity,
                enemyHeading,
                enemyX,
                enemyY,
                enemyAcceleration,
                enemyDeceleration
        });
    }

    public void onBulletHit(robocode.BulletHitBulletEvent event) {
        logger.log(
            new Object[]{
                    robot.getTime(),
                    robot.getEnergy(),
                    event.getBullet().getPower(),
                    enemyDistance,
                    enemyBearing,
                    enemyEnergy,
                    enemyVelocity,
                    enemyHeading,
                    enemyX,
                    enemyY,
                    enemyAcceleration,
                    enemyDeceleration
        });
    }

    public void close() {
        logger.close();
    }
}
