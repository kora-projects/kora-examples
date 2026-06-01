package ru.tinkoff.kora.guide.testingintegration;

import java.util.List;
import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.common.Tag;
import ru.tinkoff.kora.common.annotation.Root;
import ru.tinkoff.kora.database.common.annotation.Query;
import ru.tinkoff.kora.database.common.annotation.Repository;
import ru.tinkoff.kora.database.jdbc.JdbcRepository;
import ru.tinkoff.kora.guide.databasejdbc.Application;
import ru.tinkoff.kora.guide.databasejdbc.repository.UserDAO;

@KoraApp
public interface TestApplication extends Application {

    @Repository
    interface TestUserRepository extends JdbcRepository {

        @Query("SELECT id, name, email, created_at FROM users ORDER BY id")
        List<UserDAO> findAll();

        @Query("DELETE FROM users")
        void deleteAll();
    }

    @Tag(TestApplication.class)
    @Root
    default String testRoot(TestUserRepository ignored) {
        return "test-root";
    }
}
