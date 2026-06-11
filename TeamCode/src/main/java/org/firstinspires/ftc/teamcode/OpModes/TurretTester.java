package org.firstinspires.ftc.teamcode.OpModes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.RoadRunner.Localizer;
import org.firstinspires.ftc.teamcode.controllers.Turret.TurretModule_Simplified;
import org.firstinspires.ftc.teamcode.controllers.swerve.locate.Robot;

@TeleOp(name = "Turret Tester", group = "Test")
public class TurretTester extends LinearOpMode {

    // 速度环模式下每次调整目标速度的步长 (deg/s)
    private static final double VELOCITY_STEP = 50.0;
    private enum ControlMode {
        MANUAL,      // 手动直接控制功率
        VELOCITY     // 速度环控制
    }

    private ControlMode currentMode = ControlMode.VELOCITY;

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
                if (currentMode == ControlMode.MANUAL) {
                    currentMode = ControlMode.VELOCITY;
                    turret.setAutoControl();
                } else {
                    currentMode = ControlMode.MANUAL;
                    turret.setManualControl();
                }
            }

            // ---- 根据模式处理输入 ----
            switch (currentMode) {
                case MANUAL: {
                    double power = gamepad1.right_stick_x;
                    turret.setMotorPower(power);
                    telemetry.addData("Mode", "MANUAL (Power)");
                    telemetry.addData("Turret Power", "%.3f", power);
                    break;
                }
                case VELOCITY: {
                    // 右摇杆X轴控制目标速度增量
                    double deltaV = gamepad1.right_stick_x * VELOCITY_STEP;
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
