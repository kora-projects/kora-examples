package ru.tinkoff.kora.example.crud;

import java.util.List;
import io.koraframework.common.Component;
import io.koraframework.common.KoraApp;
import io.koraframework.common.annotation.Root;
import io.koraframework.database.common.annotation.Query;
import io.koraframework.database.common.annotation.Repository;
import io.koraframework.database.jdbc.JdbcRepository;
import io.koraframework.example.crud.model.dao.Pet;
import io.koraframework.example.crud.model.dao.PetCategory;

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
public interface TestApplication extends Application {

    @Root
    @Component
    @Repository
    interface TestPetRepository extends JdbcRepository {

        @Query("SELECT %{return#selects} FROM %{return#table}")
        List<Pet> findAll();

        @Query("DELETE FROM pets")
        void deleteAll();
    }

    @Root
    @Component
    @Repository
    interface TestCategoryRepository extends JdbcRepository {

        @Query("SELECT %{return#selects} FROM %{return#table}")
        List<PetCategory> findAll();

        @Query("DELETE FROM categories")
        void deleteAll();
    }
}
