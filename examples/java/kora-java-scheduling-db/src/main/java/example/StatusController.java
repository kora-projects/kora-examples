package example;

import io.koraframework.common.annotation.Component;
import io.koraframework.http.server.common.annotation.HttpController;
import io.koraframework.http.common.annotation.HttpRoute;
import javax.sql.DataSource;
import java.sql.SQLException;

@Component
@HttpController
public final class StatusController {
    private final DataSource dataSource;
    public StatusController(DataSource dataSource) { this.dataSource = dataSource; }

    @HttpRoute(method = "GET", path = "/ticks")
    public String ticks() throws SQLException {
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement();
             var rows = statement.executeQuery("SELECT count(*) FROM example_ticks")) {
            rows.next();
            return Long.toString(rows.getLong(1));
        }
    }
}
