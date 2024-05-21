package ru.tinkoff.kora.example.s3.aws;

import ru.tinkoff.kora.application.graph.KoraApplication;
import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.config.hocon.HoconConfigModule;
import ru.tinkoff.kora.http.client.jdk.JdkHttpClientModule;
import ru.tinkoff.kora.logging.logback.LogbackModule;
import ru.tinkoff.kora.s3.client.aws.AwsS3ClientModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        AwsS3ClientModule,
        JdkHttpClientModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}
