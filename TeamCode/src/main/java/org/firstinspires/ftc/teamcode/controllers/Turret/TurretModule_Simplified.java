package org.firstinspires.ftc.teamcode.controllers.Turret;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.Library.Team4410.MotionProfiler;
import org.firstinspires.ftc.teamcode.utility.PIDSVA.PIDController;
import org.firstinspires.ftc.teamcode.utility.PIDSVA.PIDSVAController;
import org.firstinspires.ftc.teamcode.utility.PIDSVA.SlotConfig;
import org.firstinspires.ftc.teamcode.utility.VoltageOut;

@Config
public class TurretModule_Simplified {
    private boolean manualControl = false;
    private DcMotorEx turretMotor;
    private Telemetry myTelemetry;
    private VoltageOut myVoltageOut;
    //硬件配置
    // 角度限位,单位：° 以机器的头为0°，逆时针为正
    public static double lowLimit = -180;
    public static double highLimit = 180;
    // 死区保护
    public static int encoderLimit = 1500;
    public static double DegreePerTick = 100; // 每度对应的编码器计数，需根据实际电机和齿轮比调整,用于换算tick到实际角度

    private double targetDegree = 0; // 目标朝向，单位为度
    private double currentRobotDegree = 0; // 当前炮台基于机器人的朝向，单位为度
    private double currentFieldDegree = 0; // 当前炮台基于场地的朝向，单位为度
    private double currentTick = 0; // 当前编码器计数
    private double currentVelocity = 0;


    // 控制参数
    public static double positionKp = 0.02; // 位置环P
    public static double positionKi = 0.0;  // 位置环I
    public static double positionMaxi = 50;//单位：°/s
    public static double postionIzone = 10;//单位：°
    private PIDController positionController = new PIDController(positionKp, positionKi, 0, positionMaxi, postionIzone);

    // 速度环PIDSVA控制器
    private PIDSVAController velocityController;

    // 车体角速度补偿
    private double robotAngularVelocity = 0.0; // 单位deg/s，需外部set
    // 运动规划起始时间
    private long profileStartTime = 0;

    // 用于dt计算
    private long lastUpdateTime = 0;

    //只配置前馈和P项
    public static double Ks = 0.0;
    public static double Kv = 0.0;
    public static double Ka = 0.0;
    public static double velGain = 1;
    //最大巡航速度，最大加/减速度
    public static double maxCruiseSpeed = 1000;
    public static double maxAccel = 200;
    public static double maxDccel = 200;
    public void toggleControlMode(){
        manualControl = !manualControl;
    }
    public void setManualControl(){
        manualControl = true;
    }
    public void setAutoControl(){
        manualControl = false;
    }




    // 外部设置车体角速度补偿
    public void setRobotAngularVelocity(double omegaDegPerSec) {
        this.robotAngularVelocity = omegaDegPerSec;
    }

    // 编码器tick转角度
    public static double tickToDegree(double tick) {
        return tick / DegreePerTick;
    }
    // 角度转编码器tick
    public static double degreeToTick(double degree) {
        return degree * DegreePerTick;
    }


    public TurretModule_Simplified(HardwareMap hardwareMap, Telemetry telemetryRC) {
        // 初始化 炮台电机 的硬件，归零朝向
        turretMotor = hardwareMap.get(DcMotorEx.class, "TurretMotor");
        turretMotor.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        turretMotor.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        turretMotor.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        turretMotor.setDirection(DcMotorEx.Direction.FORWARD);
        myTelemetry = telemetryRC;

        myVoltageOut = new VoltageOut(hardwareMap);

        targetDegree = 0;
        currentTick = 0;

        SlotConfig slot = new SlotConfig()
                .withKP(velGain)
                .withKI(0.0)
                .withKD(0.0)
                .withKS(Ks)
                .withKV(Kv)
                .withKA(Ka)
                .withOutputLimits(-14.0, 14.0);
        velocityController = new PIDSVAController().withSlot0(slot);
    }
    public void setTargetDegree(double degree){
        targetDegree = degree;
    }
    public void setTargetDegreeDelta(double delta){
        targetDegree += delta;
    }

    public double getCurrentRobotDegree(){
        // 获取当前炮台基于机器人的朝向，单位为度
        return currentRobotDegree;
    }
    public double getCurrentFieldDegree(double RobotHeading){
        currentFieldDegree = (currentRobotDegree + RobotHeading) % 360;
        // 获取当前炮台基于场地的朝向，单位为度
        return currentFieldDegree;
    }
    public void update(){
        //重设P SVA
        SlotConfig slot = new SlotConfig()
                .withKP(velGain)
                .withKS(Ks)
                .withKV(Kv)
                .withKA(Ka);
        velocityController.resetSlot(slot);
        // 死区保护：读取编码器，超限直接报错
        currentTick = turretMotor.getCurrentPosition();
        currentVelocity = turretMotor.getVelocity(AngleUnit.DEGREES);
        if (currentTick > encoderLimit || currentTick < -encoderLimit) {
            //只是为了保险，便于debug，且这不是比赛用程序。如果是为比赛准备，请不要直接抛报错
            throw new RuntimeException("turret is beyond hardware limit. Reset turret and restart.");
        }
        currentRobotDegree = tickToDegree(currentTick);

        //最短路径判断与限位
        targetDegree= (targetDegree % 360 + 360) % 360;
        //生成3个候选目标：直接值、+360°、-360°
        double[] candidates = {targetDegree, targetDegree + 360.0, targetDegree - 360.0};

        //在所有不超限的候选中，选择离当前位置最近的那个
        double bestTarget = 0;
        double minDistance = Double.MAX_VALUE;

        for (double candidate : candidates) {
            // 检查是否在软限位内
            if (candidate >= lowLimit && candidate <= highLimit) {
                double distance = Math.abs(candidate - currentRobotDegree);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestTarget = candidate;
                }
            }
        }
        if(minDistance == Double.MAX_VALUE){
            //同上
            throw new RuntimeException("illegal target");
        }
        else{
            targetDegree = bestTarget;
        }
        long nowTime = System.currentTimeMillis();

        //位置环PI控制（用PIDController实现）
        double dt;
        if (lastUpdateTime == 0) {
            dt = 0.05; // 首次调用假定50ms
        } else {
            dt = (nowTime - lastUpdateTime) / 1000.0;
        }
        lastUpdateTime = nowTime;
        double Velocity = positionController.calculate(targetDegree, currentRobotDegree, dt);

        //叠加车体角速度补偿
        double finalVelocitySetpoint = Velocity + robotAngularVelocity;
        double voltage = velocityController.calculate(finalVelocitySetpoint, currentVelocity, dt, true);
        double power = myVoltageOut.getVoltageOutPower(voltage);
        // 电机执行
        if(!manualControl){
            turretMotor.setPower(power);
        }


        //telemetry
        myTelemetry.addData("voltage", voltage);
        myTelemetry.addData("power", power);
        myTelemetry.addData("targetDegree", targetDegree);
        myTelemetry.addData("currentRobotDegree", currentRobotDegree);
        myTelemetry.addData("currentFieldDegree", currentFieldDegree);
        myTelemetry.addData("currentTick", currentTick);
        myTelemetry.addData("currentVelocity", currentVelocity);
    }

    public void setMotorPower(double power){
        if(manualControl){
            turretMotor.setPower(power);
        }
    }

}
