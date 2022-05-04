package com.mindarray;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class APIServer extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(APIServer.class);
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        LOG.debug("APISERVER DEPLOYED");



        Router router = Router.router(vertx);


        router.route().handler(BodyHandler.create());

        router.post(NmsConstant.DISCOVERY).method(HttpMethod.POST).handler(ctx -> {

            JsonObject jsonObject = ctx.getBodyAsJson();
            System.out.println(jsonObject);

            if(jsonObject!=null) {
                vertx.eventBus().request(NmsConstant.DISCOVERY_ADDRESS, jsonObject, req -> {
                    if (req.succeeded()){
                        LOG.debug("Response {} ", req.result().body());

                    ctx.response().end(req.result().body().toString());
                }
                    else {
                        ctx.response().end("Please try again, ERROR 404 : DATA NOT FOUND");
                    }
                });
            }
        });



        vertx.createHttpServer().requestHandler(router).listen(8888).onComplete(handler ->{
            if (handler.succeeded()){
                LOG.debug("Server Created on port 8888");
            }
            else {
                LOG.debug("Server Failed");
            }
        });


        startPromise.complete();
    }
}
