package io.koraframework.example.scheduling.quartz;

import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.common.annotation.Tag;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.logging.logback.LogbackModule;
import io.koraframework.scheduling.quartz.QuartzModule;

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
