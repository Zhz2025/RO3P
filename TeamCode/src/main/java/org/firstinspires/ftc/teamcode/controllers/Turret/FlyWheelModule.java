package org.firstinspires.ftc.teamcode.controllers.Turret;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.utility.PIDSVA.PIDSVAController;
import org.firstinspires.ftc.teamcode.utility.PIDSVA.SlotConfig;
import org.firstinspires.ftc.teamcode.utility.VoltageOut;

@Config
public class FlyWheelModule {
    /**
     * 比例系数
     */
    public static double kP = 0.1;
    /**
     * 积分系数
     */
    public static double kI = 0.0;
    /**
     * 微分系数
     */
    public static double kD = 0.0;
    /**
     * 积分上限
     */
    public static double maxI = 1.0;
    /**
     * 静态摩擦系数
     */
    public static double kS = -0.2;
    /**
     * 速度系数
     */
    public static double kV = 0.0055;
    /**
     * 加速度系数
     */
    public static double kA = 0.0;
    /**
     * 输出电压最小值
     */
    public static double outputMin = -14.0;
    /**
     * 输出电压最大值
     */
    public static double outputMax = 14.0;

    public static double VelocityTolerance = 20;
    // --- Simple LPF (NO RC math) ---
    public static double alpha = 1; // tune 0.1–0.25
    /**
     * 电机实例
     */
    private final DcMotorEx motorL;
    private final DcMotorEx motorR;
    /**
     * 电压输出控制器
     */
    private final VoltageOut voltageOut;
    /**
     * PIDSVAController 实例
     */
    private final PIDSVAController controller;
    /**
     * 控制器配置
     */
    private final SlotConfig config;
    /**
     * 遥测实例
     */
    private final Telemetry telemetry;
    /**
     * 目标速度
     */
    private double targetVelocity = 0;
    /**
     * 上次更新时间
     */
    private long lastUpdateTime = 0;

    private double rawVelocity = 0;
    private double currentVelocity = 0;
    private double outputVoltage = 0;
    private double power = 0;

    public FlyWheelModule(HardwareMap hardwareMap, Telemetry telemetryrc) {
        this.motorL = hardwareMap.get(DcMotorEx.class, "FlyWheelL");
        this.motorR = hardwareMap.get(DcMotorEx.class, "FlyWheelR");


        // 配置电机
        this.motorL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.motorL.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        this.motorL.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.FLOAT);

        this.motorR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.motorR.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        this.motorR.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.FLOAT);

        this.motorL.setDirection(DcMotorEx.Direction.FORWARD);

        this.motorR.setDirection(DcMotorEx.Direction.REVERSE);

        // 初始化电压输出控制器
        this.voltageOut = new VoltageOut();

        // 初始化控制器配置
        this.config = new SlotConfig()
                .withKP(kP).withKI(kI).withKD(kD).withMaxI(maxI)
                .withKS(kS).withKV(kV).withKA(kA)
                .withOutputLimits(outputMin, outputMax);
        // 初始化PIDSVAController
        this.controller = new PIDSVAController().withSlot0(config);

        this.telemetry = telemetryrc;

    }

    public boolean setTargetSpeed(int targetSpeed) {
        this.targetVelocity = targetSpeed;
        return reachedVelocity();
    }

    public boolean reachedVelocity() {
        return (Math.abs(rawVelocity - targetVelocity) < VelocityTolerance);//&&shooterR.reachedTarget();
    }

    public void update() {
        //double不能==0！！！！！！
        if (targetVelocity < 10.0) {
            //绕过计算直接停电机。更加保险
            motorL.setPower(0);
            motorR.setPower(0);
            telemetry.addData("TargetVelocity", targetVelocity);
            telemetry.addData("CurrentVelocity", rawVelocity);
            telemetry.addData("OutputVoltage", 0);
            telemetry.addData("Power", 0);
            return;
        }


        //TODO 检查功率。尤其是高速情况，看看有没有必要bang-bang

        config.withKP(kP).withKI(kI).withKD(kD).withMaxI(maxI)
                .withKS(kS).withKV(kV).withKA(kA)
                .withOutputLimits(outputMin, outputMax);
        controller.resetSlot(config);

        long now = System.currentTimeMillis();
        double dt = lastUpdateTime == 0 ? 0.02 : (now - lastUpdateTime) / 1000.0;
        lastUpdateTime = now;
        rawVelocity = motorL.getVelocity();
        currentVelocity += alpha * (rawVelocity - currentVelocity);
        outputVoltage = controller.calculate(targetVelocity, currentVelocity, dt, true);
        power = voltageOut.getVoltageOutPower(outputVoltage);

        motorL.setPower(power);
        motorR.setPower(power);

        telemetry.addData("TargetVelocity", targetVelocity);
        telemetry.addData("RawVelocity", rawVelocity);
        telemetry.addData("CurrentVelocity", currentVelocity);
        telemetry.addData("OutputVoltage", outputVoltage);
        telemetry.addData("Power", power);
    }

    public double getCurrentVelocity() {
        return rawVelocity;
    }

    public double getPower() {
        return power;
    }

    public double getOutputVoltage() {
        return outputVoltage;
    }
}