package org.firstinspires.ftc.teamcode.OpModes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.RoadRunner.Drawing;
import org.firstinspires.ftc.teamcode.controllers.InstanceTelemetry;
import org.firstinspires.ftc.teamcode.controllers.Turret.TurretSubsystem;
import org.firstinspires.ftc.teamcode.controllers.intake.IntakeController;
import org.firstinspires.ftc.teamcode.controllers.swerve.SwerveDrive;
import org.firstinspires.ftc.teamcode.controllers.swerve.locate.RobotPosition;

import java.util.function.BooleanSupplier;

public class RO3P_manual extends LinearOpMode {

    SwerveDrive swerveDrive;
    TurretSubsystem myTurret;
    IntakeController myIntake;

    @Override
    public void runOpMode() throws InterruptedException {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        telemetry = InstanceTelemetry.init(telemetry);
        swerveDrive = new SwerveDrive(hardwareMap);
        myTurret = new TurretSubsystem(hardwareMap,telemetry);
        myTurret.toggleAutoAiming();

        //绑定按键
        myTurret.setBoardUpSupplier(() -> gamepad2.dpad_up);
        myTurret.setBoardDownSupplier(() -> gamepad2.dpad_down);
        myTurret.setTurretTurnRightSupplier(() -> gamepad2.dpad_right);
        myTurret.setTurretTurnLeftSupplier(() -> gamepad2.dpad_left);

        myIntake = new IntakeController(hardwareMap);
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

            //turret
            myTurret.update(0);
            myIntake.update();
        }
    }
    public void telemetry(){
        for(int index = 0; index<swerveDrive.swerveController.wheelUnits.length;index++){
            telemetry.addData(index+"Heading",swerveDrive.swerveController.wheelUnits[index].getHeading());
            telemetry.addData(index+"Speed",swerveDrive.swerveController.wheelUnits[index].getSpeed());
        }
        telemetry.addData("AutoLockHeading",swerveDrive.swerveController.getAutoLockHeading());
        telemetry.addData("NoHeadMode",swerveDrive.swerveController.getUseNoHeadMode());
        telemetry.addData("x,y", RobotPosition.getInstance().getData().getPosition(DistanceUnit.INCH).toString());
        telemetry.addData("heading", RobotPosition.getInstance().getData().headingRadian);
        telemetry.addData("targetHeading",swerveDrive.swerveController.getHeadingLockRadian());
        telemetry.update();
        TelemetryPacket packet = new TelemetryPacket();
        packet.fieldOverlay().setStroke("#3F51B5");
        Drawing.drawRobot(packet.fieldOverlay(), RobotPosition.getInstance().getData().getPose2d());
        FtcDashboard.getInstance().sendTelemetryPacket(packet);
    }

}
