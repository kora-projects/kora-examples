package io.koraframework.guide.dependencyinjection;

import io.koraframework.application.graph.All;
import io.koraframework.application.graph.Lifecycle;
import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Tag;
import io.koraframework.common.annotation.Root;
import io.koraframework.guide.dependencyinjection.activity.ActivityService;
import io.koraframework.guide.dependencyinjection.common.Notifier;
import io.koraframework.guide.dependencyinjection.storage.Storage;

@Root
@Component
public final class NotifyRunner implements Lifecycle {

    private final All<Notifier> allNotifiers;
    private final Storage<String> stringStorage;
    private final ActivityService activityService;

    public NotifyRunner(@Tag(Tag.Any.class) All<Notifier> allNotifiers,
                        Storage<String> stringStorage,
                        ActivityService activityService) {
        this.allNotifiers = allNotifiers;
        this.stringStorage = stringStorage;
        this.activityService = activityService;
    }

    @Override
    public void init() {
        System.out.println("DI tutorial complete scenario start");
        for (var notifier : allNotifiers) {
            notifier.notify("Diana", "Welcome to Kora DI!");
        }
        stringStorage.save("Scenario payload for Diana");
        activityService.recordActivityByUserName("Diana");
        System.out.println("DI tutorial complete scenario done");
    }

    @Override
    public void release() {
        System.out.println("Application shutdown");
    }
}