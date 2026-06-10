package org.firstinspires.ftc.teamcode.controllers;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.utility.Math.MathSolver;

public interface AngleSensor {
    public double getVoltage();
    public double getRadian();
}
