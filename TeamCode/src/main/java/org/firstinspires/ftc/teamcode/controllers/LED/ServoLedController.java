package org.firstinspires.ftc.teamcode.controllers.LED;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.ServoImplEx;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * 利用舵机端口PWM信号控制LED亮度的控制器。
 * 通过ServoImplEx硬件底层的PWM占空比来调节LED亮度：
 * - setPwmEnable()启用连续PWM模式，获得更高频率减少闪烁
 * - setPosition(0.0~1.0) 映射为不同的PWM占空比
 * - scaleRange()可自定义PWM脉冲宽度范围
 */
@Config
public class ServoLedController {

    /** LED效果枚举 */
    public enum LedEffect {
        SOLID,       // 常亮
        BREATHING,   // 呼吸灯
        BLINK,       // 闪烁
        FAST_BLINK   // 快速闪烁
    }

    private ServoImplEx ledServo;
    private Telemetry telemetry;

    // 当前状态
    private double currentBrightness = 0.0;      // 当前亮度 0.0~1.0
    private double targetBrightness = 0.0;       // 目标亮度
    private LedEffect currentEffect = LedEffect.SOLID;
    private boolean enabled = true;

    // 平滑过渡参数
    private double fadeSpeed = 2.0;              // 淡入淡出速度（每秒变化量）
    private long lastUpdateTime = 0;

    // 呼吸灯参数
    private double breathePhase = 0.0;           // 呼吸相位 0~2π
    public static double breathePeriod = 3.0;    // 呼吸周期（秒）

    // 闪烁参数
    private double blinkPhase = 0.0;
    public static double blinkInterval = 0.5;    // 闪烁间隔（秒）
    public static double fastBlinkInterval = 0.15;// 快速闪烁间隔（秒）

    // PWM范围配置（µs）
    public static double minPulseWidth = 500.0;  // 最小脉冲宽度（对应亮度0）
    public static double maxPulseWidth = 2500.0; // 最大脉冲宽度（对应亮度1）
    /**
     * 构造函数（带Telemetry）
     * @param hardwareMap 硬件映射
     * @param telemetry   遥测对象（可为null）
     */
    public ServoLedController(HardwareMap hardwareMap, Telemetry telemetry, String name) {
        this.ledServo = hardwareMap.get(ServoImplEx.class, name);
        this.telemetry = telemetry;

        // 启用连续PWM模式（获得更高PWM频率，减少LED闪烁）
        ledServo.setPwmEnable();

        this.lastUpdateTime = System.nanoTime();
        setBrightness(0.0);
    }

    // ==================== 亮度控制 ====================

    /**
     * 设置LED亮度（立即生效）
     * @param brightness 亮度值 0.0~1.0（0=关闭，1=最亮）
     */
    public void setBrightness(double brightness) {
        this.targetBrightness = clamp(brightness, 0.0, 1.0);
        this.currentBrightness = this.targetBrightness;
        this.currentEffect = LedEffect.SOLID;
        applyBrightness(this.targetBrightness);
    }

    /**
     * 平滑过渡到目标亮度
     * @param brightness 目标亮度 0.0~1.0
     */
    public void fadeTo(double brightness) {
        this.targetBrightness = clamp(brightness, 0.0, 1.0);
        this.currentEffect = LedEffect.SOLID;
    }

    /**
     * 获取当前亮度
     */
    public double getBrightness() {
        return currentBrightness;
    }

    /**
     * 获取目标亮度
     */
    public double getTargetBrightness() {
        return targetBrightness;
    }

    // ==================== 特效控制 ====================

    /**
     * 设置呼吸灯效果（自动循环明暗变化）
     */
    public void startBreathing() {
        this.currentEffect = LedEffect.BREATHING;
        this.breathePhase = 0.0;
    }

    /**
     * 设置闪烁效果
     */
    public void startBlink() {
        this.currentEffect = LedEffect.BLINK;
        this.blinkPhase = 0.0;
    }

    /**
     * 设置快速闪烁效果
     */
    public void startFastBlink() {
        this.currentEffect = LedEffect.FAST_BLINK;
        this.blinkPhase = 0.0;
    }

    /**
     * 停止所有特效，保持当前亮度常亮
     */
    public void stopEffect() {
        this.currentEffect = LedEffect.SOLID;
    }

