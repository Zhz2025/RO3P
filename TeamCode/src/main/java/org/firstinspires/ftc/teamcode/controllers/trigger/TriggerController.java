package org.firstinspires.ftc.teamcode.controllers.trigger;

public interface TriggerController {
    public enum TriggerState{
        OPEN, CLOSED, RESETTING
    }
    public void setTriggerState(TriggerState state);
    public TriggerState getTriggerState();
    public void update();
}
