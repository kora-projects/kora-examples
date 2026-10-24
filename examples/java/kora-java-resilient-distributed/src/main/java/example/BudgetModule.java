package example;

import io.koraframework.common.annotation.Tag;
import io.koraframework.resilient.distributed.retry.DistributedRetryBudgetClient;
import io.koraframework.resilient.distributed.retry.DistributedRetryBudgetFactory;
import io.koraframework.resilient.retry.RetryBudgetFactory;

public interface BudgetModule {
    @Tag(PartnerRetry.class)
    default RetryBudgetFactory retryBudgetFactory(DistributedRetryBudgetClient client, RetryBudgetConfig config) {
        return new DistributedRetryBudgetFactory(client, config.keyPrefix(), config.bucketTtl());
    }
}
