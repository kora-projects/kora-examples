package example;

import io.koraframework.common.annotation.Component;
import io.koraframework.scheduling.db.scheduler.annotation.ScheduleDbWithFixedDelay;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.UUID;

@Component
public final class TickJob {
    private final DataSource dataSource;
    private final String instance = UUID.randomUUID().toString();

    public TickJob(DataSource dataSource) { this.dataSource = dataSource; }

    @ScheduleDbWithFixedDelay(name = "example-tick", config = "jobs.tick")
    public void tick() {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement("INSERT INTO example_ticks(instance) VALUES (?)")) {
            statement.setString(1, instance);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to persist tick", e);
        }
    }
}
