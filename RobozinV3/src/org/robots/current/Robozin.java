package org.robots.current;

import org.robots.current.controllers.GunController;
import org.robots.current.controllers.MovementController;
import org.robots.current.controllers.RadarController;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class Robozin extends AdvancedRobot {

    // TODO: Implement Encog usage
    // TODO: Implement Neural Network
    private MovementController movementController;
    private GunController gunController;
    private RadarController radarController;

    public void run() {
        String path = "C:\\Users\\loyol\\IdeaProjects\\Robozin\\RobozinV3\\src\\logs\\";
        this.movementController = new MovementController(this, path);
        this.gunController = new GunController(this, path);
        this.radarController = new RadarController(this);
        setAdjustRadarForGunTurn(true);
        setAdjustGunForRobotTurn(true);
        turnRadarRightRadians(Double.POSITIVE_INFINITY);
        do {
            scan();
        } while (true);
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        radarController.execute(e);
        gunController.execute(e);
        movementController.execute(e);
    }

    @Override
    public void onBulletHit(robocode.BulletHitEvent event) {
        gunController.onBulletHit(event);
    }

    @Override
    public void onBulletHitBullet(robocode.BulletHitBulletEvent event) {
        gunController.onBulletHit(event);
    }
    @Override
    public void onHitByBullet(robocode.HitByBulletEvent event) {
        movementController.onHitByBullet(event);
    }

    @Override
    public void onBattleEnded(robocode.BattleEndedEvent event) {
        gunController.close();
        movementController.close();
    }
}
