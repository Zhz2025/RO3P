package org.firstinspires.ftc.teamcode.Library.Team4410;


/**
 * 运动曲线生成器，用于FTC机器人平滑运动控制。
 * 支持梯形速度曲线（加速、巡航、减速），加速与减速阶段可以设置不同的最大加速度。
 */
public class MotionProfiler {

    // 用户设定的全局最大速度和最大加速度（正值，代表大小）
    private final double maxVelocity;
    private final double maxAccel;   // 加速阶段最大加速度大小
    private final double maxDecel;   // 减速阶段最大加速度大小

    // 当前路径运行状态
    private boolean isOver = true;
    private boolean isDone = false;

    // 当前路径的运动参数
    private double startPos;
    private double finalPos;
    private double distance;         // 带符号的总位移
    private double sign;             // 运动方向符号：+1 或 -1

    // 各阶段的时间分界点（相对于运动开始的累计时间）
    private double tAccelEnd;        // 加速段结束时间
    private double tCruiseEnd;       // 巡航段结束时间（开始减速的时间）
    private double tTotal;           // 总运动时间

    // 运动峰值速度（带符号）
    private double peakVelocity;
    // 各阶段的加速度（带符号）
    private double accelAcc;         // 加速段加速度
    private double decelAcc;         // 减速段加速度

    // 各阶段位移（绝对值）
    private double sAccel;
    private double sCruise;
    private double sDecel;

    // ==================== 构造函数 ====================

    /**
     * 构造函数（加减速使用相同的最大加速度）
     * @param maxVelocity 最大速度（正值）
     * @param maxAcceleration 最大加速度（正值）
     */
    public MotionProfiler(double maxVelocity, double maxAcceleration) {
        this(maxVelocity, maxAcceleration, maxAcceleration);
    }

    /**
     * 构造函数（加减速使用不同的最大加速度）
     * @param maxVelocity     最大速度（正值）
     * @param maxAcceleration 加速阶段最大加速度（正值）
     * @param maxDeceleration 减速阶段最大加速度（正值）
     */
    public MotionProfiler(double maxVelocity, double maxAcceleration, double maxDeceleration) {
        this.maxVelocity = Math.abs(maxVelocity);
        this.maxAccel = Math.abs(maxAcceleration);
        this.maxDecel = Math.abs(maxDeceleration);
    }

    // ==================== 路径初始化 ====================

    /**
     * 在使用新路径前必须调用此方法，计算运动曲线参数。
     * @param startPos 起始位置
     * @param finalPos 终点位置
     */
    public void init_new_profile(double startPos, double finalPos) {
        this.startPos = startPos;
        this.finalPos = finalPos;
        isOver = false;
        isDone = false;

        distance = finalPos - startPos;
        sign = Math.signum(distance);
        double absDistance = Math.abs(distance);

        // 加速度方向由运动方向决定：加速时加速度与运动方向相同，减速时相反
        accelAcc = sign * maxAccel;
        decelAcc = -sign * maxDecel;

        // ---- 计算以最大速度运行所需的加速距离和减速距离 ----
        double sAccelMax = 0.5 * maxVelocity * maxVelocity / maxAccel; // 加速到maxVelocity所需的距离
        double sDecelMax = 0.5 * maxVelocity * maxVelocity / maxDecel; // 从maxVelocity减速到0所需的距离

        if (sAccelMax + sDecelMax > absDistance) {
            // 距离太短，无法达到最大速度 -> 三角形速度曲线
            // 根据位移和加速度求解实际峰值速度：0.5*v^2*(1/maxAccel + 1/maxDecel) = absDistance
            double vPeakAbs = Math.sqrt(2.0 * absDistance / (1.0 / maxAccel + 1.0 / maxDecel));
            peakVelocity = sign * vPeakAbs;

            // 只有加速段和减速段
            tAccelEnd = vPeakAbs / maxAccel;
            double tDecel = vPeakAbs / maxDecel;
            tCruiseEnd = tAccelEnd;       // 无巡航段，减速直接从加速结束开始
            tTotal = tAccelEnd + tDecel;

            sAccel = 0.5 * vPeakAbs * vPeakAbs / maxAccel;
            sDecel = 0.5 * vPeakAbs * vPeakAbs / maxDecel;
            sCruise = 0.0;
        } else {
            // 能够达到最大速度 -> 梯形速度曲线
            peakVelocity = sign * maxVelocity;

            sAccel = sAccelMax;
            sDecel = sDecelMax;
            sCruise = absDistance - sAccel - sDecel;

            tAccelEnd = maxVelocity / maxAccel;
            double tCruise = sCruise / maxVelocity;
            double tDecel = maxVelocity / maxDecel;
            tCruiseEnd = tAccelEnd + tCruise;
            tTotal = tAccelEnd + tCruise + tDecel;
        }
    }

    // ==================== 运动状态查询 ====================

    /**
     * 根据当前已运动时间返回参考位置。
     * @param currentDt 从运动开始的累计时间
     * @return 期望位置
     */
    public double motion_profile_pos(double currentDt) {
        if (currentDt >= tTotal) {
            isOver = true;
            isDone = true;
            return finalPos;
        }

        if (currentDt <= tAccelEnd) {
            // 加速段：s = 0.5 * a * t^2
            return startPos + 0.5 * accelAcc * currentDt * currentDt;
        } else if (currentDt <= tCruiseEnd) {
            // 巡航段：s = s_accel + v_peak * (t - t_accel)
            double tCruise = currentDt - tAccelEnd;
            return startPos + sign * sAccel + peakVelocity * tCruise;
        } else {
            // 减速段：从巡航结束开始，以减速度 decelAcc 运动
            double tDecel = currentDt - tCruiseEnd;
            // s = s_accel + s_cruise + v_peak * t - 0.5 * |decel| * t^2，注意 decelAcc 为负
            double posAtDecelStart = startPos + sign * (sAccel + sCruise);
            return posAtDecelStart + peakVelocity * tDecel + 0.5 * decelAcc * tDecel * tDecel;
        }
    }

    /**
     * 根据当前已运动时间返回参考速度。
     * @param currentDt 从运动开始的累计时间
     * @return 期望速度
     */
    public double motion_profile_vel(double currentDt) {
        if (currentDt >= tTotal) {
            return 0.0;
        }

        if (currentDt <= tAccelEnd) {
            // 加速段：v = a * t
            return accelAcc * currentDt;
        } else if (currentDt <= tCruiseEnd) {
            // 巡航段：速度恒定
            return peakVelocity;
        } else {
            // 减速段：v = v_peak + a_decel * (t - t_cruise_end)
            double tDecel = currentDt - tCruiseEnd;
            return peakVelocity + decelAcc * tDecel;
        }
    }

    /**
     * 根据当前已运动时间返回参考加速度。
     * @param currentDt 从运动开始的累计时间
     * @return 期望加速度
     */
    public double motion_profile_accel(double currentDt) {
        if (currentDt >= tTotal) {
            return 0.0;
        }

        if (currentDt <= tAccelEnd) {
            return accelAcc;
        } else if (currentDt <= tCruiseEnd) {
            return 0.0;
        } else {
            return decelAcc;
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取运动总时长。
     * @return 总时间
     */
    public double getEntire_dt() {
        return tTotal;
    }

    /**
     * 判断运动曲线是否已执行完毕（到达终点后保持）。
     * @return true 表示已结束
     */
    public boolean isOver() {
        return isOver;
    }

    /**
     * 判断运动是否完成并已停止。
     * @return true 表示完成
     */
    public boolean isDone() {
        return isDone;
    }
}

