package org.firstinspires.ftc.teamcode.controllers.Turret;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;

public class TurretSubsystem {
    private Velocity currentVelocity;
    private AngularVelocity currentAngularVelocity;
    private Telemetry myTelemetry;
    public TurretModule turretModule;
    public FlyWheelModule flyWheelModule;
    public BoardModule boardModule;

    public TurretSubsystem(HardwareMap hardwareMap, Telemetry telemetryRC){
        turretModule = new TurretModule(hardwareMap,telemetryRC);
        flyWheelModule = new FlyWheelModule(hardwareMap,telemetryRC);
        boardModule = new BoardModule(hardwareMap,telemetryRC);
    }


    public void updateVelocity(Velocity velocityRC, AngularVelocity angularVelocityRC){
        currentVelocity = velocityRC;
        currentAngularVelocity = angularVelocityRC;
    }

    public void setTarget(double tx, double distance, double RobotHeading){

    }
    public void update(double RobotHeading){
        turretModule.update();
        flyWheelModule.update();
        boardModule.update();

    }
}
