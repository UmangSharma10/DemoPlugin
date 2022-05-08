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

        discoveryRoute.get("/discovery").setName("get").handler(this::validate).handler(this::get);

        discoveryRoute.put("/discovery").setName("delete").handler(this::validate).handler(this::delete);

        discoveryRoute.delete("/discovery").setName("update").handler(this::validate).handler(this::update);
    }


    private void update(RoutingContext routingContext) {

    }

    private void delete(RoutingContext routingContext) {
        try {

            JsonObject deleteData = routingContext.getBodyAsJson();

            LOGGER.debug(deleteData.encode());

            Bootstrap.vertx.eventBus().<JsonObject>request(Constant.EVENTBUS_DELETE, deleteData, deletebyID ->{
                JsonObject deleteResult = deletebyID.result().body();
                LOGGER.debug("Response {} ", deletebyID.result().body().toString());
                routingContext.response().setStatusCode(200).putHeader("content-type", Constant.CONTENT_TYPE).end(deleteResult.encode());
            });

        }catch (Exception exception){
            routingContext.response().setStatusCode(400).putHeader("content-type", Constant.CONTENT_TYPE).end(new JsonObject().put(Constant.STATUS, Constant.FAILED).encode());
        }
    }

    private void get(RoutingContext routingContext) {
    }

    private void validate(RoutingContext routingContext) {
       JsonObject trimData = routingContext.getBodyAsJson();
        try {
        HashMap<String, Object> result;
        if (!(trimData == null)) {

            result = new HashMap<>(trimData.getMap());

            for (String key : result.keySet()) {

                var val = result.get(key);

                if(val instanceof String) {
                    result.put(key, val.toString().trim());
                }
            }

            trimData = new JsonObject(result);
            JsonObject finalTrimData = trimData;

            switch (routingContext.currentRoute().getName()){
                case "create" :
                    LOGGER.debug("Create Route");
                    Bootstrap.vertx.eventBus().<JsonObject>request(Constant.EVENTBUS_CHECK_DISNAME, trimData, handler ->{
                        if (handler.succeeded()){
                            JsonObject checkNameData = handler.result().body();
                            if(!checkNameData.containsKey(Constant.ERROR)){
                                routingContext.setBody(finalTrimData.toBuffer());
                                LOGGER.info(finalTrimData.encode());
                                routingContext.next();
                            }else{
                                routingContext.response().setStatusCode(400).putHeader("content-type", Constant.CONTENT_TYPE).end(checkNameData.encode());
                            }
                        }
                    });
                case "delete" :
                    LOGGER.debug("delete Route");
                    Bootstrap.vertx.eventBus().<JsonObject>request(Constant.EVENTBUS_CHECKID_DISCOVERY, trimData , deleteid ->{
                        if (deleteid.succeeded()){
                            JsonObject deleteIdData = deleteid.result().body();
                            if (!deleteIdData.containsKey(Constant.ERROR)){
                                routingContext.setBody(finalTrimData.toBuffer());
                                LOGGER.info(finalTrimData.encode());
                                routingContext.next();
                            }else{
                                routingContext.response().setStatusCode(400).putHeader("content-type", Constant.CONTENT_TYPE).end(deleteIdData.encode());
                            }
                        }
                    });

                case "update" :


            }
        }
        else
        {
            routingContext.response().setStatusCode(400).putHeader("content-type", Constant.CONTENT_TYPE).end(new JsonObject().put(Constant.STATUS, Constant.FAILED).encode());
        }

    }catch(Exception exception)
        {
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
        }catch (Exception exception){
            routingContext.response().setStatusCode(400).putHeader("content-type", Constant.CONTENT_TYPE).end(new JsonObject().put(Constant.STATUS, Constant.FAILED).encode());
        }
    }

}
