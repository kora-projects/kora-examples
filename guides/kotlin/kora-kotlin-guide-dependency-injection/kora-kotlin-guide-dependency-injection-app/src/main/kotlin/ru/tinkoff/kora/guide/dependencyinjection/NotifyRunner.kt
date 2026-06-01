package ru.tinkoff.kora.guide.dependencyinjection

import ru.tinkoff.kora.application.graph.All
import ru.tinkoff.kora.application.graph.Lifecycle
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.guide.dependencyinjection.activity.ActivityService
import ru.tinkoff.kora.guide.dependencyinjection.common.Notifier
import ru.tinkoff.kora.guide.dependencyinjection.storage.Storage

@Root
@Component
class NotifyRunner(
    @Tag(Tag.Any::class) private val allNotifiers: All<Notifier>,
    private val stringStorage: Storage<String>,
    private val activityService: ActivityService
) : Lifecycle {

    override fun init() {
        println("DI tutorial complete scenario start")
        for (notifier in allNotifiers) {
            notifier.notify("Diana", "Welcome to Kora DI!")
        }
        stringStorage.save("Scenario payload for Diana")
        activityService.recordActivityByUserName("Diana")
        println("DI tutorial complete scenario done")
    }

    override fun release() {
        println("Application shutdown")
    }
}
