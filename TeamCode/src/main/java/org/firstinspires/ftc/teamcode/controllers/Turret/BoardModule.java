package org.firstinspires.ftc.teamcode.controllers.Turret;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

@Config
public class BoardModule {
    private Servo servoL;
    private Servo servoR;
    private Telemetry myTelemetry;
    private double targetPosition = 0;
    public static double High_Position = 0.5;
    public static double Low_Position = 0.0;

    public BoardModule(HardwareMap hardwareMap, Telemetry telemetryRC){
        servoL = hardwareMap.get(Servo.class,"BoardServoL");
        servoR = hardwareMap.get(Servo.class,"BoardServoR");
        servoL.setDirection(Servo.Direction.FORWARD);
        servoR.setDirection(Servo.Direction.REVERSE);
        myTelemetry = telemetryRC;

        setLowPosition();
    }
    public void setHighPosition(){
        targetPosition = High_Position;
    }
    public void setLowPosition() {
        targetPosition = Low_Position;
    }
    public void update(){
        servoL.setPosition(targetPosition);
        servoR.setPosition(targetPosition);
        myTelemetry.addData("Board Position", targetPosition);
    }
}
