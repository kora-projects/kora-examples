package example;

import java.time.Duration;

import io.koraframework.config.common.annotation.ConfigSource;

@ConfigSource("distributedRetryBudget")
public interface RetryBudgetConfig {

    String keyPrefix();

    Duration bucketTtl();
}
