package org.robots.current;

import org.robots.current.controllers.GunController;
import org.robots.current.controllers.MovementController;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class Robozin extends AdvancedRobot {

    // TODO: Implement Encog usage
    // TODO: Implement Neural Network
    private final MovementController movementController;
    private final GunController gunController;

    public Robozin() {
        String path = "logs/";
        this.movementController = new MovementController(this, path);
        this.gunController = new GunController(this, path);
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
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
