package org.firstinspires.ftc.teamcode.utility;

import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.controllers.swerve.locate.Robot;

public class VoltageOut {
    private long lastGetVoltageTime = 0;
    public static long voltageUpdateInterval = 50; // Update voltage every 100 ms
    private double currentVoltage = 0;
    public VoltageOut(){

    }
    public double getVoltage(){
        long currentTime = System.currentTimeMillis();
        if(currentTime - lastGetVoltageTime > voltageUpdateInterval){
            currentVoltage = Robot.getInstance().getData_Voltage().getVoltage();
            lastGetVoltageTime = currentTime;
        }
        return currentVoltage;
    }

    /**
     * 根据目标电压和当前电压计算输出功率，在电压过低时安全停止
     * 输入范围(理论，如果超过电池电压就达不到，输出占空比为1)：[-14.0，14.0]输出范围：[-1.0，1.0]
     * @param targetVoltage
     * @return power
     */
    public double getVoltageOutPower(double targetVoltage){
        currentVoltage = getVoltage();
        if (currentVoltage <= 6) {
            // 防护：读数异常或几乎没电，直接安全停止或降级
            return(0);
        } else {
            double power = targetVoltage / currentVoltage;
            power = Range.clip(power, -1.0, 1.0);
            return(power);
        }
    }

}
