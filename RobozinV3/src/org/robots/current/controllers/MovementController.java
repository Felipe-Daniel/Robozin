package org.robots.current.controllers;

import org.robots.helpers.Logger;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class MovementController {
    private final AdvancedRobot robot;
    private final Logger logger;

    // TODO: Implement Encog usage
    // TODO: Implement Neural Network
    // TODO: Implement Wave Surfing
    // TODO: Define the data to be captured and add it to headers
    // TODO: Define the logging worthy scenarios properly
    // TODO: Validate if the captured data is enough to train the neural network
    public MovementController(AdvancedRobot rb, String path) {
        final String headers =  new StringBuilder()
                .append("time,")
                .toString();
        this.robot = rb;
        this.logger = new Logger(path.concat("movement_controller_logs"), headers);

    }

    public void execute(ScannedRobotEvent enemyRobotEvent) {

    }

    public void onHitByBullet(robocode.HitByBulletEvent event) {
        logger.log(new Object[]{
                robot.getTime(),
        });
    }

    public void close() {
        logger.close();
    }

}
