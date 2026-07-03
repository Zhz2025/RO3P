package org.firstinspires.ftc.teamcode.controllers.Turret;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

@Config
public class HoodModule {
    private Servo servoL;
    private Servo servoR;
    private Telemetry myTelemetry;
    private double targetPosition = 0;
    public static double High_Position = 0.5;
    public static double Low_Position = 0.1;
    //1:
    //先归零，再安装！！！
    //除非有yra一样善良的工程，设计了把hood完全拉起来可以脱离齿轮的结构

    //2:
    //两个相同的270°舵机，实际可旋转的范围也不同（例如0-250和0-260）
    //导致setposition时两边不同步，一高一低
    //考虑到该误差近似线性，乘了一个系数k来平衡
    public static double k = 0.7;

    public HoodModule(HardwareMap hardwareMap, Telemetry telemetryRC){
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
    public void setPosition(double position){
        targetPosition = position;
    }

    public void update(){
        servoL.setPosition(targetPosition);
        servoR.setPosition(targetPosition * k);
        myTelemetry.addData("Board Position", targetPosition);
    }
}
