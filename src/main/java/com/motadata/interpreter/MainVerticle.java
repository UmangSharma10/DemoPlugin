package com.motadata.interpreter;

import com.motadata.data.DiscoverCredentials;
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

  DiscoverCredentials cred = new DiscoverCredentials();

  public MainVerticle() {
  }

  @Override
  public void start(Promise<Void> startPromise) {

    Router router = Router.router(vertx);

    router.route().handler(BodyHandler.create());
    EventBus eventBus = vertx.eventBus();

    router.post("/discovery").method(HttpMethod.POST).handler(ctx->{

      JsonObject jsonObject = ctx.getBodyAsJson();

      cred.setMetricType(jsonObject.getString("device"));

      cred.setHost(jsonObject.getString("host"));

      cred.setUser(jsonObject.getString("user"));

      cred.setPort(jsonObject.getString("port"));

      cred.setPassword(jsonObject.getString("password"));

      cred.setCommunity(jsonObject.getString("community"));

      cred.setVersion(jsonObject.getString("version"));

      eventBus.request("my.request.address", jsonObject, req ->{

        LOG.debug("Response {} ", req.result().body());

        ctx.response().end(req.result().body().toString());

      });




    });
    vertx.createHttpServer().requestHandler(router).listen(8888, http -> {

      if (http.succeeded())
      {
        startPromise.complete();

        LOG.debug("HTTP server started on port 8888");
      }
      else
      {
        startPromise.fail(http.cause());
      }
    });
  }
}
