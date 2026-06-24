package org.firstinspires.ftc.teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.controllers.Turret.BoardModule;

@TeleOp
public class BoardTester extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        BoardModule myBoard = new  BoardModule(hardwareMap,telemetry);
        myBoard.setLowPosition();
        waitForStart();
        while(opModeIsActive()){
            if(gamepad1.a){
                myBoard.setLowPosition();
            }
            if(gamepad1.b){
                myBoard.setHighPosition();
            }
            myBoard.update();
            sleep(20);
        }
    }
}
