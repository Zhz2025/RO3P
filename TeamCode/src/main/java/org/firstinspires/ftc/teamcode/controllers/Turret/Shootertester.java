package org.firstinspires.ftc.teamcode.controllers.Turret;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

@TeleOp(name = "Shootertester", group = "Tests")
public class Shootertester extends LinearOpMode {

    public FlyWheelModule flyWheelModule;

    public void runOpMode() throws InterruptedException {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        // 初始化飞轮系统，根据实际电机名称和反转设置调整
        flyWheelModule = new FlyWheelModule(hardwareMap,telemetry);
        waitForStart();
        while (opModeIsActive()) {
            // 测试不同速度
            if (gamepad1.aWasPressed()) {
                flyWheelModule.setTargetSpeed(2000); // 高速
            } else if (gamepad1.bWasPressed()) {
                flyWheelModule.setTargetSpeed(1000); // 中高速
            } else if (gamepad1.yWasPressed()) {
                flyWheelModule.setTargetSpeed(650); // 中速
            } else if (gamepad1.xWasPressed()) {
                flyWheelModule.setTargetSpeed(400); // 低速
            } else if (gamepad1.dpad_up) {
                flyWheelModule.setTargetSpeed(0); // 停止
            }
            flyWheelModule.update();

            telemetry.update();
        }
    }
}