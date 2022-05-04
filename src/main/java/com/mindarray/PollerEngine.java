package com.mindarray;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollerEngine extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(PollerEngine.class);

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        LOG.debug("POLLER ENGINE DEPLOYED");
        startPromise.complete();
    }
}
