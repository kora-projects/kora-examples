package io.koraframework.example.s3.aws;

import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.http.client.jdk.JdkHttpClientModule;
import io.koraframework.logging.logback.LogbackModule;
import io.koraframework.s3.client.aws.AwsS3ClientModule;

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
