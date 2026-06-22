package org.firstinspires.ftc.teamcode.controllers.Turret;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.utility.PIDSVA.PIDController;
import org.firstinspires.ftc.teamcode.utility.PIDSVA.PIDSVAController;
import org.firstinspires.ftc.teamcode.utility.PIDSVA.SlotConfig;
import org.firstinspires.ftc.teamcode.utility.VoltageOut;

//为什么要用串级PID
//串级PID最大的优势在于抗干扰和对参数的适应能力，或者说能容忍硬件变化（如磨损）对参数的改变
//优势的实现的理论基础是内环的高响应速度，可以在扰动产生的瞬间就做出响应，消除扰动，在扰动传导到最终输出之前，就把它扼杀在了摇篮里
//同时，串级还可以简化外环控制的对象。单级PID，PID计算的输出是电机的输出功率，这和电机实际产生的速度是非线性的。内环可以把电机简化成一个速度与功率成正比的理想电机，这种职能的划分，减轻了外环的控制压力。
//在当前的ftc环境中，我们没有执行器端的微处理器，可以在电机上跑速度环，实现更高刷新率。故串级的第一个优势一定程度上失效了
//但其第二个优势—— 简化外环控制对象 仍然存在。这也方便了我们叠加车体旋转的速度补偿
@Config
public class TurretModule_Simplified {
    public static double testSpeed = 0;
    private boolean manualControl = false;
    private boolean velTestMode = false;
    private DcMotorEx turretMotor;
    private Telemetry myTelemetry;
    private VoltageOut myVoltageOut;
    private SlotConfig slot;
    //硬件配置
    // 角度限位,单位：° 以炮台初始位置为0°，逆时针为正，可旋转范围 ±180°
    public static double lowLimit = -180;
    public static double highLimit = 180;
    // 死区保护
    public static int encoderLimit = 500;
    public static double TicksForOneDegree = 2.43434569; // 每度对应的编码器计数，用于换算tick到实际角度

    private double targetDegree = 0; // 目标朝向，单位为度
    private double currentRobotDegree = 0; // 当前炮台基于机器人的朝向，单位为度
    private double currentFieldDegree = 0; // 当前炮台基于场地的朝向，单位为度
    private double currentTick = 0; // 当前编码器计数
    private double currentVelocity = 0;


    // 控制参数
    public static double positionKp = 20  ; // 位置环P
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
    public static double Ks = 1.41;
    public static double Kv = 0.0073;
    public static double Ka = 0.0;
    public static double vel_P = 0.03;
    public static double vel_I = 0;
    public static double vel_D = 0;
    public static double Min_Vel = 10;
    public static double Min_Power = 0.15;
    public void toggleControlMode(){
        manualControl = !manualControl;
    }
    public void setManualControl(){
        manualControl = true;
    }
    public void setAutoControl(){
        manualControl = false;
    }
    public void toggleVelTestMode(boolean mode){
        velTestMode = mode;
    }




    // 外部设置车体角速度补偿
    public void setRobotAngularVelocity(double omegaDegPerSec) {
        this.robotAngularVelocity = omegaDegPerSec;
    }

    // 编码器tick转角度
    public static double tickToDegree(double tick) {
        return tick / TicksForOneDegree;
    }
    // 角度转编码器tick
    public static double degreeToTick(double degree) {
        return degree * TicksForOneDegree;
    }

    // 将任意角度映射到有效连续区间 [lowLimit, highLimit) 内的唯一表示
    // 因为区间恰好是一整圈（360°），每个角度在此区间内只有一种合法位置
    private double mapToValidRange(double angle) {
        double result = angle;
        while (result < lowLimit) result += 360;
        while (result >= highLimit) result -= 360;
        return result;
    }


    public TurretModule_Simplified(HardwareMap hardwareMap, Telemetry telemetryRC) {
        // 初始化 炮台电机 的硬件，归零朝向
        turretMotor = hardwareMap.get(DcMotorEx.class, "turret");
        turretMotor.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        turretMotor.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        turretMotor.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        turretMotor.setDirection(DcMotorEx.Direction.FORWARD);
        myTelemetry = telemetryRC;

        myVoltageOut = new VoltageOut();

        targetDegree = 0;
        currentTick = 0;

        slot = new SlotConfig()
                .withKP(vel_P)
                .withKI(vel_I)
                .withKD(vel_D)
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

    /**
     ----------------[-180, 180)------------
     获取当前炮台基于机器人的朝向，单位为度
     */
    public double getCurrentRobotDegree(){
        return currentRobotDegree;
    }
    /**
     ----------------[-180, 180)------------
     获取当前炮台基于场地的朝向，单位为度
     */
    public double getCurrentFieldDegree(double RobotHeading){
        currentFieldDegree = (currentRobotDegree + RobotHeading) % 360;
        return currentFieldDegree;
    }
    public void update(){
        //重设PID SVA参数 *****请注意，不要通过构建新的SlotConfig对象来修改参数，用with()函数修改，否则会把其他参数（如OutputLimits）恢复到默认值
        slot.withKP(vel_P).withKI(vel_I).withKD(vel_D)
                .withKS(Ks).withKV(Kv).withKA(Ka);
        velocityController.resetSlot(slot);
        positionController.setPID(positionKp, positionKi, 0);
        // 死区保护：读取编码器，超限直接报错
        currentTick = turretMotor.getCurrentPosition();
        currentVelocity = turretMotor.getVelocity(AngleUnit.DEGREES);
//        if (currentTick > encoderLimit || currentTick < -encoderLimit) {
//            //只是为了保险，便于debug，且这不是比赛用程序。如果是为比赛准备，请不要直接抛报错
//            throw new RuntimeException("turret is beyond hardware limit. Reset turret and restart.");
//        }

        currentRobotDegree = tickToDegree(currentTick);
        // 确保当前位置在有效范围内
        currentRobotDegree = mapToValidRange(currentRobotDegree);

        // 目标映射到唯一合法连续位置——因为有效范围恰好 360°，
        // 每个角度在此区间内只有一种合法表示，不存在"选哪条路"的歧义
        targetDegree = mapToValidRange(targetDegree);
        //180会有bug，特殊处理一下
        if(Math.abs(targetDegree - 180) < 0.01 || Math.abs(targetDegree + 180) < 0.01){
            targetDegree = 179.99;
        }

        long nowTime = System.currentTimeMillis();


        double dt;
        if (lastUpdateTime == 0) {
            dt = 0.05;
        } else {
            dt = (nowTime - lastUpdateTime) / 1000.0;
        }
        lastUpdateTime = nowTime;
        double Velocity = 0;

        if(!velTestMode){
            Velocity = positionController.calculate(targetDegree, currentRobotDegree, dt);
        }
        else{
            Velocity = testSpeed;
        }

        //todo 叠加车体角速度补偿
        //double finalVelocitySetpoint = Velocity + robotAngularVelocity;
        double finalVelocitySetpoint = Velocity + 0;

        double voltage = velocityController.calculate(finalVelocitySetpoint, currentVelocity, dt, true);
        double power = myVoltageOut.getVoltageOutPower(voltage);
        //todo 重复/过小输出约束
        if(Math.abs(power) < Min_Power){
            power = 0;
        }
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
        myTelemetry.addData("targetVelocity", finalVelocitySetpoint);
    }

    public void setMotorPower(double power){
        if(manualControl){
            turretMotor.setPower(power);
        }
    }

}
