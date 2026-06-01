package ru.tinkoff.kora.guide.dependencyinjection;

import ru.tinkoff.kora.application.graph.All;
import ru.tinkoff.kora.application.graph.Lifecycle;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.Tag;
import ru.tinkoff.kora.common.annotation.Root;
import ru.tinkoff.kora.guide.dependencyinjection.activity.ActivityService;
import ru.tinkoff.kora.guide.dependencyinjection.common.Notifier;
import ru.tinkoff.kora.guide.dependencyinjection.storage.Storage;

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