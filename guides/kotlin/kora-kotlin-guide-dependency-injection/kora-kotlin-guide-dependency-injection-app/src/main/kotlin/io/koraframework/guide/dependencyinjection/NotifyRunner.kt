package io.koraframework.guide.dependencyinjection

import io.koraframework.application.graph.All
import io.koraframework.application.graph.Lifecycle
import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Tag
import io.koraframework.common.annotation.Root
import io.koraframework.guide.dependencyinjection.activity.ActivityService
import io.koraframework.guide.dependencyinjection.common.Notifier
import io.koraframework.guide.dependencyinjection.storage.Storage

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
