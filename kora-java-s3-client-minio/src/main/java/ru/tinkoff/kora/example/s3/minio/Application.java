package ru.tinkoff.kora.example.s3.minio;

import ru.tinkoff.kora.application.graph.KoraApplication;
import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.config.hocon.HoconConfigModule;
import ru.tinkoff.kora.http.client.ok.OkHttpClientModule;
import ru.tinkoff.kora.logging.logback.LogbackModule;
import ru.tinkoff.kora.s3.client.minio.MinioS3ClientModule;

@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        MinioS3ClientModule,
        OkHttpClientModule  {

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}
