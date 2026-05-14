package org.firstinspires.ftc.teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.controllers.Turret.TurretModule_Simplified;

@TeleOp(name = "Turret Tester", group = "Test")
public class TurretTester extends LinearOpMode {


    @Override
    public void runOpMode() throws InterruptedException {
        TurretModule_Simplified turret = new TurretModule_Simplified(hardwareMap, telemetry);
        // Enable manual control mode
        turret.toggleControlMode();

        telemetry.addLine("Ready to control turret. Use right stick X to control power.");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {
            // Use right stick X axis for turret power
            double power = gamepad1.right_stick_x;
            turret.setMotorPower(power);

            telemetry.addData("Turret Power", power);
            telemetry.update();
        }
    }
}
