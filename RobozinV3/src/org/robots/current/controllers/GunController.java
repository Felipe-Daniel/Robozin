package org.robots.current.controllers;

import org.robots.current.helpers.Wave;
import org.robots.helpers.Logger;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import static org.robots.current.helpers.CustomUtils.*;

public class GunController {
    private static final String BULLET_CONTROLLER_LOGS = "bullet_controller_logs";
    private final AdvancedRobot robot;
    private final Logger logger;

    private double enemyVelocity;

    private double bearingDirection;
    private int timeSinceDeccel;

    public boolean bulletHit = false;

    int[][][][][][] aimFactors = new int[DISTANCE_INDEXES]
            [VELOCITY_INDEXES][LAST_VELOCITY_INDEXES][DECCEL_TIME_INDEXES][WALL_INDEXES][AIM_FACTORS];
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
                .append("bulletHit")
                .append("\n")
                .toString();

        this.logger = new Logger(path.concat(BULLET_CONTROLLER_LOGS), headers);
    }
    // IMPLEMENTS GUESS FACTOR TARGETING
    public void execute(ScannedRobotEvent enemyRobotEvent) {
        int mostVisited = MIDDLE_FACTOR, i = AIM_FACTORS;
        Wave wave = new Wave(this.robot);
        Rectangle2D fieldRectangle = new Rectangle2D.Double(WALL_MARGIN, WALL_MARGIN,
                BATTLE_FIELD_WIDTH - WALL_MARGIN * 2, BATTLE_FIELD_HEIGHT - WALL_MARGIN * 2);
        double enemyDistance;
        double enemyAbsoluteBearing = this.robot.getHeadingRadians() + enemyRobotEvent.getBearingRadians();
        wave.enemyLocation = project(wave.wGunLocation = new Point2D.Double(
                this.robot.getX(),
                this.robot.getY()),
                enemyAbsoluteBearing,
                enemyDistance = enemyRobotEvent.getDistance())
        ;
        int lastVelocityIndex = (int)Math.abs(enemyVelocity) / 2;
        int velocityIndex = (int)Math.abs((enemyVelocity = enemyRobotEvent.getVelocity()) / 2);
        if (velocityIndex < lastVelocityIndex) {
            timeSinceDeccel = 0;
        }

        if (enemyVelocity != 0) {
            bearingDirection = enemyVelocity * Math.sin(enemyRobotEvent.getHeadingRadians() - enemyAbsoluteBearing) > 0 ?
                    0.7 / (double)MIDDLE_FACTOR : -0.7 / (double)MIDDLE_FACTOR;
        }
        wave.wBearingDirection = bearingDirection;

        int distanceIndex;
        wave.wBulletPower = Math.min(enemyRobotEvent.getEnergy() / 4,
                (distanceIndex = (int)(enemyDistance / (MAX_DISTANCE / DISTANCE_INDEXES))) > 1 ? BULLET_POWER : MAX_BULLET_POWER);


        wave.wAimFactors = aimFactors[distanceIndex][velocityIndex][lastVelocityIndex][Math.min(5, timeSinceDeccel++ / 13)]
                [fieldRectangle.contains(project(wave.wGunLocation, enemyAbsoluteBearing + wave.wBearingDirection * 13, enemyDistance)) ? 1 : 0];

        wave.wBearing = enemyAbsoluteBearing;


        do  {
            if (wave.wAimFactors[--i] > wave.wAimFactors[mostVisited]) {
                mostVisited = i;
            }
        } while (i > 0);

        this.robot.setTurnGunRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing - this.robot.getGunHeadingRadians() +
                wave.wBearingDirection * (mostVisited - MIDDLE_FACTOR)));

        this.robot.setFire(wave.wBulletPower);
        if (this.robot.getEnergy() >= BULLET_POWER) {
            this.robot.addCustomEvent(wave);
        }
       logger.log(new Object[]{
                robot.getTime(),
                robot.getEnergy(),
                enemyDistance,
                enemyAbsoluteBearing,
                enemyRobotEvent.getEnergy(),
                enemyVelocity,
                enemyRobotEvent.getHeading(),
                wave.enemyLocation.getX(),
                wave.enemyLocation.getY(),
        });
        if(bulletHit)
            bulletHit = false;
    }

    public void close() {
        logger.close();
    }


}
