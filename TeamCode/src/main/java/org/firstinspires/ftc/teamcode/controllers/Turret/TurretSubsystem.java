package org.firstinspires.ftc.teamcode.controllers.Turret;

import android.util.Size;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.BuiltinCameraDirection;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;
import org.firstinspires.ftc.teamcode.Library.Team4410.LinearInterpolation;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;
import java.util.function.BooleanSupplier;

public class TurretSubsystem {
    public static class Params {
        static double SwitchDistance = 101.808; //切换挡板高/低状态的阈值，单位inch
        static double[] Dis = {0,200}; //inch
        static double[] Speed = {0,2000}; //tick/s
        static double ManualPower = 1; // 手动控制炮台的功率
    }

    int manualTargetpeed = 0;
    
    LinearInterpolation disToSpeed = new LinearInterpolation(Params.Dis, Params.Speed);
    
    //logic
    private boolean UsingAutoAiming = true;
    public void toggleAutoAiming(){
        UsingAutoAiming = !UsingAutoAiming;
    }

    //hardware
    private Velocity currentVelocity;
    private AngularVelocity currentAngularVelocity;
    private Telemetry myTelemetry;
    public TurretModule_Simplified turretModule;
    public FlyWheelModule flyWheelModule;
    public BoardModule boardModule;
    private boolean HighBoard = true;
    
    private BooleanSupplier BoardUp;
    private BooleanSupplier BoardDown;
    private BooleanSupplier TurretTurnRight;
    private BooleanSupplier TurretTurnLeft;

    // 外部传入 Supplier
    public void setBoardUpSupplier(BooleanSupplier supplier) {
        this.BoardUp = supplier;
    }
    public void setBoardDownSupplier(BooleanSupplier supplier) {
        this.BoardDown = supplier;
    }
    public void setTurretTurnRightSupplier(BooleanSupplier supplier) {
        this.TurretTurnRight = supplier;
    }
    public void setTurretTurnLeftSupplier(BooleanSupplier supplier) {
        this.TurretTurnLeft = supplier;
    }

    //vision
    private AprilTagProcessor aprilTag;
    private VisionPortal visionPortal;
    private double currentDistanceToGoal = 0;
    private double currentBearingToGoal = 0;

    public TurretSubsystem(HardwareMap hardwareMap, Telemetry telemetryRC){
        myTelemetry = telemetryRC;
        turretModule = new TurretModule_Simplified(hardwareMap,telemetryRC);
        flyWheelModule = new FlyWheelModule(hardwareMap,telemetryRC);
        boardModule = new BoardModule(hardwareMap,telemetryRC);
        // Create the AprilTag processor.
        aprilTag = new AprilTagProcessor.Builder()
                .setDrawAxes(false)
                .setDrawCubeProjection(false)
                .setDrawTagOutline(true)
                .setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES)
                //.setLensIntrinsics(578.272, 578.272, 402.145, 221.506)

                .build();
        VisionPortal.Builder builder = new VisionPortal.Builder();
        builder.setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"));
        builder.setCameraResolution(new Size(640, 480));

        builder.setStreamFormat(VisionPortal.StreamFormat.YUY2);

        builder.addProcessor(aprilTag);

        visionPortal = builder.build();
    }


    public void updateVelocity(Velocity velocityRC, AngularVelocity angularVelocityRC){
        currentVelocity = velocityRC;
        currentAngularVelocity = angularVelocityRC;
    }

    public void setTarget(double tx, double distance, double RobotHeading){

    }
    public void update(double RobotHeading){
        if(UsingAutoAiming){
            turretModule.setAutoControl();
            List<AprilTagDetection> currentDetections = aprilTag.getDetections();
            myTelemetry.addData("#### AprilTags Detected ####", currentDetections.size());
            if(currentDetections.isEmpty()){
                currentBearingToGoal = 0;
                currentDistanceToGoal = 0;
            }
            else{
                for (AprilTagDetection detection : currentDetections) {
                    if (detection.metadata != null) {
                        currentDistanceToGoal = detection.ftcPose.range;
                        currentBearingToGoal = detection.ftcPose.bearing;
                        myTelemetry.addLine(String.format("RBE %6.1f %6.1f %6.1f  (inch, deg, deg)", detection.ftcPose.range, detection.ftcPose.bearing, detection.ftcPose.elevation));
                    } else {
                        myTelemetry.addData("Unknow AprilTag", detection.id);
                        currentBearingToGoal = 0;
                        currentDistanceToGoal = 0;
                    }
                }
            }
            flyWheelModule.setTargetSpeed((int)disToSpeed.interpolate(currentDistanceToGoal));
            turretModule.setTargetDegreeDelta(currentBearingToGoal);
            if(currentDistanceToGoal > Params.SwitchDistance){
                boardModule.setLowPosition();
            }
            else{
                boardModule.setLowPosition();
            }
        }
        else{
            // 手动模式下根据 supplier 控制
            // 挡板优先级：Up > Down
            if (BoardUp != null && BoardUp.getAsBoolean()) {
                manualBoardHigh();
            } else if (BoardDown != null && BoardDown.getAsBoolean()) {
                manualBoardLow();
            }
            // 炮台优先级：Right > Left
            if (TurretTurnRight != null && TurretTurnRight.getAsBoolean()) {
                manualTurretForward();
            } else if (TurretTurnLeft != null && TurretTurnLeft.getAsBoolean()) {
                manualTurretReverse();
            } else {
                // 如果都没按，停止炮台
                turretModule.setMotorPower(0);
            }
            flyWheelModule.setTargetSpeed(manualTargetpeed);
        }

        turretModule.update();
        flyWheelModule.update();
        boardModule.update();
        myTelemetry.addData("Manual?", UsingAutoAiming);
        myTelemetry.addData("TurretTurnRightSupplier", TurretTurnRight.getAsBoolean());
    }

    // 手动控制：挡板高位
    public void manualBoardHigh() {
        boardModule.setHighPosition();
    }

    // 手动控制：挡板低位
    public void manualBoardLow() {
        boardModule.setLowPosition();
    }

    // 手动控制：炮台正转
    public void manualTurretForward() {
        turretModule.setManualControl();
        turretModule.setMotorPower(Params.ManualPower);
    }

    // 手动控制：炮台反转
    public void manualTurretReverse() {
        turretModule.setManualControl();
        turretModule.setMotorPower(-Params.ManualPower);
    }

    public void setManualTargetSpeed(int speed){
        manualTargetpeed = speed;
    }

}
