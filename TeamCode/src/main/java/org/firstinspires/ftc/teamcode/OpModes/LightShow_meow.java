package org.firstinspires.ftc.teamcode.OpModes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Constants;
import org.firstinspires.ftc.teamcode.RoadRunner.Localizer;
import org.firstinspires.ftc.teamcode.controllers.Turret.HoodModule;
import org.firstinspires.ftc.teamcode.controllers.Turret.TurretModule_Simplified;
import org.firstinspires.ftc.teamcode.controllers.led.I2CLedController;
import org.firstinspires.ftc.teamcode.controllers.led.ServoLedController;
import org.firstinspires.ftc.teamcode.controllers.swerve.locate.Robot;

@Config
@TeleOp
public class LightShow_meow extends LinearOpMode {
    TurretModule_Simplified myTurret;
    HoodModule myHood;
    private ServoLedController led;
    private ServoLedController led2;

    I2CLedController ledController;
    @Override
    public void runOpMode() throws InterruptedException {
        Robot.refresh(new Localizer() {
            @Override
            public void setPose(Pose2d pose) {

            }

            @Override
            public Pose2d getPose() {
                return new Pose2d(0,0,0);
            }

            @Override
            public PoseVelocity2d update() {
                return new PoseVelocity2d(new Vector2d(0,0),0);
            }
        },hardwareMap.voltageSensor.iterator().next());

        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
//        led = new ServoLedController(hardwareMap, telemetry, "ledBulb");
//        led2 = new ServoLedController(hardwareMap, telemetry, "ledBulb2");
        ledController = hardwareMap.get(I2CLedController.class,"ledMusicController");
//        led.turnOff();
//        led2.turnOff();

        myHood = new HoodModule(hardwareMap, telemetry);
        myTurret = new TurretModule_Simplified(hardwareMap, telemetry);

        waitForStart();

        // 记录开始时间，用于计算运行时长
        long startNanoTime = System.nanoTime();

        while (opModeIsActive()) {


            // === 线性插值表测试：基于系统运行时间打印预期的 t, degree, height ===
            double elapsedSeconds = (System.nanoTime() - startNanoTime) / 1e9;
            double interpolatedDegree = Constants.t_degree.interpolate(elapsedSeconds);
            double interpolatedHeight = Constants.t_height.interpolate(elapsedSeconds);

            telemetry.addData("=== 插值表测试 ===", "");
            telemetry.addData("当前运行时间 t (s)", "%.3f", elapsedSeconds);
            telemetry.addData("预期 degree (°)", "%.2f", interpolatedDegree);
            telemetry.addData("预期 height (m)", "%.4f", interpolatedHeight);

            telemetry.addData("CurrentMusicMode", ledController.getCurrentMusicMode().toString());
            telemetry.addData("MusicEnabled",ledController.isMusicEnabled());

            myTurret.setTargetDegree(interpolatedDegree);
            myHood.setPosition(interpolatedHeight);
            myTurret.update();
            myHood.update();

//            if(elapsedSeconds > 10){
//                led .turnOn();
//            }
//            if(elapsedSeconds > 20){
//                led2.turnOn();
//            }
            telemetry.addData("LightShow", "Meow!");
            telemetry.update();
        }
    }
}
