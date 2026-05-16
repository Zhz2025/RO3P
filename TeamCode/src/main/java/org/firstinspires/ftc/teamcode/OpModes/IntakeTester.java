package org.firstinspires.ftc.teamcode.OpModes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.controllers.intake.IntakeController;

@TeleOp(name = "IntakeTester",group = "Test")
public class IntakeTester extends LinearOpMode {
    IntakeController intakeController;
    VoltageSensor voltageSensor;
    @Override
    public void runOpMode() throws InterruptedException {
        voltageSensor = hardwareMap.voltageSensor.iterator().next();
        intakeController = new IntakeController(hardwareMap);
        waitForStart();
        while(opModeIsActive()){
            double current = intakeController.getIntakeMotor().getCurrent(CurrentUnit.AMPS);
            double voltage = intakeController.getIntakeMotor().getPower() * voltageSensor.getVoltage();
            telemetry.addData("intakeState",intakeController.getIntakeState());
            telemetry.addData("intakeVelocity",intakeController.getIntakeMotor().getVelocity());
            telemetry.addData("intakeCurrent",current);
            telemetry.addData("intakeVoltage",voltage);
            telemetry.addData("intakePower",current*voltage);
            telemetry.update();
            if(gamepad1.aWasReleased()) {
                intakeController.setIntakeState(intakeController.getIntakeState().next());
            }
            else if(gamepad1.dpadUpWasReleased()){
                intakeController.setIntakeState(IntakeController.IntakeState.BITE);
            }
            else if(gamepad1.dpadRightWasReleased()){
                intakeController.setIntakeState(IntakeController.IntakeState.SWALLOW);
            }
            else if(gamepad1.dpadDownWasReleased()){
                intakeController.setIntakeState(IntakeController.IntakeState.SLEEP);
            }
            else if(gamepad1.dpadLeftWasReleased()){
                intakeController.setIntakeState(IntakeController.IntakeState.OMIT);
            }
            intakeController.update();
        }
    }
}
