package org.firstinspires.ftc.teamcode.OpModes;

import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.RoadRunner.Localizer;
import org.firstinspires.ftc.teamcode.RoadRunner.PinpointLocalizer;
import org.firstinspires.ftc.teamcode.controllers.swerve.locate.Data_Position;
import org.firstinspires.ftc.teamcode.controllers.swerve.locate.Data_Voltage;
import org.firstinspires.ftc.teamcode.controllers.swerve.locate.Robot;

@TeleOp(name = "SensorDataTest")
public class SensorDataTest extends LinearOpMode {
    @Override
    public void runOpMode() {
        VoltageSensor voltageSensor = hardwareMap.voltageSensor.iterator().next();
        Localizer localizer = new PinpointLocalizer(hardwareMap, 0.00199, new Pose2d(0,0,0));
        Robot.refresh(localizer, voltageSensor);
        waitForStart();
        while (opModeIsActive()) {
            Robot.getInstance().update();
            Data_Position pos = Robot.getInstance().getData_Position();
            Data_Voltage volt = Robot.getInstance().getData_Voltage();
            telemetry.addData("Voltage", "%.2f V", volt.getVoltage());
            telemetry.update();
        }
    }
}