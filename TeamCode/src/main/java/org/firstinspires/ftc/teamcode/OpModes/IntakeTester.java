package org.firstinspires.ftc.teamcode.OpModes;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ServoImplEx;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.RoadRunner.Localizer;
import org.firstinspires.ftc.teamcode.controllers.intake.IntakeController;
import org.firstinspires.ftc.teamcode.controllers.swerve.locate.Robot;
import org.firstinspires.ftc.teamcode.controllers.trigger.DoorTriggerController;
import org.firstinspires.ftc.teamcode.controllers.trigger.MultipleTriggerController;
import org.firstinspires.ftc.teamcode.controllers.trigger.RotationTriggerController;
import org.firstinspires.ftc.teamcode.controllers.trigger.TriggerController;

@TeleOp(name = "IntakeTester",group = "Test")
public class IntakeTester extends LinearOpMode {
    IntakeController intakeController;
    TriggerController triggerModule;
    VoltageSensor voltageSensor;
    @Override
    public void runOpMode() throws InterruptedException {
        voltageSensor = hardwareMap.voltageSensor.iterator().next();
        Robot.refresh(new Localizer() {
            @Override
            public void setPose(Pose2d pose) {

            }

            @Override
            public Pose2d getPose() {
                return new Pose2d(0,0,0);
            }

            @Override
            public PoseVelocity2d update() {
                return new PoseVelocity2d(new Vector2d(0,0),0);
            }
        },hardwareMap.voltageSensor.iterator().next());
        intakeController = new IntakeController(hardwareMap);
        triggerModule = new MultipleTriggerController(
                new RotationTriggerController(hardwareMap.get(ServoImplEx.class,"pushServo")),
                new DoorTriggerController(hardwareMap.get(ServoImplEx.class,"triggerServo"))
        );
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
            if(gamepad1.xWasPressed()){
                triggerModule.setTriggerState(TriggerController.TriggerState.OPEN);
            }else if(gamepad1.yWasPressed()) {
                triggerModule.setTriggerState(TriggerController.TriggerState.CLOSED);
            }else if(gamepad1.bWasPressed()){
                triggerModule.setTriggerState(TriggerController.TriggerState.RESTING);
            }
            triggerModule.update();
        }
    }
}
