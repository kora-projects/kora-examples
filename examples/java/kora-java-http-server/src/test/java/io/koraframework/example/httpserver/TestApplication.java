package io.koraframework.example.httpserver;

import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.common.annotation.Root;
import io.koraframework.example.http.server.Application;
import io.koraframework.example.http.server.JsonPostController;
import io.koraframework.http.client.common.annotation.HttpClient;
import io.koraframework.http.client.jdk.JdkHttpClientModule;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.json.common.annotation.Json;

/**
 * Тестовый контейнер приложение, который расширяет основное приложение и например добавляет
 * некоторые компоненты
 * из общих модулей, которые не используются в данном приложении, которые могут быть например
 * использованы в других подобных приложениях.
 * Например, когда у вас есть разные приложения READ API и WRITE API.
 * Либо, вам нужны некоторые функции сохранения/удаления/обновления только для тестирования в
 * качестве быстрой тестовой утилиты.
 * <p>
 * Но мы НАСТОЯТЕЛЬНО РЕКОМЕНДУЕМ ТЕСТИРОВАТЬ приложения как черный ящик в качестве основного
 * подхода.
 * -------
 * Test Application than extends Real Application and may be adds some components
 * from common modules that are not used in Real App, but may be used in other similar apps.
 * Like when you have different READ API application and WRITE API application
 * or may be, you need some save/delete/update methods only for testing as fast test utils.
 * <p>
 * But we STRONGLY ENCOURAGE AND RECOMMEND TO USE black box testing as a primary source of truth for
 * tests.
 */
@KoraApp
public interface TestApplication extends Application, JdkHttpClientModule {

    @Root
    @Component
    @HttpClient(configPath = "testHttpClient")
    interface JsonHttpClient {

        @HttpRoute(method = HttpMethod.POST, path = "/json")
        @Json
        JsonPostController.JsonResponse post(@Json JsonPostController.JsonRequest body);
    }
}
