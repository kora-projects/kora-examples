package ru.tinkoff.kora.example.scheduling.quartz;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.Tag;
import ru.tinkoff.kora.scheduling.quartz.ScheduleWithTrigger;

@Component
public final class TriggerScheduler {

    private int state = 0;

    /**
     * @see Application#myTrigger()
     */
    @ScheduleWithTrigger(@Tag(TriggerScheduler.class))
    void schedule() {
        state++;
    }

    public int getState() {
        return state;
    }
}
