package com.motadata.nms;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(MainVerticle.class);


    public MainVerticle() {
    }

    @Override
    public void start(Promise<Void> startPromise) {

        LOG.debug("MainVerticle Deployed");
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        EventBus eventBus = vertx.eventBus();

        router.post("/discovery").method(HttpMethod.POST).handler(ctx -> {

            JsonObject jsonObject = ctx.getBodyAsJson();

            eventBus.request("my.request.address", jsonObject, req -> {

                LOG.debug("Response {} ", req.result().body());

                ctx.response().end(req.result().body().toString());

            });
        });

        router.put("/MetricGroup").handler(metric -> {
            JsonObject jsonObjectMetric = metric.getBodyAsJson();

            eventBus.request("metric.address", jsonObjectMetric, req -> {
                LOG.debug("Response {} ", req.result().body());
                metric.response().end(req.result().body().toString());
            });

        });

        router.put("/Monitor").handler(monitor -> {
            JsonObject jsonObjectMonitor = monitor.getBodyAsJson();
            eventBus.request("monitor.address", jsonObjectMonitor, req -> {
                LOG.debug("Response {}", req.result().body());
                monitor.response().end(req.result().body().toString());
            });
        });

        router.delete("/Monitor").handler(monitor -> {
            JsonObject jsonObjectDeleteMonitor = monitor.getBodyAsJson();
            eventBus.request("monitor.address", jsonObjectDeleteMonitor, req -> {
                LOG.debug("Response {}", req.result().body());
                monitor.response().end(req.result().body().toString());
            });

        });


        vertx.createHttpServer().requestHandler(router).listen(8888, http -> {

            if (http.succeeded()) {
                startPromise.complete();

                LOG.debug("HTTP server started on port 8888");
            } else {
                startPromise.fail(http.cause());
            }
        });
    }
}
