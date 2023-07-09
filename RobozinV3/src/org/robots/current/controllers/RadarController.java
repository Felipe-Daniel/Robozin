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
        final double factor = 2.0;
        double radarTurn = robot.getHeadingRadians()
            + se.getBearingRadians()
            - robot.getRadarHeadingRadians();
        robot.setTurnRadarRightRadians(factor * Utils.normalRelativeAngle(radarTurn));
    }


}
