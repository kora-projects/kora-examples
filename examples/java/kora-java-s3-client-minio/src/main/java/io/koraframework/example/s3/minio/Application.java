package io.koraframework.example.s3.minio;

import io.koraframework.application.graph.KoraApplication;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.config.hocon.HoconConfigModule;
import io.koraframework.http.client.ok.OkHttpClientModule;
import io.koraframework.logging.logback.LogbackModule;
import io.koraframework.s3.client.minio.MinioS3ClientModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        MinioS3ClientModule,
        OkHttpClientModule {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}
