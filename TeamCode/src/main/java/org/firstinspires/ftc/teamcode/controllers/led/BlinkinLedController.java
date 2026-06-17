package org.firstinspires.ftc.teamcode.controllers.led;

import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class BlinkinLedController {


    public RevBlinkinLedDriver blinkinLedDriver;
    private RevBlinkinLedDriver.BlinkinPattern currentPattern;

    public BlinkinLedController(HardwareMap hardwareMap) {
        this.blinkinLedDriver  = hardwareMap.get(RevBlinkinLedDriver.class, "blinkin");
        this.currentPattern = RevBlinkinLedDriver.BlinkinPattern.BLACK;
        blinkinLedDriver.setPattern(currentPattern);
    }



    public void showRedTeam() {
        setPattern(RevBlinkinLedDriver.BlinkinPattern.RED);
    }

    public void showBlueTeam() {
        setPattern(RevBlinkinLedDriver.BlinkinPattern.BLUE);
    }

    public void showReady() {
        setPattern(RevBlinkinLedDriver.BlinkinPattern.GREEN);
    }

    public void showError() {
        setPattern(RevBlinkinLedDriver.BlinkinPattern.YELLOW);
    }


    /**
     * Set by enum preset
     */
    public void setPattern(RevBlinkinLedDriver.BlinkinPattern preset) {
        this.currentPattern = preset;
        blinkinLedDriver.setPattern(preset);
    }

    public void setNextPattern(){
        if (currentPattern != null) {
            currentPattern = currentPattern.next();
            blinkinLedDriver.setPattern(currentPattern);
        }
    }
    public void setPreviousPattern(){
        if (currentPattern != null) {
            currentPattern = currentPattern.previous();
            blinkinLedDriver.setPattern(currentPattern);
        }
    }

    public RevBlinkinLedDriver.BlinkinPattern getCurrentPattern() {
        return currentPattern;
    }

    /**
     * Returns the PWM pulse width in microseconds for the current pattern.
     * Range: 1105 μs (RAINBOW_RAINBOW_PALETTE) to 2095 μs (BLACK), step 10 μs.
     */
    public int getCurrentLength() {
        if (currentPattern == null) {
            return 0;
        }
        return 1105 + currentPattern.ordinal() * 10;
    }

    public void turnOff() {
        setPattern(RevBlinkinLedDriver.BlinkinPattern.BLACK);
    }
    /*
    固定颜色：RED BLUE GREEN YELLOW ORANGE WHITE BLACK  // 关闭
    闪烁效果：STROBE_RED STROBE_BLUE STROBE_GOLD STROBE_WHITE
    彩虹效果：RAINBOW_RAINBOW_PALETTE RAINBOW_PARTY_PALETTE
            RAINBOW_OCEAN_PALETTE   RAINBOW_FOREST_PALETTE
    心跳效果：HEARTBEAT_RED HEARTBEAT_BLUE HEARTBEAT_WHITE
    扫描效果：SINELON_RAINBOW SINELON_PARTY SINELON_OCEAN SINELON_FOREST
    流动效果：LARSON_SCANNER_RED LARSON_SCANNER_BLUE LARSON_SCANNER_GOLD
            LIGHT_CHASE_RED LI_BLUEGHT_CHASE LIGHT_CHASE_GOLD
    其他效果：CONFETTI FIREWORKS CPMETTI_SHOT FIRE_LARGE
            FIRE_MEDIUM FIRE_SMALL CANDLE FILLER SHOT_RED SHOT_BLUE
    */
}