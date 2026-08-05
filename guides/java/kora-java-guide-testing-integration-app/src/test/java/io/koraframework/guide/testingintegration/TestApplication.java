package io.koraframework.guide.testingintegration;

import java.util.List;
import io.koraframework.common.annotation.KoraApp;
import io.koraframework.common.annotation.Tag;
import io.koraframework.common.annotation.Root;
import io.koraframework.database.common.annotation.Query;
import io.koraframework.database.common.annotation.Repository;
import io.koraframework.database.jdbc.JdbcRepository;
import io.koraframework.guide.databasejdbc.Application;
import io.koraframework.guide.databasejdbc.repository.UserDAO;

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
