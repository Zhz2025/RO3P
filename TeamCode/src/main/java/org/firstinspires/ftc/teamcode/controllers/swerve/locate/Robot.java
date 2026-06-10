package org.firstinspires.ftc.teamcode.controllers.swerve.locate;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.teamcode.RoadRunner.Localizer;
import org.firstinspires.ftc.teamcode.utility.Math.MathSolver;
import org.firstinspires.ftc.teamcode.utility.Math.Point2D;
@Config
public class Robot {
    public static class Params {
        static int minUpdateIntervalMs = 20; // 最小更新时间间隔，单位毫秒

    }
    public void setMinUpdateIntervalMs(int interval){
        Params.minUpdateIntervalMs=interval;
    }

    private static Robot instance;
    public static Robot getInstance(){
        if(instance==null){
            throw new IllegalStateException("Robot not initialized, call setInstance first");
        }
        return instance;
    }
    /**
    * 初始化位置
     * @param localizer 定位器
    * @return RobotPosition实例
     */
    public static Robot refresh(Localizer localizer, VoltageSensor voltageSensor){
        Point2D initialPosition=MathSolver.toPoint2D(localizer.getPose());
        double initialHeadingRadian=localizer.getPose().heading.toDouble();
        instance=new Robot();
        instance.initialPosition=initialPosition;
        instance.initialHeadingRadian=initialHeadingRadian;
        Data_Position.instance.setPosition(initialPosition);
        Data_Position.instance.headingRadian=initialHeadingRadian;
        instance.localizer = localizer;

        instance.voltageSensor = voltageSensor;
        return instance;
    }

    private Robot(){}
    public VoltageSensor voltageSensor;

    public Localizer localizer;
    public Point2D initialPosition=new Point2D(0,0);
    public double initialHeadingRadian=0;

    public long lastUpdateTime=0;

    public void update(){
        if(System.currentTimeMillis()-lastUpdateTime<Params.minUpdateIntervalMs){
            return;
        }
        PoseVelocity2d poseVelocity2d = localizer.update();
        Pose2d pose = localizer.getPose();
        Data_Position.instance.headingSpeedRadianPerSec=poseVelocity2d.angVel;
        Data_Position.instance.setSpeed(new Vector2d(-poseVelocity2d.linearVel.y,+poseVelocity2d.linearVel.x));
        Data_Position.instance.setPosition(new Point2D(-pose.position.y,+pose.position.x));
        Data_Position.instance.headingRadian=pose.heading.log();

        Data_Voltage.instance.setVoltage(voltageSensor.getVoltage());
        lastUpdateTime=System.currentTimeMillis();
    }
    public Data_Position getData_Position(){
        update();
        return Data_Position.instance;
    }
    public Data_Voltage getData_Voltage(){
        update();
        return Data_Voltage.instance;
    }
}
