package com.mindarray.api;

import com.mindarray.APIServer;
import com.mindarray.Bootstrap;
import com.mindarray.Constant;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class Discovery {
    private static final Logger LOGGER = LoggerFactory.getLogger(APIServer.class);

    public void init(Router discoveryRoute) {

        LOGGER.debug("Discovery Route deployed");

        discoveryRoute.post("/discovery").setName("create").handler(this::validate).handler(this::create);

        discoveryRoute.get("/discovery/:id").setName("get").handler(this::validate).handler(this::getById);

        discoveryRoute.get("/discovery").setName("getAll").handler(this::validate).handler(this::getAll);

        discoveryRoute.delete("/discovery/:id").setName("delete").handler(this::validate).handler(this::delete);

        discoveryRoute.put("/discovery").setName("update").handler(this::validate).handler(this::update);

        discoveryRoute.post("/provision").setName("create").handler(this::createProvision);
    }



    private void validate(RoutingContext routingContext) {

        JsonObject trimData = routingContext.getBodyAsJson();


        if (routingContext.currentRoute().getName().equals("create") || routingContext.currentRoute().getName().equals("update")) {

            try {

                HashMap<String, Object> result;

                if (!(trimData == null)) {

                    result = new HashMap<>(trimData.getMap());

                    for (String key : result.keySet()) {

                        var val = result.get(key);

                        if (val instanceof String) {

                            result.put(key, val.toString().trim());
                        }

                        trimData = new JsonObject(result);

                        routingContext.setBody(trimData.toBuffer());
                    }
                }

                else
                {

                    routingContext.response().setStatusCode(400).putHeader("content-type", Constant.CONTENT_TYPE).end(new JsonObject().put(Constant.STATUS, Constant.FAILED).encode());

                }
            }

            catch (Exception exception) {

                routingContext.response().setStatusCode(400).putHeader("content-type", Constant.CONTENT_TYPE).end(new JsonObject().put(Constant.STATUS, Constant.FAILED).encode());
            }
        }

        switch (routingContext.currentRoute().getName()) {
            case "create":
                LOGGER.debug("Create Route");

                Bootstrap.vertx.eventBus().<JsonObject>request(Constant.EVENTBUS_CHECK_DISNAME, trimData, handler -> {
                    if (handler.succeeded()) {
                        JsonObject checkNameData = handler.result().body();
                        if (!checkNameData.containsKey(Constant.ERROR)) {
                           routingContext.next();
                        } else {
                            routingContext.response().setStatusCode(400).putHeader("content-type", Constant.CONTENT_TYPE).end(checkNameData.encode());
                        }
                    }
                });
                break;
            case "delete":
                LOGGER.debug("delete Route");
                String id = routingContext.pathParam("id");
                Bootstrap.vertx.eventBus().<JsonObject>request(Constant.EVENTBUS_CHECKID_DISCOVERY, id, deleteid -> {
                    if (deleteid.succeeded()) {
                        JsonObject deleteIdData = deleteid.result().body();
                        if (!deleteIdData.containsKey(Constant.ERROR)) {
                           routingContext.next();
                        } else {
                            routingContext.response().setStatusCode(400).putHeader("content-type", Constant.CONTENT_TYPE).end(deleteIdData.encode());
                        }
                    }
                    else {
                        LOGGER.error("failed");
                    }
                });
                break;

            case "update":
                LOGGER.debug("Update Route");
                Bootstrap.vertx.eventBus().<JsonObject>request(Constant.EVENTBUS_CHECKID_JSON, trimData, handler -> {
                    if (handler.succeeded()) {
                        JsonObject checkUpdateData = handler.result().body();
                        if (!checkUpdateData.containsKey(Constant.ERROR)) {
                            routingContext.next();
                        } else {
                            routingContext.response().setStatusCode(400).putHeader("content-type", Constant.CONTENT_TYPE).end(checkUpdateData.encode());
                        }
                    }
                });
                break;
            case "get":
                LOGGER.debug("Get Routing");
                String getId = routingContext.pathParam("id");
                Bootstrap.vertx.eventBus().<JsonObject>request(Constant.EVENTBUS_CHECKID_DISCOVERY, getId , get ->{
                   if (get.succeeded()){
                       JsonObject getDisData = get.result().body();
                       if (!getDisData.containsKey(Constant.ERROR)){
                           routingContext.next();
                       }
                       else {
                           routingContext.response().setStatusCode(400).putHeader("content-type", Constant.CONTENT_TYPE).end(getDisData.encode());
                       }
                   }
                   else {
                       LOGGER.error("Error");
                   }
                });
                break;
            case "getAll" :
                LOGGER.debug("Get ALL");
                routingContext.next();

        }
    }

    private void update(RoutingContext routingContext) {
        try {
            JsonObject createData = routingContext.getBodyAsJson();
            LOGGER.debug(createData.encode());
            Bootstrap.vertx.eventBus().<JsonObject>request(Constant.EVENTBUS_UPDATE, createData, createHandler -> {
                JsonObject dbData = createHandler.result().body();
                LOGGER.debug("Response {} ", createHandler.result().body().toString());
                routingContext.response().setStatusCode(200).putHeader("content-type", Constant.CONTENT_TYPE).end(dbData.encode());
            });
        } catch (Exception exception) {
            routingContext.response().setStatusCode(400).putHeader("content-type", Constant.CONTENT_TYPE).end(new JsonObject().put(Constant.STATUS, Constant.FAILED).encode());
        }
    }

    private void delete(RoutingContext routingContext) {
        try {
             String id = routingContext.pathParam("id");
            System.out.println(id);

            Bootstrap.vertx.eventBus().<JsonObject>request(Constant.EVENTBUS_DELETEDIS, id, deletebyID -> {
                JsonObject deleteResult = deletebyID.result().body();
                LOGGER.debug("Response {} ", deletebyID.result().body().toString());
                routingContext.response().setStatusCode(200).putHeader("content-type", Constant.CONTENT_TYPE).end(deleteResult.encode());
            });

        } catch (Exception exception) {
            routingContext.response().setStatusCode(400).putHeader("content-type", Constant.CONTENT_TYPE).end(new JsonObject().put(Constant.STATUS, Constant.FAILED).encode());
        }
    }

    private void getById(RoutingContext routingContext) {
    try {
        String getId = routingContext.pathParam("id");
        Bootstrap.vertx.eventBus().<JsonObject>request(Constant.EVENTBUS_GETDISCOVERY, getId, createHandler -> {
            JsonObject getData = createHandler.result().body();
            LOGGER.debug("Response {} ", createHandler.result().body().toString());
            routingContext.response().setStatusCode(200).putHeader("content-type", Constant.CONTENT_TYPE).end(getData.encode());
        });
    } catch (Exception exception) {
        routingContext.response().setStatusCode(400).putHeader("content-type", Constant.CONTENT_TYPE).end(new JsonObject().put(Constant.STATUS, Constant.FAILED).encode());
    }

    }

    private void getAll(RoutingContext routingContext) {
        try {
           String id = "getAll";
            Bootstrap.vertx.eventBus().<JsonObject>request(Constant.EVENTBUS_GETALL, id, createHandler -> {
                JsonObject getData = createHandler.result().body();
                LOGGER.debug("Response {} ", createHandler.result().body().toString());
                routingContext.response().setStatusCode(200).putHeader("content-type", Constant.CONTENT_TYPE).end(getData.encode());
            });
        } catch (Exception exception) {
            routingContext.response().setStatusCode(400).putHeader("content-type", Constant.CONTENT_TYPE).end(new JsonObject().put(Constant.STATUS, Constant.FAILED).encode());
        }
    }

    private void create(RoutingContext routingContext) {
        try {
            JsonObject createData = routingContext.getBodyAsJson();
            LOGGER.debug(createData.encode());
            Bootstrap.vertx.eventBus().<JsonObject>request(Constant.EVENTBUS_INSERTDISCOVERY, createData, createHandler -> {
                JsonObject dbData = createHandler.result().body();
                LOGGER.debug("Response {} ", createHandler.result().body().toString());
                routingContext.response().setStatusCode(200).putHeader("content-type", Constant.CONTENT_TYPE).end(dbData.encode());
            });
        } catch (Exception exception) {
            routingContext.response().setStatusCode(400).putHeader("content-type", Constant.CONTENT_TYPE).end(new JsonObject().put(Constant.STATUS, Constant.FAILED).encode());
        }
    }

    private void createProvision(RoutingContext routingContext) {
    }

}
