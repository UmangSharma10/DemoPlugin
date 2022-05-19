package com.mindarray;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bootstrap {

    public final static Vertx vertx = Vertx.vertx();
    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryEngine.class);
    public static void main(String[] args) {

        start(APIServer.class.getName())

               .compose(future -> start(DatabaseEngine.class.getName()))

               .compose(future -> start(DiscoveryEngine.class.getName()))

                .compose(future -> start(PollerEngine.class.getName()))

                .onComplete(handler -> {

                    if (handler.succeeded()) {

                        LOGGER.debug("ALL VERTICLES DEPLOYED");

                    } else {
                        LOGGER.error("ERROR IN DEPLOYING");
                    }

                });
    }

    public static Future<Void> start(String verticle) {

        Promise<Void> promise = Promise.promise();

        vertx.deployVerticle(verticle, handler -> {

            if (handler.succeeded()) {

                promise.complete();

            } else {
                System.out.println(handler.cause());

                promise.fail(handler.cause());

            }
        });
        return promise.future();
    }


}