    /**
     * 获取当前特效
     */
    public LedEffect getCurrentEffect() {
        return currentEffect;
    }

    // ==================== 开关控制 ====================

    /**
     * 打开LED（恢复之前的目标亮度）
     */
    public void turnOn() {
        this.enabled = true;
        if (currentEffect == LedEffect.SOLID) {
            fadeTo(targetBrightness > 0 ? targetBrightness : 1.0);
        }
    }

    /**
     * 关闭LED
     */
    public void turnOff() {
        this.enabled = false;
        setBrightness(0.0);
    }

    /**
     * 切换开关状态
     */
    public void toggle() {
        if (enabled) {
            turnOff();
        } else {
            turnOn();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    // ==================== 配置 ====================

    /**
     * 设置淡入淡出速度
     * @param speed 每秒亮度变化量（默认2.0，即0.5秒完成全范围过渡）
     */
    public void setFadeSpeed(double speed) {
        this.fadeSpeed = Math.max(speed, 0.1);
    }

    /**
     * 设置PWM范围（高级用法，通常不需要修改）
     * @param minUs 最小脉冲宽度（µs）
     * @param maxUs 最大脉冲宽度（µs）
     */
    public void setPwmRange(double minUs, double maxUs) {
        ledServo.scaleRange(minUs / 1000.0, maxUs / 1000.0);
    }

    // ==================== 周期性更新 ====================

    /**
     * 需要在每个loop周期中调用此方法以驱动特效和平滑过渡
     */
    public void update() {
        long now = System.nanoTime();
        double dt = (now - lastUpdateTime) / 1_000_000_000.0; // 转换为秒
        lastUpdateTime = now;

        // 限制最大dt防止跳帧导致的计算异常
        if (dt > 0.1) dt = 0.1;
        if (dt <= 0) dt = 0.001;

        if (!enabled) {
            applyBrightness(0.0);
            addTelemetry();
            return;
        }

        switch (currentEffect) {
            case SOLID:
                updateSolid(dt);
                break;
            case BREATHING:
                updateBreathing(dt);
                break;
            case BLINK:
                updateBlink(dt, blinkInterval);
                break;
            case FAST_BLINK:
                updateBlink(dt, fastBlinkInterval);
                break;
        }

        addTelemetry();
    }

    private void updateSolid(double dt) {
        // 平滑过渡到目标亮度
        double diff = targetBrightness - currentBrightness;
        double step = fadeSpeed * dt;

        if (Math.abs(diff) <= step) {
            currentBrightness = targetBrightness;
        } else {
            currentBrightness += Math.signum(diff) * step;
        }

        applyBrightness(currentBrightness);
    }

    private void updateBreathing(double dt) {
        // 使用正弦波实现呼吸灯效果: brightness = (sin(phase) + 1) / 2
        breathePhase += (2.0 * Math.PI / breathePeriod) * dt;
        if (breathePhase > 2.0 * Math.PI) {
            breathePhase -= 2.0 * Math.PI;
        }

        double brightness = (Math.sin(breathePhase) + 1.0) / 2.0;
        currentBrightness = brightness;
        applyBrightness(brightness);
    }

    private void updateBlink(double dt, double interval) {
        blinkPhase += dt;
        if (blinkPhase >= interval) {
            blinkPhase -= interval;
        }

        // 前半周期亮，后半周期灭
        double brightness = (blinkPhase < interval * 0.5) ? targetBrightness : 0.0;
        currentBrightness = brightness;
        applyBrightness(brightness);
    }

    // ==================== 底层PWM输出 ====================

    /**
     * 将亮度值写入舵机端口（映射为PWM占空比）
     * @param brightness 0.0~1.0
     */
    private void applyBrightness(double brightness) {
        // Servo.setPosition() 设置PWM脉宽：0.0→最小脉宽，1.0→最大脉宽
        // 对于LED控制，亮度越高→脉宽越大→LED越亮
        ledServo.setPosition(clamp(brightness, 0.0, 1.0));
    }

    // ==================== 遥测 ====================

    private void addTelemetry() {
        if (telemetry != null) {
            telemetry.addData("LED Brightness", String.format("%.2f", currentBrightness));
            telemetry.addData("LED Effect", currentEffect.toString());
            telemetry.addData("LED Enabled", enabled);
        }
    }

    // ==================== 工具方法 ====================

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
