package ru.tinkoff.kora.guide.dependencyinjection.activity;

import ru.tinkoff.kora.application.graph.LifecycleWrapper;
import ru.tinkoff.kora.application.graph.Wrapped;
import ru.tinkoff.kora.common.Module;

@Module
public interface ActivityModule {

    default Wrapped<ActivityRecorder> activityRecorder() {
        var recorder = new ActivityRecorder() {
            private boolean connected;

            @Override
            public void connect() {
                if (!connected) {
                    System.out.println("Connecting to activity recorder");
                    connected = true;
                    System.out.println("Activity recorder connected");
                }
            }

            @Override
            public void disconnect() {
                if (connected) {
                    System.out.println("Disconnecting from activity recorder");
                    connected = false;
                }
            }

            @Override
            public boolean isConnected() {
                return connected;
            }

            @Override
            public void recordUser(String user) {
                if (!connected) {
                    connect();
                }
                System.out.println("Recording user activity: " + user);
            }
        };

        return new LifecycleWrapper<>(recorder, r -> {}, ActivityRecorder::disconnect);
    }
}