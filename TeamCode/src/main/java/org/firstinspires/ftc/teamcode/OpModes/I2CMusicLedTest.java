package org.firstinspires.ftc.teamcode.OpModes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.controllers.led.I2CLedController;

@TeleOp(name = "I2CMusicLedTester",group = "Test")
public class I2CMusicLedTest extends LinearOpMode {
    I2CLedController ledController;
    @Override
    public void runOpMode() throws InterruptedException {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        ledController = hardwareMap.get(I2CLedController.class,"ledMusicController");
        waitForStart();
        while(opModeIsActive()) {
            if (gamepad1.aWasReleased()) {
                ledController.enable(I2CLedController.MusicMode.BREATH);
            } else if (gamepad1.bWasReleased()) {
                ledController.enable(I2CLedController.MusicMode.FLOW);
            }
            telemetry.addData("CurrentMusicMode", ledController.getCurrentMusicMode().toString());
            telemetry.addData("MusicEnabled",ledController.isMusicEnabled());
            telemetry.update();
        }
    }
}
