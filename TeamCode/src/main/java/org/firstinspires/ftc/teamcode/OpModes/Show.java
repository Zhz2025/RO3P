package org.firstinspires.ftc.teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.controllers.LED.BlinkinLedController;

public class Show extends LinearOpMode {
    BlinkinLedController myLedBelt;
    @Override
    public void runOpMode() throws InterruptedException {
        myLedBelt = new BlinkinLedController(hardwareMap);
        waitForStart();
        while(opModeIsActive()){

        }
    }
}
