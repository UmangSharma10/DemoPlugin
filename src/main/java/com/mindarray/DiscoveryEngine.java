package com.mindarray;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryEngine extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryEngine.class);
    Utility utility = new Utility();

    JsonObject userData = new JsonObject();

    JsonObject result = new JsonObject();

    @Override
    public void start(Promise<Void> startPromise) {

        LOG.debug("DISCOVERY ENGINE DEPLOYED");

        JsonObject error = new JsonObject();

        vertx.eventBus().consumer(NmsConstant.DISCOVERY_ADDRESS, handler -> {

            userData = (JsonObject) handler.body();

            System.out.println(userData);

            JsonObject validation = utility.validation(userData);

            System.out.println(validation);

            if (!validation.containsKey("error")) {

                vertx.eventBus().request(NmsConstant.DATABASECHECKIP, userData, ret -> {

                    if (ret.succeeded()) {

                        JsonObject check = new JsonObject(ret.result().body().toString());

                        System.out.println(check);

                        if (!check.containsKey("Error")) {

                            vertx.executeBlocking(event -> {

                                try {

                                    result = utility.pingAvailiblity(userData.getString(NmsConstant.IP_ADDRESS).trim());

                                    if (result.getString("status").equals("up")) {

                                       JsonObject trimData = utility.trimData(userData);
                                       userData.mergeIn(trimData);

                                        JsonObject resultPlugin = utility.plugin(userData);

                                        if (resultPlugin.getString("status").equals("success")) {

                                            event.complete(resultPlugin);

                                        } else {

                                            error.put("status", "Plugin Discovery Failed");

                                            event.fail(resultPlugin.encode());

                                        }

                                    }
                                    else if (result.getString("status").equals("down")){
                                        error.put("status", "down");
                                        event.fail(result.encode());
                                    }




                                } catch (NullPointerException e) {

                                    LOG.debug("NUll point Exception");

                                } catch (Exception e) {

                                    throw new RuntimeException(e);

                                }

                            }).onComplete(evehandler -> {

                                if (evehandler.succeeded()) {

                                    //JsonObject trimData = utility.trimData(userData);

                                    vertx.eventBus().request("my.request.db", userData, request -> {

                                        LOG.debug("Response {} ", request.result().body().toString());

                                        JsonObject jsonObject = (JsonObject) request.result().body();

                                        handler.reply(jsonObject);

                                    });
                                } else {

                                    handler.reply(evehandler.cause().toString());

                                }
                            });
                        }
                        else {

                            error.put("status", "Already Discovered");

                            handler.reply(error);

                        }
                    }
                });
            } else {

                handler.reply("Validation Failed" + validation.encode());

            }
        });
        startPromise.complete();
    }
}