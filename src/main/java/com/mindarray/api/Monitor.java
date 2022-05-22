package com.mindarray.api;

import com.mindarray.Bootstrap;
import com.mindarray.Constant;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class Monitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Monitor.class);

    public void init(Router monitorRoute){
        LOGGER.debug("Monitor Class Deployed");

        monitorRoute.put("/monitor").setName("update").handler(this::validate).handler(this::update);

        monitorRoute.delete("/monitor/:id").setName("delete").handler(this::delete);

        monitorRoute.get("/monitor/:id").setName("get").handler(this::validate).handler(this::getById);

        monitorRoute.get("/monitor").setName("getAll").handler(this::getAll);




    }

    private void validate(RoutingContext routingContext) {

        JsonObject trimData = routingContext.getBodyAsJson();


        if (routingContext.currentRoute().getName().equals("create") || routingContext.currentRoute().getName().equals("update")) {

            try {

                HashMap<String, Object> result;

                if ((trimData != null)) {

                    result = new HashMap<>(trimData.getMap());

                    for (String key : result.keySet()) {

                        var val = result.get(key);

                        if (val instanceof String) {

                            result.put(key, val.toString().trim());
                        }

                        trimData = new JsonObject(result);

                        routingContext.setBody(trimData.toBuffer());
                    }
                } else {

                    routingContext.response().setStatusCode(400).putHeader(Constant.CONTENT_TYPE, Constant.APPLICATION_JSON).end(new JsonObject().put(Constant.STATUS, Constant.FAILED).encode());

                }
            } catch (Exception exception) {

                routingContext.response().setStatusCode(400).putHeader(Constant.CONTENT_TYPE, Constant.APPLICATION_JSON).end(new JsonObject().put(Constant.STATUS, Constant.FAILED).encode());
            }


        }

        switch (routingContext.currentRoute().getName()){
            case "get":
                LOGGER.debug("getById");
                String getId = routingContext.pathParam("id");
                Bootstrap.vertx.eventBus().<JsonObject>request(Constant.EVENTBUS_CHECK_MONITOR, getId, get -> {
                    if (get.succeeded()) {
                        JsonObject getDisData = get.result().body();
                        if (!getDisData.containsKey(Constant.ERROR)) {
                            routingContext.next();
                        } else {
                            routingContext.response().setStatusCode(400).putHeader(Constant.CONTENT_TYPE, Constant.APPLICATION_JSON).end(getDisData.encode());
                        }
                    } else {
                        LOGGER.error("Error");
                    }
                });
        }
    }

    private void getAll(RoutingContext routingContext) {
    }

    private void getById(RoutingContext routingContext) {
        try {
            String getId = routingContext.pathParam("id");
            Bootstrap.vertx.eventBus().<JsonObject>request(Constant.EVENTBUS_GET_MONITOR_BY_ID, getId, createHandler -> {
                JsonObject getData = createHandler.result().body();
                LOGGER.debug("Response {} ", createHandler.result().body().toString());
                routingContext.response().setStatusCode(200).putHeader("content-type", Constant.APPLICATION_JSON).end(getData.encode());
            });
        } catch (Exception exception) {
            routingContext.response().setStatusCode(400).putHeader("content-type", Constant.APPLICATION_JSON).end(new JsonObject().put(Constant.STATUS, Constant.FAILED).encode());
        }
    }

    private void delete(RoutingContext routingContext) {
    }

    private void update(RoutingContext routingContext) {

    }


}
