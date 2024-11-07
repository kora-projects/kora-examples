package ru.tinkoff.kora.example.crud;

import java.util.List;
import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.common.Tag;
import ru.tinkoff.kora.common.annotation.Root;
import ru.tinkoff.kora.database.common.annotation.Query;
import ru.tinkoff.kora.database.common.annotation.Repository;
import ru.tinkoff.kora.database.jdbc.JdbcRepository;
import ru.tinkoff.kora.example.crud.model.dao.Pet;
import ru.tinkoff.kora.example.crud.model.dao.PetCategory;

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

    @Repository
    interface TestPetRepository extends JdbcRepository {

        @Query("SELECT %{return#selects} FROM %{return#table}")
        List<Pet> findAll();

        @Query("DELETE FROM pets")
        void deleteAll();
    }

    @Repository
    interface TestCategoryRepository extends JdbcRepository {

        @Query("SELECT %{return#selects} FROM %{return#table}")
        List<PetCategory> findAll();

        @Query("DELETE FROM categories")
        void deleteAll();
    }

    // Need any fake root to require components to include them in graph
    @Tag(TestApplication.class)
    @Root
    default String testRoot(TestPetRepository petRepository, TestCategoryRepository categoryRepository) {
        return "root";
    }
}
