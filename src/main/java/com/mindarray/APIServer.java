package com.mindarray;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class APIServer extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(APIServer.class);

    @Override
    public void start(Promise<Void> startPromise) {
        LOGGER.debug("APISERVER DEPLOYED");

        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        router.post(Constant.DISCOVERY).method(HttpMethod.POST).handler(routingContext -> {

            JsonObject requestBody = routingContext.getBodyAsJson();

            if (requestBody != null) {

                vertx.eventBus().request(Constant.EVENTBUS_DISCOVERY, requestBody, req -> {

                    if (req.succeeded()) {

                        LOGGER.debug("Response {} ", req.result().body());

                        routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end(req.result().body().toString());

                    } else {

                        routingContext.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code()).end(new JsonObject().put("error", "DATA NOT FOUND, PLEASE TRY AGAIN LATER").encode());

                    }

                });
            }
        });


        vertx.createHttpServer().requestHandler(router).listen(8888).onComplete(handler -> {

            if (handler.succeeded()) {

                LOGGER.debug("Server Created on port 8888");

            } else {

                LOGGER.debug("Server Failed");

            }

        });


        startPromise.complete();
    }
}
