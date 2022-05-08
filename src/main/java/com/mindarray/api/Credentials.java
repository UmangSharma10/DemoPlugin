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

public class Credentials  {

    private static final Logger LOGGER = LoggerFactory.getLogger(APIServer.class);

    public void init(Router discoveryRoute) {

        LOGGER.debug("Cred Route Deployed");

        discoveryRoute.post("/credential").setName("create").handler(this::validate).handler(this::create);
        discoveryRoute.get("/credential").setName("GetByID").handler(this::validate).handler(this::getByID);
        discoveryRoute.get("/credential").setName("GET").handler(this::validate).handler(this::get);
        discoveryRoute.put("/credential").setName("DELETE").handler(this::validate).handler(this::delete);
        discoveryRoute.delete("/credential").setName("PUT").handler(this::validate).handler(this::update);
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
                Bootstrap.vertx.eventBus().<JsonObject>request(Constant.EVENTBUS_CHECK_CREDNAME, trimData, handler ->{
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


    private void get(RoutingContext routingContext) {
    }

    private void update(RoutingContext routingContext) {
    }

    private void delete(RoutingContext routingContext) {
    }

    private void getByID(RoutingContext routingContext) {
    }

    private void create(RoutingContext routingContext) {
        try {
            JsonObject createData = routingContext.getBodyAsJson();
            System.out.println("data in create " + createData);
            Bootstrap.vertx.eventBus().<JsonObject>request(Constant.EVENTBUS_INSERTCRED, createData, createHandler -> {
                JsonObject dbData = createHandler.result().body();
                LOGGER.debug("Response {} ", createHandler.result().body().toString());
                routingContext.response().setStatusCode(200).putHeader("content-type", Constant.CONTENT_TYPE).end(dbData.encode());
            });
        }catch (Exception exception){
            routingContext.response().setStatusCode(400).putHeader("content-type", Constant.CONTENT_TYPE).end(new JsonObject().put(Constant.STATUS, Constant.FAILED).encode());
        }
    }

}
