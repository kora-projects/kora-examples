package io.koraframework.example.config.hocon;

import io.koraframework.application.graph.Lifecycle;
import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UncheckedIOException;

@Root
@Component
public final class RootService implements Lifecycle {

    private static final Logger log = LoggerFactory.getLogger(RootService.class);
    private final FooConfig fooConfig;

    public RootService(FooConfig fooConfig) {
        this.fooConfig = fooConfig;
    }

    @Override
    public void init() throws Exception {
        log.info("INIT");
        try {
            if(true) {
                throw new IllegalStateException("OPS");
            }
        } catch (IllegalStateException e) {
            // do nothing
        }
    }

    @Override
    public void release() throws Exception {
        log.info("RELEASE");
    }
}
