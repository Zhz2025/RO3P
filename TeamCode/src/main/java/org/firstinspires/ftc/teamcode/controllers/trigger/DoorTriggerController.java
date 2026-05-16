package org.firstinspires.ftc.teamcode.controllers.trigger;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.ServoImplEx;

@Config
public class DoorTriggerController implements TriggerController{
    public static class Params {
        public double OPEN_POS = 1;
        public double CLOSED_POS = 0;
    }
    public static Params PARAMS = new Params();
    private final ServoImplEx triggerServo;
    public DoorTriggerController(ServoImplEx triggerServo){
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
                if(!triggerServo.isPwmEnabled()) triggerServo.setPwmEnable();
                triggerServo.setPosition(PARAMS.OPEN_POS);
                break;
            case CLOSED:
                if(!triggerServo.isPwmEnabled()) triggerServo.setPwmEnable();
                triggerServo.setPosition(PARAMS.CLOSED_POS);
                break;
            case RESETTING:
                if(triggerServo.isPwmEnabled())
                    triggerServo.setPwmDisable();
                break;
        }
    }
}
