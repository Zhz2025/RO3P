package org.firstinspires.ftc.teamcode.controllers.intake;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.utility.VoltageOut;

@Config
public class IntakeController {
    public static class Params {
        double biteVoltage = 12.0;
        double swallowVoltage = 8.0;
        double omitVoltage = 6.0;
    }

    public static Params PARAMS = new Params();

    public IntakeController(HardwareMap hardwareMap, String deviceName){
        this.intakeMotor=hardwareMap.get(DcMotorEx.class, deviceName);
        intakeMotor.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        voltageOut = new VoltageOut(hardwareMap);
    }
    DcMotorEx intakeMotor;
    private VoltageOut voltageOut;

    /**
     * BITE: get the object into the robot
     * <p>
     * SWALLOW: send the object into the turret
     * <p>
     * SLEEP: stop the motor
     * <p>
     * OMIT: spit the object out
     *
     */
    public enum IntakeState{
        BITE, SWALLOW, SLEEP, OMIT;
        public IntakeState next(){
            return values()[(ordinal()+1)%values().length];
        }
    }
    private IntakeState intakeState = IntakeState.SLEEP;
    public IntakeState getIntakeState(){
        return intakeState;
    }
    public void setIntakeState(IntakeState intakeState){
        this.intakeState = intakeState;
    }
    public void update(){
        switch (intakeState){
            case BITE:
                intakeMotor.setPower(voltageOut.getVoltageOutPower(PARAMS.biteVoltage));
                break;
            case SWALLOW:
                intakeMotor.setPower(voltageOut.getVoltageOutPower(PARAMS.swallowVoltage));
                break;
            case SLEEP:
                intakeMotor.setPower(0);
                break;
            case OMIT:
                intakeMotor.setPower(-voltageOut.getVoltageOutPower(PARAMS.omitVoltage));
                break;
        }
    }
    public void stop(){
        intakeMotor.setPower(0);
    }
    public DcMotorEx getIntakeMotor(){
        return intakeMotor;
    }
}
