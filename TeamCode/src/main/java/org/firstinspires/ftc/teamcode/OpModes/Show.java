package org.firstinspires.ftc.teamcode.OpModes;

import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.controllers.LED.BlinkinLedController;
import org.firstinspires.ftc.teamcode.controllers.LED.ServoLedController;

@TeleOp(name = "LED Show", group = "Test")
public class Show extends LinearOpMode {
    private BlinkinLedController ledBelt;

    private ServoLedController led;
    private double manualBrightness = 0.5;

    @Override
    public void runOpMode() throws InterruptedException {
        led = new ServoLedController(hardwareMap, telemetry);
        ledBelt = new BlinkinLedController(hardwareMap);
        ledBelt.turnOff();

        telemetry.addLine("=== LED Show 手柄控制 ===");
        telemetry.addLine("A: 开关  |  B: 25%  |  X: 50%  |  Y: 100%");
        telemetry.addLine("DPAD上: 亮度+  |  DPAD下: 亮度-");
        telemetry.addLine("DPAD左: 呼吸灯  |  DPAD右: 闪烁");
        telemetry.addLine("LB: 快闪  |  RB: 常亮  |  LT: 渐灭  |  RT: 渐亮");
        telemetry.addLine("左摇杆Y: 手动调亮度");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            handleInput();
            led.update();
            addTelemetry();
            setledBelt();
        }
    }

    private void setledBelt() {
        if(gamepad2.aWasPressed()){
            ledBelt.setColor(RevBlinkinLedDriver.BlinkinPattern.BREATH_BLUE);
        }
        if(gamepad2.bWasPressed()){
            ledBelt.setColor(RevBlinkinLedDriver.BlinkinPattern.CP2_STROBE);
        }
        if(gamepad2.xWasPressed()){
            ledBelt.setColor(RevBlinkinLedDriver.BlinkinPattern.HEARTBEAT_RED);
        }
        if(gamepad2.yWasPressed()){
            ledBelt.setColor(RevBlinkinLedDriver.BlinkinPattern.BLUE_GREEN);
        }
    }

    private void handleInput() {
        // ---- A: 开关 ----
        if (gamepad1.aWasPressed()) {
            led.toggle();
        }

        // ---- B: 25% 亮度 ----
        if (gamepad1.bWasPressed()) {
            led.setBrightness(0.25);
        }

        // ---- X: 50% 亮度 ----
        if (gamepad1.xWasPressed()) {
            led.setBrightness(0.5);
        }

        // ---- Y: 100% 亮度 ----
        if (gamepad1.yWasPressed()) {
            led.setBrightness(1.0);
        }

        // ---- DPAD上: 亮度+0.1 ----
        if (gamepad1.dpadUpWasPressed()) {
            manualBrightness = Math.min(1.0, led.getTargetBrightness() + 0.1);
            led.setBrightness(manualBrightness);
        }

        // ---- DPAD下: 亮度-0.1 ----
        if (gamepad1.dpadDownWasPressed()) {
            manualBrightness = Math.max(0.0, led.getTargetBrightness() - 0.1);
            led.setBrightness(manualBrightness);
        }

        // ---- DPAD左: 呼吸灯 ----
        if (gamepad1.dpadLeftWasPressed()) {
            led.startBreathing();
        }

        // ---- DPAD右: 闪烁 ----
        if (gamepad1.dpadRightWasPressed()) {
            led.startBlink();
        }

        // ---- LB: 快速闪烁 ----
        if (gamepad1.leftBumperWasPressed()) {
            led.startFastBlink();
        }

        // ---- RB: 停止特效（常亮） ----
        if (gamepad1.rightBumperWasPressed()) {
            led.stopEffect();
        }

        // ---- LT: 平滑渐灭到0 ----
        if (gamepad1.left_trigger > 0.3) {
            led.fadeTo(0.0);
        }

        // ---- RT: 平滑渐亮到1 ----
        if (gamepad1.right_trigger > 0.3) {
            led.fadeTo(1.0);
        }

        // ---- 左摇杆Y: 手动调亮度 ----
        double stickY = -gamepad1.left_stick_y; // 上推为正
        if (Math.abs(stickY) > 0.05) {
            manualBrightness = (stickY + 1.0) / 2.0; // [-1,1] -> [0,1]
            led.setBrightness(manualBrightness);
        }
    }

    private void addTelemetry() {
        telemetry.addLine("=== LED Show ===");
        telemetry.addData("状态", led.isEnabled() ? "开启" : "关闭");
        telemetry.addData("当前亮度", String.format("%.2f", led.getBrightness()));
        telemetry.addData("目标亮度", String.format("%.2f", led.getTargetBrightness()));
        telemetry.addData("当前特效", led.getCurrentEffect().toString());
        telemetry.addLine("-----------------------------");
        telemetry.addLine("A:开关 B:25% X:50% Y:100%");
        telemetry.addLine("DPAD: 上+ / 下- / 左呼吸 / 右闪烁");
        telemetry.addLine("LB:快闪 RB:常亮 LT:渐灭 RT:渐亮");
        telemetry.update();
    }
}
