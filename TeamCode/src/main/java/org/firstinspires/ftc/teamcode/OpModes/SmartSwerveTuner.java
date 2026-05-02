package org.firstinspires.ftc.teamcode.OpModes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;
import org.firstinspires.ftc.teamcode.controllers.swerve.SwerveDrive;
import org.firstinspires.ftc.teamcode.utility.Math.Line;
import org.firstinspires.ftc.teamcode.utility.Math.MathSolver;
import org.firstinspires.ftc.teamcode.utility.Math.Point2D;

import java.util.ArrayList;
import java.util.List;

@Config
@TeleOp
public class SmartSwerveTuner extends LinearOpMode {
    public static double AccelerationThreshold = 1;
    public static double SpeedThreshold = 200;
    public  static  double minVEL = 1;
    public static double minOMEGA = 0.5;
    List<Point2D> point2Ds_SV_LF = new ArrayList<>();
    List<Point2D> point2Ds_SV_RF = new ArrayList<>();
    List<Point2D> point2Ds_SV_LB = new ArrayList<>();
    List<Point2D> point2Ds_SV_RB = new ArrayList<>();

    @Override
    public void runOpMode() throws InterruptedException {
        
        BNO055IMU imu = hardwareMap.get(BNO055IMU.class, "imu");
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        imu.initialize(parameters);
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        SwerveDrive swerveDrive = new SwerveDrive(hardwareMap);
        waitForStart();
        while(opModeIsActive()){
            swerveDrive.swerveController.gamepadInput(40 * gamepad1.left_stick_x,-40*gamepad1.left_stick_y,-20*gamepad1.right_stick_x);
            swerveDrive.resetSVA();
            

            Acceleration acceleration = imu.getLinearAcceleration();
            double currentAcceleration = Math.sqrt(acceleration.xAccel * acceleration.xAccel + acceleration.yAccel * acceleration.yAccel + acceleration.zAccel * acceleration.zAccel);
            Velocity currentVelocity = imu.getVelocity();
            double ABSvelocity = Math.sqrt(currentVelocity.xVeloc * currentVelocity.xVeloc + currentVelocity.yVeloc * currentVelocity.yVeloc + currentVelocity.zVeloc * currentVelocity.zVeloc);

            AngularVelocity currentAngularVelocity = imu.getAngularVelocity();
            double ABSOmega = Math.abs(currentAngularVelocity.xRotationRate);
            telemetry.addData("currentVelocity", currentVelocity);
            telemetry.addData("ABSvelocity", ABSvelocity);

            telemetry.addData("currentAngularVelocity", currentAngularVelocity);
            telemetry.addData("ABSOmega", ABSOmega);

            telemetry.addData("Current Acceleration", currentAcceleration);
            double lfSpeed = swerveDrive.swerveController.wheelUnits[0].getSpeed();
            double lfVoltage = swerveDrive.swerveController.wheelUnits[0].getVoltage();
            double rfSpeed = swerveDrive.swerveController.wheelUnits[1].getSpeed();
            double rfVoltage = swerveDrive.swerveController.wheelUnits[1].getVoltage();
            double lbSpeed = swerveDrive.swerveController.wheelUnits[2].getSpeed();
            double lbVoltage = swerveDrive.swerveController.wheelUnits[2].getVoltage();
            double rbSpeed = swerveDrive.swerveController.wheelUnits[3].getSpeed();
            double rbVoltage = swerveDrive.swerveController.wheelUnits[3].getVoltage();
            telemetry.addData("leftFront Speed", lfSpeed);
            telemetry.addData("leftFront Voltage", lfVoltage);
            telemetry.addData("rightFront Speed", rfSpeed);
            telemetry.addData("rightFront Voltage", rfVoltage);
            telemetry.addData("leftBack Speed", lbSpeed);
            telemetry.addData("leftBack Voltage", lbVoltage);
            telemetry.addData("rightBack Speed", rbSpeed);
            telemetry.addData("rightBack Voltage", rbVoltage);
            if(currentAcceleration < AccelerationThreshold && (ABSvelocity> minVEL || ABSOmega > minOMEGA)) {

                if(lfSpeed > SpeedThreshold) {
                    point2Ds_SV_LF.add(new Point2D(lfSpeed, lfVoltage));
                }


                if(rfSpeed > SpeedThreshold) {
                    point2Ds_SV_RF.add(new Point2D(rfSpeed, rfVoltage));
                }


                if(lbSpeed > SpeedThreshold) {
                    point2Ds_SV_LB.add(new Point2D(lbSpeed, lbVoltage));
                }


                if(rbSpeed > SpeedThreshold) {
                    point2Ds_SV_RB.add(new Point2D(rbSpeed, rbVoltage));
                }
            }

            if(point2Ds_SV_LF.size() >= 2) {
                telemetry.addData("count", point2Ds_SV_LB.size());
                Line lineLF = MathSolver.fitLine(point2Ds_SV_LF);
                telemetry.addData("leftFront kS", lineLF.getIntercept());
                telemetry.addData("leftFront kV", lineLF.getSlope());
                telemetry.addData("leftFront R2", lineLF.getRSquared());

                Line lineRF =MathSolver.fitLine(point2Ds_SV_RF);
                telemetry.addData("rightFront kS", lineRF.getIntercept());
                telemetry.addData("rightFront kV", lineRF.getSlope());
                telemetry.addData("rightFront R2", lineRF.getRSquared());

                Line lineLB = MathSolver.fitLine(point2Ds_SV_LB);
                telemetry.addData("leftBack kS", lineLB.getIntercept());
                telemetry.addData("leftBack kV", lineLB.getSlope());
                telemetry.addData("leftBack R2", lineLB.getRSquared());

                Line lineRB = MathSolver.fitLine(point2Ds_SV_RB);
                telemetry.addData("rightBack kS", lineRB.getIntercept());
                telemetry.addData("rightBack kV", lineRB.getSlope());
                telemetry.addData("rightBack R2", lineRB.getRSquared());
            }
            telemetry.update();
        }
    }
}
