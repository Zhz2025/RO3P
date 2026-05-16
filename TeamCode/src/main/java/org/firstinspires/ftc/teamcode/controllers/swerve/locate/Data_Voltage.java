package org.firstinspires.ftc.teamcode.controllers.swerve.locate;

public class Data_Voltage {
    private Data_Voltage(){}
    @Override
    public String toString() {
        return String.format("Battery Voltage: %.2f V", voltage);
    }
    static Data_Voltage instance=new Data_Voltage();
    public static Data_Voltage getInstance(){return instance;}
    private double voltage = 0;

    public double getVoltage() {
        return voltage;
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }
}
