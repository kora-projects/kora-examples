package example;

import io.koraframework.resilient.distributed.ratelimiter.annotation.RateLimiterDistributedSpec;
import io.koraframework.resilient.ratelimiter.RateLimiter;

@RateLimiterDistributedSpec("resilient.ratelimiter.shared")
public interface SharedLimiter extends RateLimiter {}
