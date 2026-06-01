package ru.tinkoff.kora.example.scheduling.quartz;

import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import ru.tinkoff.kora.application.graph.KoraApplication;
import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.common.Tag;
import ru.tinkoff.kora.config.hocon.HoconConfigModule;
import ru.tinkoff.kora.logging.logback.LogbackModule;
import ru.tinkoff.kora.scheduling.quartz.QuartzModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        QuartzModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }

    @Tag(TriggerScheduler.class)
    default Trigger myTrigger() {
        return TriggerBuilder.newTrigger()
                .withIdentity("myTrigger")
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMilliseconds(50)
                        .repeatForever())
                .build();
    }
}
