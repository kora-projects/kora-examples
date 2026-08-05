package io.koraframework.example.scheduling.quartz;

import io.koraframework.common.annotation.Component;
import io.koraframework.scheduling.quartz.ScheduleWithTrigger;

@Component
public final class TriggerScheduler {

    private int state = 0;

    /**
     * @see Application#myTrigger()
     */
    @ScheduleWithTrigger(TriggerScheduler.class)
    void schedule() {
        state++;
    }

    public int getState() {
        return state;
    }
}
