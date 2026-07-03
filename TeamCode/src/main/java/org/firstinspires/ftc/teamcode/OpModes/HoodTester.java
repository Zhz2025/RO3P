package org.firstinspires.ftc.teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.controllers.Turret.HoodModule;

@TeleOp
public class HoodTester extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        HoodModule myHood = new HoodModule(hardwareMap,telemetry);
        myHood.setLowPosition();
        waitForStart();
        while(opModeIsActive()){
            if(gamepad1.a){
                myHood.setLowPosition();
            }
            if(gamepad1.b){
                myHood.setHighPosition();
            }
            myHood.update();
            sleep(20);
        }
    }
}
