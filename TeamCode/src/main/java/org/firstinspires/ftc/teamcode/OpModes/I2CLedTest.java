package org.firstinspires.ftc.teamcode.OpModes;

import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.controllers.led.LedController;

@TeleOp(name = "I2CLedTester",group = "Test")
public class I2CLedTest extends LinearOpMode {
    LedController ledController;
    @Override
    public void runOpMode() throws InterruptedException {
        ledController = hardwareMap.get(LedController.class,"ledController");
        waitForStart();
        while(opModeIsActive()) {
            if (gamepad1.aWasReleased()) {
                ledController.setPattern(ledController.getCurrentPattern()+1);
            } else if (gamepad1.bWasReleased()) {
                ledController.setPattern(ledController.getCurrentPattern()-1);
            }
            telemetry.addData("CurrentPreset", RevBlinkinLedDriver.BlinkinPattern.fromNumber(ledController.getCurrentPattern()).toString());
            telemetry.addData("CurrentIndex",ledController.getCurrentPattern());
            telemetry.update();
        }
    }
}
