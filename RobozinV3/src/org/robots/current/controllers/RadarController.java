package org.robots.current.controllers;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class RadarController {
    private final AdvancedRobot robot;
    public RadarController(AdvancedRobot rb) {
        this.robot = rb;
    }

    public void execute(ScannedRobotEvent se) {

        double radarTurn = robot.getHeadingRadians()
            + se.getBearingRadians()
            - robot.getRadarHeadingRadians();
        robot.setTurnRadarRightRadians(1.9 * Utils.normalRelativeAngle(radarTurn));
    }


}
