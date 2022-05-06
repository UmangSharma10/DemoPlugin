package com.mindarray;

import com.mindarray.utility.Utility;
import com.mindarray.utility.ValidationUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryEngine extends AbstractVerticle {

    Utility utility = new Utility();
    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryEngine.class);

    @Override
    public void start(Promise<Void> startPromise) {

        LOGGER.debug("DISCOVERY ENGINE DEPLOYED");

        vertx.eventBus().consumer(Constant.EVENTBUS_DISCOVERY, handler -> {

            JsonObject discoveryCredentials = new JsonObject(handler.body().toString());

            utility.trimData(discoveryCredentials);


            JsonObject validationResult = ValidationUtil.validation(discoveryCredentials);

            if (!validationResult.containsKey(Constant.ERROR)) {

                vertx.eventBus().request(Constant.EVENTBUS_CHECKIP, discoveryCredentials, checkipmessage -> {

                    if (checkipmessage.succeeded()) {

                        JsonObject jsonCheckIpData = new JsonObject(checkipmessage.result().body().toString());

                        if (!jsonCheckIpData.containsKey(Constant.ERROR)) {

                            vertx.executeBlocking(event -> {

                                try {

                                    JsonObject result = utility.pingAvailiblity(discoveryCredentials.getString(Constant.IP_ADDRESS));

                                    if (result.getString(Constant.STATUS).equals(Constant.UP)) {

                                        JsonObject discoveryResult = utility.discovery(discoveryCredentials);

                                        if (discoveryResult.getString(Constant.STATUS).equals(Constant.SUCCESS)) {

                                            event.complete( discoveryCredentials );

                                        } else {
                                            event.fail(discoveryResult.encode());

                                        }

                                    } else if (result.getString(Constant.STATUS).equals(Constant.DOWN)) {

                                        event.fail(result.encode());
                                    }


                                } catch (NullPointerException e) {

                                    LOGGER.debug("NUll point Exception");

                                } catch (Exception e) {

                                    throw new RuntimeException(e);

                                }

                            }).onComplete(eventDbhandler -> {

                                if (eventDbhandler.succeeded()) {

                                    vertx.eventBus().request(Constant.EVENTBUS_INSERTDB, discoveryCredentials, request -> {

                                        LOGGER.debug("Response {} ", request.result().body().toString());

                                        JsonObject jsonObject = (JsonObject) request.result().body();

                                        handler.reply(jsonObject);

                                    });
                                } else {

                                    handler.reply(eventDbhandler.cause().toString());

                                }
                            });
                        } else {

                            handler.reply(jsonCheckIpData);

                        }
                    }
                });
            } else {

                handler.reply("Validation Failed" + validationResult.encode());

            }
        });
        startPromise.complete();
    }
}