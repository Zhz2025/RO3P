package org.firstinspires.ftc.teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.controllers.Turret.TurretModule;

@TeleOp
public class TurretMotorTester extends LinearOpMode {
    DcMotorEx motor;
    @Override
    public void runOpMode() throws InterruptedException {
        motor = hardwareMap.get(DcMotorEx.class, "turret");
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        waitForStart();
        while(opModeIsActive()){
            if(Math.abs(gamepad1.left_stick_x) > 0.2){
                motor.setPower(gamepad1.left_stick_x);
            }
            else{
                if(gamepad1.a) {
                    motor.setPower(1);
                } else if (gamepad1.b) {
                    motor.setPower(0.5);
                }
                else if(gamepad1.x){
                    motor.setPower(-1);
                }
                else if(gamepad1.y){
                    motor.setPower(-0.5);
                }
            }
            telemetry.addData("pos in ticks", motor.getCurrentPosition());
            telemetry.addData("pos in degree", TurretModule.tickToDegree(motor.getCurrentPosition()));
            telemetry.addData("vel", motor.getVelocity());
            telemetry.addData("current", motor.getCurrent(CurrentUnit.AMPS));
            telemetry.update();
        }
    }
}
