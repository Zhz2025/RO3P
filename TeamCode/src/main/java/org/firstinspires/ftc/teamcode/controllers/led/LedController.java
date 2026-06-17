package org.firstinspires.ftc.teamcode.controllers.led;


import android.graphics.ColorSpace;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchDevice;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.I2cDeviceType;

/**
 * 自定义 I2C LED 控制器（Arduino 从机）
 * 通信协议：4 字节命令
 *   0x01 设置图案 (0~99)
 *   0x02 设置 Color1 (R,G,B)
 *   0x03 设置 Color2 (R,G,B)
 *   0x04 设置亮度 (0~255)
 */
@Config
@SuppressWarnings({"WeakerAccess", "unused"}) // Ignore access and unused warnings
// Both driver classes cannot register the sensor at the same time. One driver should have the
// sensor registered, and the other should be commented out
@I2cDeviceType
@DeviceProperties(name = "Arduino WS2812B Controller", description = "an arduino led controller", xmlTag = "UNO3")
public class LedController extends I2cDeviceSynchDevice<I2cDeviceSynch> {

    // I2C 地址（7位），0x08
    private static final I2cAddr I2C_ADDRESS = I2cAddr.create7bit(0x08);

    // 当前缓存值
    private int currentPattern = 99;
    private int currentBrightness = 255;
    private int[] currentColor1 = new int[]{255, 0, 0};
    private int[] currentColor2 = new int[]{0, 0, 255};

    private static final int REPEAT_THRESHOLD = 3; // 重复调用多少次后强制发送

    // 各参数的重复调用计数
    private int patternRepeatCount = 0;
    private int brightnessRepeatCount = 0;
    private int color1RepeatCount = 0;
    private int color2RepeatCount = 0;



    /**
     * 构造函数，由硬件映射自动调用
     * @param deviceClient 从硬件映射获取的 I2cDeviceSynch 对象
     */
    public LedController(I2cDeviceSynch deviceClient) {
        super(deviceClient, true);  // true 表示本类拥有 deviceClient 所有权（关闭时释放）
        this.deviceClient.setI2cAddress(I2C_ADDRESS);
        // 关闭写合并，保证每条命令立即发送
        this.deviceClient.enableWriteCoalescing(false);
        // 注册回调，在设备就绪时自动初始化（可选）
        registerArmingStateCallback(false);
    }

    @Override
    protected boolean doInitialize() {
        setPattern(99);
        patternRepeatCount = REPEAT_THRESHOLD;
        brightnessRepeatCount = REPEAT_THRESHOLD;
        color1RepeatCount = REPEAT_THRESHOLD;
        color2RepeatCount = REPEAT_THRESHOLD;
        return true;
    }

    @Override
    public Manufacturer getManufacturer() {
        return Manufacturer.Other;
    }

    @Override
    public String getDeviceName() {
        return "Arduino WS2812B Controller";
    }

    private void sendPattern(int pattern) {
        byte[] data = {(byte)0x01, (byte)pattern, 0, 0};
        deviceClient.write(data);
        currentPattern = pattern;
    }

    private void sendColor1(int r, int g, int b) {
        byte[] data = {(byte)0x02, (byte)r, (byte)g, (byte)b};
        deviceClient.write(data);
        currentColor1 = new int[]{r, g, b};
    }

    private void sendColor2(int r, int g, int b) {
        byte[] data = {(byte)0x03, (byte)r, (byte)g, (byte)b};
        deviceClient.write(data);
        currentColor2 = new int[]{r, g, b};
    }
    private void sendBrightness(int brightness) {
        byte[] data = new byte[]{(byte)0x04, (byte)brightness, 0, 0};
        deviceClient.write(data);
        currentBrightness = brightness;
    }


    /**
     * 设置图案（0~99）
     */
    public void setPattern(int pattern) {
        if (pattern < 0 || pattern > 99) throw new IllegalArgumentException("Pattern must be 0..99");
        if (pattern == currentPattern) {
            // 与当前值相同，累计计数
            patternRepeatCount++;
            if (patternRepeatCount >= REPEAT_THRESHOLD) {
                sendPattern(pattern);
                patternRepeatCount = 0; // 发送后重置
            }
        } else {
            // 值发生变化，立即发送并重置计数
            sendPattern(pattern);
            patternRepeatCount = 0;
        }

    }
    public void setPattern(RevBlinkinLedDriver.BlinkinPattern pattern){
        setPattern(pattern.ordinal());
    }

    /**
     * 设置 Color1 (R,G,B)
     */
    public void setColor1(int r, int g, int b) {
        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255)
            throw new IllegalArgumentException("R,G,B must be 0..255");
        if (r == currentColor1[0] && g == currentColor1[1] && b == currentColor1[2]) {
            color1RepeatCount++;
            if (color1RepeatCount >= REPEAT_THRESHOLD) {
                sendColor1(r, g, b);
                color1RepeatCount = 0;
            }
        } else {
            sendColor1(r, g, b);
            color1RepeatCount = 0;
        }
    }

    /**
     * 设置 Color2 (R,G,B)
     */
    public void setColor2(int r, int g, int b) {
        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255)
            throw new IllegalArgumentException("R,G,B must be 0..255");
        if (r == currentColor2[0] && g == currentColor2[1] && b == currentColor2[2]) {
            color2RepeatCount++;
            if (color2RepeatCount >= REPEAT_THRESHOLD) {
                sendColor2(r, g, b);
                color2RepeatCount = 0;
            }
        } else {
            sendColor2(r, g, b);
            color2RepeatCount = 0;
        }
    }

    /**
     * 设置整体亮度 (0~255)
     */
    public void setBrightness(int brightness) {
        if (brightness < 0 || brightness > 255) throw new IllegalArgumentException("Brightness 0..255");
        if (brightness == currentBrightness) {
            brightnessRepeatCount++;
            if (brightnessRepeatCount >= REPEAT_THRESHOLD) {
                sendBrightness(brightness);
                brightnessRepeatCount = 0;
            }
        } else {
            sendBrightness(brightness);
            brightnessRepeatCount = 0;
        }
    }

    // 获取当前缓存值
    public int getCurrentPattern() { return currentPattern; }
    public int getCurrentBrightness() { return currentBrightness; }
    public int[] getCurrentColor1() { return currentColor1.clone(); }
    public int[] getCurrentColor2() { return currentColor2.clone(); }
}
