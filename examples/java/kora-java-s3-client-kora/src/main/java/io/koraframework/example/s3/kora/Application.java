package io.koraframework.example.s3.kora;

import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.http.client.apache.ApacheHttpClientModule;
import io.koraframework.logging.logback.LogbackModule;
import io.koraframework.s3.client.kora.KoraS3ClientModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        KoraS3ClientModule,
        ApacheHttpClientModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}
