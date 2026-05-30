package org.firstinspires.ftc.teamcode.controllers.Turret;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.RoadRunner.Localizer;
import org.firstinspires.ftc.teamcode.controllers.swerve.locate.Robot;

@Config
@TeleOp(name = "Shootertester", group = "Tests")
public class Shootertester extends LinearOpMode {
    public static double high_speed = 1800;
    public static double medium_high_speed = 1600;
    public static double medium_low_speed = 1200;
    public static double low_speed = 1000;
    public FlyWheelModule flyWheelModule;

    public void runOpMode() throws InterruptedException {
        Robot.refresh(new Localizer() {
            @Override
            public void setPose(Pose2d pose) {

            }

            @Override
            public Pose2d getPose() {
                return new Pose2d(0,0,0);
            }

            @Override
            public PoseVelocity2d update() {
                return new PoseVelocity2d(new Vector2d(0,0),0);
            }
        },hardwareMap.voltageSensor.iterator().next());
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        // 初始化飞轮系统，根据实际电机名称和反转设置调整
        flyWheelModule = new FlyWheelModule(hardwareMap,telemetry);
        waitForStart();
        while (opModeIsActive()) {
            // 测试不同速度
            if (gamepad1.aWasPressed()) {
                flyWheelModule.setTargetSpeed((int) high_speed); // 高速
            } else if (gamepad1.bWasPressed()) {
                flyWheelModule.setTargetSpeed((int) medium_high_speed); // 中高速
            } else if (gamepad1.yWasPressed()) {
                flyWheelModule.setTargetSpeed((int) medium_low_speed); // 中速
            } else if (gamepad1.xWasPressed()) {
                flyWheelModule.setTargetSpeed((int) low_speed); // 低速
            } else if (gamepad1.dpad_up) {
                flyWheelModule.setTargetSpeed(0); // 停止
            }
            if(gamepad1.left_stick_y!=0){
                flyWheelModule.setTargetSpeed((int)(-gamepad1.left_stick_y*high_speed));
            }
            flyWheelModule.update();

            telemetry.update();
        }
    }
}