package io.koraframework.guide.dependencyinjection.activity;

import io.koraframework.application.graph.ValueOf;
import io.koraframework.common.annotation.Component;

@Component
public final class ActivityService {

    private final ValueOf<ActivityRecorder> activityRecorder;

    public ActivityService(ValueOf<ActivityRecorder> activityRecorder) {
        this.activityRecorder = activityRecorder;
        System.out.println("ActivityService created (ActivityRecorder not yet accessed)");
    }

    public void recordActivityByUserName(String user) {
        System.out.println("Recording activity for: " + user);
        ActivityRecorder recorder = activityRecorder.get();
        recorder.recordUser(user);
        System.out.println("Activity recorded successfully");
    }
}