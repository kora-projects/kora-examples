package example;

import io.koraframework.common.annotation.Component;
import io.koraframework.http.server.common.annotation.HttpController;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.resilient.retry.annotation.Retryable;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@HttpController
public class PartnerController {
    private final SharedLimiter limiter;
    private final AtomicInteger attempts = new AtomicInteger();
    public PartnerController(SharedLimiter limiter) { this.limiter = limiter; }

    @HttpRoute(method = "POST", path = "/permit")
    public String permit() { return limiter.tryAcquire() ? "allowed" : "limited"; }

    @HttpRoute(method = "POST", path = "/fail")
    @Retryable(PartnerRetry.class)
    public String fail() {
        attempts.incrementAndGet();
        throw new IllegalStateException("Simulated downstream failure");
    }

    @HttpRoute(method = "GET", path = "/attempts")
    public String attempts() { return Integer.toString(attempts.get()); }
}
