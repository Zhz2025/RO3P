package org.firstinspires.ftc.teamcode.controllers.led;


import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchDevice;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.I2cDeviceType;

/**
 * 自定义 I2C LED 控制器（Arduino 从机 — 音乐模式专用）
 * 通信协议：4 字节命令
 *   0x01 设置图案 (0~99)
 *   0x02 设置 Color1 (R,G,B)
 *   0x03 设置 Color2 (R,G,B)
 *   0x04 设置亮度 (0~255)
 *   0x05 音乐模式:
 *         data[1] = 模式: 0 = BREATH (呼吸), 1 = FLOW (流光)
 *         data[2] = 使能: 0 = 启用, 1 = 禁用
 *         data[3] = 保留 (未使用)
 */
@Config
@SuppressWarnings({"WeakerAccess", "unused"}) // Ignore access and unused warnings
@I2cDeviceType
@DeviceProperties(name = "I2C Arduino LED Controller", description = "an arduino led controller using i2c", xmlTag = "ArduinoMini")
public class I2CLedController extends I2cDeviceSynchDevice<I2cDeviceSynch> {

    // ========================================================================
    // 音乐模式枚举
    // ========================================================================
    public enum MusicMode {
        BREATH, // 呼吸模式
        FLOW    // 流光模式
    }

    // I2C 地址（7位），0x08
    private static final I2cAddr I2C_ADDRESS = I2cAddr.create7bit(0x08);

    // 当前缓存值
    private MusicMode currentMode = MusicMode.FLOW;

    private boolean musicEnabled = false;




    /**
     * 构造函数，由硬件映射自动调用
     * @param deviceClient 从硬件映射获取的 I2cDeviceSynch 对象
     */
    public I2CLedController(I2cDeviceSynch deviceClient) {
        super(deviceClient, true);  // true 表示本类拥有 deviceClient 所有权（关闭时释放）
        this.deviceClient.setI2cAddress(I2C_ADDRESS);
        // 关闭写合并，保证每条命令立即发送
        this.deviceClient.enableWriteCoalescing(false);
        // 注册回调，在设备就绪时自动初始化（可选）
        registerArmingStateCallback(false);
    }

    @Override
    protected boolean doInitialize() {
        disable();
        musicEnabled = false;
        currentMode = MusicMode.FLOW;
        return true;
    }

    // ========================================================================
    // 音乐模式命令 (0x05)
    // ========================================================================

    /**
     * 发送音乐模式命令
     * @param mode  0 = BREATH, 1 = FLOW
     * @param enable 0 = 启用, 1 = 禁用
     */
    private void sendMusicMode(int mode, int enable) {
        byte[] data = {(byte)0x05, (byte)mode, (byte)enable, 0};
        deviceClient.write(data);
    }

    /**
     * 启用指定音乐模式（记录当前时刻为 startTime 并开始动画）
     * @param mode 音乐模式枚举（BREATH 或 FLOW）
     */
    public void enable(MusicMode mode) {
        int modeCode = (mode == MusicMode.FLOW) ? 1 : 0;
        sendMusicMode(modeCode, 0); // enable = 0 → 启用
        currentMode = mode;
        musicEnabled = true;
    }

    /**
     * 禁用音乐模式（LED 清零并保持暗灭）
     */
    public void disable() {
        sendMusicMode((currentMode == MusicMode.BREATH) ? 0 : 1, 1); // mode 忽略, enable = 1 → 禁用
        musicEnabled = false;
    }

    /**
     * 查询当前是否处于音乐模式
     */
    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    /**
     * 获取当前音乐模式（未启用时返回 null）
     */
    public MusicMode getCurrentMusicMode() {
        return currentMode;
    }

    @Override
    public Manufacturer getManufacturer() {
        return Manufacturer.Other;
    }

    @Override
    public String getDeviceName() {
        return "Arduino WS2812B Controller Music Edition";
    }

}
