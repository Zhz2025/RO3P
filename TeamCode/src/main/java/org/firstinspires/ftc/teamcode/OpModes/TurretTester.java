package org.firstinspires.ftc.teamcode.OpModes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.RoadRunner.Localizer;
import org.firstinspires.ftc.teamcode.controllers.Turret.TurretModule_Simplified;
import org.firstinspires.ftc.teamcode.controllers.swerve.locate.Robot;

@Config
@TeleOp(name = "Turret Tester", group = "Test")
public class TurretTester extends LinearOpMode {
    public static double degree = 0;
    private enum ControlMode {
        POWER,
        DEGREE,      // 手动直接控制功率
        VELOCITY     // 速度环控制
    }

    private ControlMode currentMode = ControlMode.DEGREE;

    @Override
    public void runOpMode() throws InterruptedException {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
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
        TurretModule_Simplified turret = new TurretModule_Simplified(hardwareMap, telemetry);

        telemetry.addLine("Ready!");
        telemetry.addLine("A: toggle Manual/Velocity mode");
        telemetry.addLine("Manual: RightStickX -> power");
        telemetry.addLine("Velocity: RightStickX -> adjust target velocity");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {
            if (gamepad1.aWasPressed()) {
                // 切换模式
                if (currentMode == ControlMode.DEGREE) {
                    currentMode = ControlMode.VELOCITY;
                    turret.toggleVelTestMode(true);
                } else {
                    currentMode = ControlMode.DEGREE;
                    turret.toggleVelTestMode(false);
                }
            }
            if(gamepad1.bWasPressed()){
                if(currentMode != ControlMode.POWER){
                    currentMode = ControlMode.POWER;
                    turret.setManualControl();
                }
                else{
                    currentMode = ControlMode.DEGREE;
                    turret.setAutoControl();
                }
            }

            // ---- 根据模式处理输入 ----
            switch (currentMode) {
                case POWER:
                    turret.setManualControl();
                    turret.setMotorPower(gamepad1.left_stick_x);
                    telemetry.addData("Mode", "POWER");
                    telemetry.addData("POWER", "%.3f", gamepad1.left_stick_x);
                    break;
                case DEGREE: {
                    turret.setAutoControl();
                    turret.setTargetDegree(degree);
                    turret.update();
                    telemetry.addData("Mode", "DEGREE");
                    telemetry.addData("TargetDegree", "%.3f", degree);
                    break;
                }
                case VELOCITY: {
                    turret.setAutoControl();
                    // 调用 update() 执行速度环控制
                    turret.update();
                    telemetry.addData("Mode", "VELOCITY (Speed Loop)");
                    break;
                }
            }
            telemetry.addData("Degree", turret.getCurrentRobotDegree());
            telemetry.addData("---", "---");
            telemetry.update();
        }
    }
}
