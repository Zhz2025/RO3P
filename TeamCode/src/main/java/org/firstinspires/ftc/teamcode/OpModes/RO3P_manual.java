package org.firstinspires.ftc.teamcode.OpModes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ServoImplEx;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.RoadRunner.Drawing;
import org.firstinspires.ftc.teamcode.controllers.InstanceTelemetry;
import org.firstinspires.ftc.teamcode.controllers.Turret.TurretSubsystem;
import org.firstinspires.ftc.teamcode.controllers.intake.IntakeController;
import org.firstinspires.ftc.teamcode.controllers.swerve.SwerveDrive;
import org.firstinspires.ftc.teamcode.controllers.swerve.locate.Robot;
import org.firstinspires.ftc.teamcode.controllers.trigger.DoorTriggerController;
import org.firstinspires.ftc.teamcode.controllers.trigger.MultipleTriggerController;
import org.firstinspires.ftc.teamcode.controllers.trigger.RotationTriggerController;
import org.firstinspires.ftc.teamcode.controllers.trigger.TriggerController;

public class RO3P_manual extends LinearOpMode {

    SwerveDrive swerveDrive;
    TurretSubsystem myTurret;
    IntakeController myIntake;
    TriggerController myTrigger;

    @Override
    public void runOpMode() throws InterruptedException {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        telemetry = InstanceTelemetry.init(telemetry);
        swerveDrive = new SwerveDrive(hardwareMap);
        myTurret = new TurretSubsystem(hardwareMap,telemetry);
        myTurret.toggleAutoAiming();
        myTrigger = new MultipleTriggerController(
                new RotationTriggerController(hardwareMap.get(ServoImplEx.class,"pushServo")),
                new DoorTriggerController(hardwareMap.get(ServoImplEx.class,"triggerServo"))
        );

        //绑定按键
        myTurret.setBoardUpSupplier(() -> gamepad2.dpad_up);
        myTurret.setBoardDownSupplier(() -> gamepad2.dpad_down);
        myTurret.setTurretTurnRightSupplier(() -> gamepad2.dpad_right);
        myTurret.setTurretTurnLeftSupplier(() -> gamepad2.dpad_left);

        myIntake = new IntakeController(hardwareMap);

        swerveDrive.swerveController.setAutoLockHeading(false);
        waitForStart();
        while(opModeIsActive()){
            //drivetrain
            swerveDrive.swerveController.gamepadInput(gamepad1.left_stick_x,-gamepad1.left_stick_y,-gamepad1.right_stick_x);
            if(gamepad1.aWasReleased()) swerveDrive.swerveController.setAutoLockHeading(!swerveDrive.swerveController.getAutoLockHeading());
            if(gamepad1.bWasReleased()) swerveDrive.swerveController.exchangeNoHeadMode();
            if(gamepad1.xWasReleased()) swerveDrive.swerveController.resetNoHeadModeStartError();



            if(gamepad1.left_bumper){
                myIntake.setIntakeState(IntakeController.IntakeState.BITE);
            }
            else if(gamepad1.right_bumper){
                myIntake.setIntakeState(IntakeController.IntakeState.SWALLOW);
            }
            else if(gamepad2.a){
                myIntake.setIntakeState(IntakeController.IntakeState.OMIT);
            }
            else{
                myIntake.setIntakeState(IntakeController.IntakeState.SLEEP);
            }

            if(gamepad1.left_trigger > 0.5){
                //shoot
                myIntake.setIntakeState(IntakeController.IntakeState.SWALLOW);
            }
            if(gamepad1.right_trigger > 0.5){
                //shoot harder
                myIntake.setIntakeState(IntakeController.IntakeState.SWALLOW);
            }

            switch (myIntake.getIntakeState()) {
                case BITE:
                case OMIT:
                    myTrigger.setTriggerState(TriggerController.TriggerState.CLOSED);
                    break;
                case SWALLOW:
                    myTrigger.setTriggerState(TriggerController.TriggerState.OPEN);
                    break;
                case SLEEP:
                    myTrigger.setTriggerState(TriggerController.TriggerState.RESETTING);
                    break;
            }

            //turret
            myTurret.update(Robot.getInstance().getData().headingRadian);
            myIntake.update();
            myTrigger.update();
        }
    }
    public void telemetry(){
        telemetry.addData("AutoLockHeading",swerveDrive.swerveController.getAutoLockHeading());
        telemetry.addData("NoHeadMode",swerveDrive.swerveController.getUseNoHeadMode());
        telemetry.addData("x,y", Robot.getInstance().getData().getPosition(DistanceUnit.INCH).toString());
        telemetry.addData("heading", Robot.getInstance().getData().headingRadian);
        telemetry.addData("targetHeading",swerveDrive.swerveController.getHeadingLockRadian());
        telemetry.addLine();
        telemetry.addData("IntakeState",myIntake.getIntakeState());
        telemetry.addData("TriggerState",myTrigger.getTriggerState());
        telemetry.addLine();
        for(int index = 0; index<swerveDrive.swerveController.wheelUnits.length;index++){
            telemetry.addData(index+"Heading",swerveDrive.swerveController.wheelUnits[index].getHeading());
            telemetry.addData(index+"Speed",swerveDrive.swerveController.wheelUnits[index].getSpeed());
        }
        telemetry.update();
        TelemetryPacket packet = new TelemetryPacket();
        packet.fieldOverlay().setStroke("#3F51B5");
        Drawing.drawRobot(packet.fieldOverlay(), Robot.getInstance().getData().getPose2d());
        FtcDashboard.getInstance().sendTelemetryPacket(packet);
    }

}
