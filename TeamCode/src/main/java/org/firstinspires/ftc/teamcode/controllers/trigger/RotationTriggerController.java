package org.firstinspires.ftc.teamcode.controllers.trigger;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.ServoImplEx;

@Config
public class RotationTriggerController implements TriggerController{
    public static class Params {
        public double OPEN_POWER = 1;
        public double CLOSED_POWER = 0;
    }
    public static Params PARAMS = new Params();
    private ServoImplEx triggerServo;
    public RotationTriggerController(ServoImplEx triggerServo){
        this.triggerServo = triggerServo;
    }
    private TriggerState triggerState = TriggerState.RESETTING;
    @Override
    public void setTriggerState(TriggerState triggerState) {
        this.triggerState = triggerState;
    }
    @Override
    public TriggerState getTriggerState(){
        return triggerState;
    }
    @Override
    public void update(){
        switch (triggerState) {
            case OPEN:
                triggerServo.setPosition(PARAMS.OPEN_POWER);
                break;
            case CLOSED:
                triggerServo.setPosition(PARAMS.CLOSED_POWER);
                break;
            case RESETTING:
                triggerServo.setPosition(0.5);
                break;
        }
    }
}
