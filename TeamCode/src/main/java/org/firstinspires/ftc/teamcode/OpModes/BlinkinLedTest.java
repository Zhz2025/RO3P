package org.firstinspires.ftc.teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.controllers.led.BlinkinLedController;

@TeleOp(name = "BlinkinLedTester",group = "Test")
public class BlinkinLedTest extends LinearOpMode {
    BlinkinLedController blinkinLedController;
    @Override
    public void runOpMode() throws InterruptedException {
        blinkinLedController = new BlinkinLedController(hardwareMap);
        waitForStart();
        while(opModeIsActive()) {
            if (gamepad1.aWasReleased()) {
                blinkinLedController.setNextPattern();
            } else if (gamepad1.bWasReleased()) {
                blinkinLedController.setPreviousPattern();
            }
            telemetry.addData("CurrentPreset", blinkinLedController.getCurrentPattern().toString());
            telemetry.addData("CurrentLength",blinkinLedController.getCurrentLength());
            telemetry.addData("CurrentIndex",blinkinLedController.getCurrentPattern().ordinal());
            telemetry.update();
        }
    }
}
