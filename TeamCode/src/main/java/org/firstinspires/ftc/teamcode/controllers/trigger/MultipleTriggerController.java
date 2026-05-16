package org.firstinspires.ftc.teamcode.controllers.trigger;

public class MultipleTriggerController implements TriggerController{
    TriggerController[] triggerControllers;
    public MultipleTriggerController(TriggerController... triggerControllers){
        this.triggerControllers = triggerControllers;
    }
    TriggerState triggerState = TriggerState.RESETTING;
    @Override
    public void setTriggerState(TriggerState state) {
        triggerState =state;
        for(TriggerController triggerController : triggerControllers){
            triggerController.setTriggerState(triggerState);
        }
    }

    @Override
    public TriggerState getTriggerState() {
        return triggerState;
    }

    @Override
    public void update() {
        for(TriggerController triggerController : triggerControllers){
            triggerController.update();
        }
    }
}
