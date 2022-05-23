package com.mindarray.api;

import com.mindarray.Bootstrap;
import com.mindarray.Constant;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

import static com.mindarray.Constant.*;
import static com.mindarray.Constant.FAILED;

public class Monitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Monitor.class);

    public void init(Router monitorRoute){
        LOGGER.debug("Monitor Class Deployed");

        monitorRoute.put("/monitor").setName("update").handler(this::validate).handler(this::update);

        monitorRoute.delete("/monitor/:id").setName("delete").handler(this::validate).handler(this::delete);

        monitorRoute.get("/monitor/:id/last").setName("get").handler(this::validate).handler(this::getlastInstance);

        monitorRoute.get("/monitor").setName("getAll").handler(this::getAll);




    }

    private void validate(RoutingContext routingContext) {

        JsonObject data = routingContext.getBodyAsJson();

        HttpServerResponse response = routingContext.response();

        if (routingContext.currentRoute().getName().equals("create") || routingContext.currentRoute().getName().equals("update")) {
            try {

                HashMap<String, Object> result;

                if ((data != null)) {

                    result = new HashMap<>(data.getMap());

                    for (String key : result.keySet()) {

                        var val = result.get(key);

                        if (val instanceof String) {

                            result.put(key, val.toString().trim());
                        }

                        data = new JsonObject(result);

                        routingContext.setBody(data.toBuffer());
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
                Bootstrap.vertx.eventBus().<JsonObject>request(EVENTBUS_CHECK_PROMONITORDID, getId, get -> {
                    if (get.succeeded()) {
                        JsonObject getDisData = get.result().body();
                        if (!getDisData.containsKey(Constant.ERROR)) {
                            routingContext.next();
                        } else {
                            String result = get.result().body().toString();
                            routingContext.response().setStatusCode(400).putHeader(Constant.CONTENT_TYPE, Constant.APPLICATION_JSON).end(result);
                        }
                    } else {
                        LOGGER.error("Error");
                    }
                });

                break;

            case "update":
                LOGGER.debug("Monitor Metric Update");
                if (!(routingContext.getBodyAsJson().containsKey(MONITOR_ID)) || routingContext.getBodyAsJson().getString(MONITOR_ID) == null) {
                    response.setStatusCode(400).putHeader(CONTENT_TYPE, APPLICATION_JSON);
                    response.end(new JsonObject().put(STATUS, FAILED).put(ERROR, "Id is null").encodePrettily());
                    LOGGER.error("id is null");
                }
                else {
                    if (data!=null) {
                        data.put(METHOD, EVENTBUS_CHECK_MONITORMETRIC);
                        Bootstrap.vertx.eventBus().<JsonObject>request(EVENTBUS_DATABASE, data, handler -> {
                            if (handler.succeeded()) {
                                JsonObject checkUpdateData = handler.result().body();
                                if (!checkUpdateData.containsKey(Constant.ERROR)) {
                                    routingContext.next();
                                }
                            } else {
                                response.setStatusCode(400).putHeader(CONTENT_TYPE, APPLICATION_JSON);
                                response.end(new JsonObject().put(ERROR, handler.cause().getMessage()).put(STATUS, FAILED).encodePrettily());
                                LOGGER.error(handler.cause().getMessage());
                            }

                        });
                    }
                }
                break;

            case "delete":
                LOGGER.debug("delete Route");
                if (routingContext.pathParam("id")!= null) {
                    String id = routingContext.pathParam("id");
                    Bootstrap.vertx.eventBus().<JsonObject>request(EVENTBUS_CHECK_PROMONITORDID, id, deleteid -> {
                        if (deleteid.succeeded()) {
                            routingContext.next();
                        }else {
                            response.setStatusCode(400).putHeader(CONTENT_TYPE, APPLICATION_JSON);
                            response.end(new JsonObject().put(ERROR, deleteid.cause().getMessage()).put(STATUS, FAILED).encodePrettily());
                            LOGGER.error(deleteid.cause().getMessage());
                        }
                    });
                }
                else {
                    response.setStatusCode(400).putHeader(CONTENT_TYPE, APPLICATION_JSON);
                    response.end(new JsonObject().put(ERROR, "id is null").put(STATUS, FAILED).encodePrettily());
                    LOGGER.error("id is null");
                }

                break;

        }
    }

    private void getAll(RoutingContext routingContext) {

    }

    private void getlastInstance(RoutingContext routingContext) {
        try {
            String getId = routingContext.pathParam("id");
            Bootstrap.vertx.eventBus().<JsonObject>request(Constant.EVENTBUS_GET_MONITOR_BY_ID, getId, getLastInstanceHandler -> {
                if (getLastInstanceHandler.succeeded()) {
                    JsonObject getData = getLastInstanceHandler.result().body();
                    LOGGER.debug("Response {} ", getLastInstanceHandler.result().body());
                    routingContext.response().setStatusCode(200).putHeader(CONTENT_TYPE, Constant.APPLICATION_JSON).end(getData.encode());
                }
                else {
                    String result = getLastInstanceHandler.cause().getMessage();
                    routingContext.response().setStatusCode(400).putHeader("content-type", Constant.APPLICATION_JSON).end(result);
                }
            });
        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
        }
    }

    private void delete(RoutingContext routingContext) {
        try {
            String id = routingContext.pathParam("id");

            Bootstrap.vertx.eventBus().<JsonObject>request(MONITOR_ENDPOINT, new JsonObject().put(METHOD, EVENTBUS_DELETE_PROVISION).put(MONITOR_ID, id), deletebyID -> {
                if (deletebyID.succeeded()) {
                    JsonObject deleteResult = deletebyID.result().body();
                    LOGGER.debug("Response {} ", deletebyID.result().body());
                    routingContext.response().setStatusCode(200).putHeader(CONTENT_TYPE, Constant.APPLICATION_JSON).end(deleteResult.encode());
                }
                else {
                    String result = deletebyID.cause().getMessage();
                    routingContext.response().setStatusCode(200).putHeader(CONTENT_TYPE, Constant.APPLICATION_JSON).end(result);
                }
            });

        } catch (Exception exception) {
            routingContext.response().setStatusCode(400).putHeader(CONTENT_TYPE, Constant.APPLICATION_JSON).end(new JsonObject().put(Constant.STATUS, Constant.FAILED).encode());
        }
    }

    private void update(RoutingContext routingContext) {
        try {
            JsonObject createData = routingContext.getBodyAsJson();
            Bootstrap.vertx.eventBus().<JsonObject>request(EVENTBUS_UPDATE_METRIC, createData, updateHandler -> {
                if (updateHandler.succeeded()) {
                    JsonObject dbData = updateHandler.result().body();
                    LOGGER.debug("Response {} ", updateHandler.result().body());
                    routingContext.response().setStatusCode(200).putHeader(CONTENT_TYPE, Constant.APPLICATION_JSON).end(dbData.encode());
                }
                else {
                    routingContext.response().setStatusCode(400).putHeader(CONTENT_TYPE, APPLICATION_JSON);
                    routingContext.response().end(new JsonObject().put(STATUS, FAILED).put(ERROR, updateHandler.cause().getMessage()).encodePrettily());
                    LOGGER.error(updateHandler.cause().getMessage());
                }
            });
        } catch (Exception exception) {
            routingContext.response().setStatusCode(400).putHeader("content-type", Constant.APPLICATION_JSON).end(new JsonObject().put(Constant.STATUS, Constant.FAILED).encode());
        }

    }


}