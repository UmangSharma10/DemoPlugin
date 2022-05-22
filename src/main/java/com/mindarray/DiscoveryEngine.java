package com.mindarray;

import com.mindarray.utility.Utility;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mindarray.Constant.DIS_ID;

public class DiscoveryEngine extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryEngine.class);

    DatabaseEngine databaseEngine = new DatabaseEngine();
    Utility utility = new Utility();
    @Override
    public void start(Promise<Void> startPromise) {

        LOGGER.debug("DISCOVERY ENGINE DEPLOYED");

        vertx.eventBus().<JsonObject>consumer(Constant.EVENTBUS_DISCOVERY, handler -> {

            JsonObject discoveryCredentials = handler.body();

               Bootstrap.vertx.<JsonObject>executeBlocking(event -> {

                    try{

                        JsonObject result = utility.pingAvailiblity(discoveryCredentials.getString(Constant.IP_ADDRESS));

                        if (result.getString(Constant.STATUS).equals(Constant.UP)) {

                            JsonObject discoveryResult = utility.spawning(discoveryCredentials);

                            if (discoveryResult.getString(Constant.STATUS).equals(Constant.SUCCESS)) {

                                discoveryCredentials.mergeIn(discoveryResult);

                                event.complete(discoveryCredentials);

                            } else {
                                event.fail(discoveryResult.encode());

                            }

                        } else if (result.getString(Constant.STATUS).equals(Constant.DOWN)) {

                            event.fail(result.encode());
                        }

                    }

                    catch (Exception exception){

                        LOGGER.error(exception.getMessage());

                    }

               }).onComplete(resultHandler -> {

                   JsonObject result = new JsonObject();
                   if (resultHandler.succeeded()){
                       JsonObject discoveryData = resultHandler.result();

                   if (!discoveryData.containsKey("error")) {

                       databaseEngine.updateDiscovery(discoveryData.getLong(DIS_ID));

                       result.put(Constant.STATUS, Constant.SUCCESS);

                       result.put("Discovery", Constant.SUCCESS);

                       handler.reply(result);
                   }
               }
                   else {
                       String discoveryData = resultHandler.cause().getMessage();
                       result.put(Constant.STATUS, Constant.FAILED);
                       result.put("Discovery", Constant.FAILED);
                       result.put(Constant.ERROR, discoveryData);

                       handler.reply(result);

                   }

               });

        });

        startPromise.complete();

    }

}