package org.firstinspires.ftc.teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.controllers.Turret.TurretModule_Simplified;

@TeleOp(name = "Turret Tester", group = "Test")
public class TurretTester extends LinearOpMode {

    // 速度环模式下每次调整目标速度的步长 (deg/s)
    private static final double VELOCITY_STEP = 50.0;
    private enum ControlMode {
        MANUAL,      // 手动直接控制功率
        VELOCITY     // 速度环控制
    }

    private ControlMode currentMode = ControlMode.MANUAL;

    @Override
    public void runOpMode() throws InterruptedException {
        TurretModule_Simplified turret = new TurretModule_Simplified(hardwareMap, telemetry);
        // 默认为手动控制模式
        turret.setManualControl();

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
