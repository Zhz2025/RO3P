package org.firstinspires.ftc.teamcode.OpModes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.Library.Team4410.MotionProfiler;
import org.firstinspires.ftc.teamcode.controllers.Turret.TurretModule;

/**
 * 测试用 OpMode：在 FTC Dashboard 中可调参数，实时输出运动曲线的位置、速度、加速度。
 *
 * 前提条件：
 * 1. 已正确安装 FTC Dashboard（需包含在项目中）。
 * 2. 使用优化版的 MotionProfiler（支持加减速独立最大加速度）。
 *
 * 使用方式：
 * - 连接 Dashboard 后，在 “Config” 界面会看到 ProfileConfig 下的可调变量。
 * - 在 OpMode 运行期间修改参数，曲线会立即重置并从新起点开始运行。
 * - telemetry 中会显示当前曲线的期望位置、速度、加速度以及参数状态。
 */
@TeleOp(name = "Motion Profiler Test", group = "Test")
public class MotionProfilerTest extends LinearOpMode {
    DcMotorEx motor;

    // Dashboard 可调参数类：
    @Config
    public static class ProfileConfig {
        // 最大速度（正值，单位与你后续使用的距离单位一致，例如 ticks/s）
        public static double maxVel = 1000.0;
        // 加速阶段最大加速度（正值）
        public static double maxAccel = 2000.0;
        // 减速阶段最大加速度（正值）
        public static double maxDecel = 2000.0;
        // 起点位置（与后续曲线计算中的单位相同）
        public static double startPos = 0.0;
        // 终点位置
        public static double targetPos = 5000.0;
    }

    @Override
    public void runOpMode() {
        // 同时向 Driver Station 和 FTC Dashboard 发送 telemetry
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        motor = hardwareMap.get(DcMotorEx.class, "turret");
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        // 运动曲线生成器（先使用默认参数，等待 start 后再第一次初始化）
        MotionProfiler profiler = new MotionProfiler(
                ProfileConfig.maxVel,
                ProfileConfig.maxAccel,
                ProfileConfig.maxDecel
        );

        // 记录上一次的参数值，用于检测变化
        double lastMaxVel = ProfileConfig.maxVel;
        double lastMaxAccel = ProfileConfig.maxAccel;
        double lastMaxDecel = ProfileConfig.maxDecel;
        double lastStartPos = ProfileConfig.startPos;
        double lastTargetPos = ProfileConfig.targetPos;

        // 运动曲线的起始时刻（秒）
        double profileStartTime = 0.0;
        boolean needReInit = true;  // 是否需要重新初始化曲线

        telemetry.addLine("Waiting for start...");
        telemetry.update();

        waitForStart();

        // 记录 OpMode 整体开始时间，用于计算每条曲线的相对时间
        ElapsedTime runtime = new ElapsedTime();
        runtime.reset();

        while (opModeIsActive()) {
            // ---------- 检测 Dashboard 参数变化 ----------
            if (ProfileConfig.maxVel != lastMaxVel ||
                    ProfileConfig.maxAccel != lastMaxAccel ||
                    ProfileConfig.maxDecel != lastMaxDecel ||
                    ProfileConfig.startPos != lastStartPos ||
                    ProfileConfig.targetPos != lastTargetPos) {

                // 更新本地备份
                lastMaxVel = ProfileConfig.maxVel;
                lastMaxAccel = ProfileConfig.maxAccel;
                lastMaxDecel = ProfileConfig.maxDecel;
                lastStartPos = ProfileConfig.startPos;
                lastTargetPos = ProfileConfig.targetPos;

                // 用新参数重新创建 MotionProfiler
                profiler = new MotionProfiler(lastMaxVel, lastMaxAccel, lastMaxDecel);
                needReInit = true;
            }

            // ---------- 需要重新初始化曲线 ----------
            if (needReInit) {
                profiler.init_new_profile(ProfileConfig.startPos, ProfileConfig.targetPos);
                profileStartTime = runtime.seconds();
                needReInit = false;
            }

            // ---------- 计算当前时刻的期望状态 ----------
            double currentDt = runtime.seconds() - profileStartTime;
            double targetPosition = profiler.motion_profile_pos(currentDt);
            double targetVelocity = profiler.motion_profile_vel(currentDt);
//            motor.setPower(targetVelocity);
//            telemetry.addData("pos in ticks", motor.getCurrentPosition());
//            telemetry.addData("pos in degree", TurretModule.tickToDegree(motor.getCurrentPosition()));
//            telemetry.addData("vel", motor.getVelocity());
//            telemetry.addData("current", motor.getCurrent(CurrentUnit.AMPS));
            double targetAccel = profiler.motion_profile_accel(currentDt);

            // ---------- 显示信息 ----------
            telemetry.addData("Status", profiler.isDone() ? "Done" : "Running");
            telemetry.addData("Time (s)", "%.3f", currentDt);
            telemetry.addData("Target Pos", "%.1f", targetPosition);
            telemetry.addData("Target Vel", "%.1f", targetVelocity);
            telemetry.addData("Target Accel", "%.1f", targetAccel);
            telemetry.addLine("");
            telemetry.addData("Max Vel", "%.1f", ProfileConfig.maxVel);
            telemetry.addData("Max Accel", "%.1f", ProfileConfig.maxAccel);
            telemetry.addData("Max Decel", "%.1f", ProfileConfig.maxDecel);
            telemetry.addData("Start Pos", "%.1f", ProfileConfig.startPos);
            telemetry.addData("Target Pos (config)", "%.1f", ProfileConfig.targetPos);
            telemetry.addData("Curve total time", "%.3f s", profiler.getEntire_dt());
            telemetry.update();

            // 如果曲线已经完成，不断输出最后的静止状态，同时仍然检测参数变化以允许重启新曲线
            if (profiler.isDone()) {
                // 可以主动将 needReInit 设为 true 以便下次参数变化时自动启动新曲线
                // 这里为了方便观察，不做自动重新初始化，用户修改参数后会自动触发
//                needReInit = true;
            }

            // 控制循环频率，避免占用过多 CPU
            sleep(10);
        }
    }
}