package com.motadata.interpreter;

import com.motadata.data.DiscoverCredentials;
import com.motadata.repo.DiscoveryRepo;
import com.motadata.service.InitialDiscovery;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.SQLException;


public class Discovery extends AbstractVerticle {
    public static final Logger LOG = LoggerFactory.getLogger(Discovery.class);
    InitialDiscovery initialDiscovery = new InitialDiscovery();
    DiscoveryRepo repo = new DiscoveryRepo();

    DiscoverCredentials credentials;
    JsonObject jsonObject = new JsonObject();
    public Discovery() throws SQLException, ClassNotFoundException {
    }
    @Override
    public void start(Promise<Void> startPromise) {

        EventBus eventBus = vertx.eventBus();

        eventBus.consumer("my.request.address", handler -> {

            jsonObject = (JsonObject) handler.body();

            Boolean result = false;

            try {

                if(!repo.CheckIP(jsonObject)) {

                    result = initialDiscovery.Fping(jsonObject);
                }
                else {
                    handler.reply("Already Discovered");
                    jsonObject.put("discovery", "false");
                    initialDiscovery.Plugin(jsonObject);
                    jsonObject.remove("discovery");
                }

                if (result) {
                    jsonObject.put("discovery" , "true");
                    Boolean outcome = initialDiscovery.Plugin(jsonObject);
                    jsonObject.remove("discovery");

                    if (outcome) {
                        eventBus.request("my.request.db", jsonObject, req -> {

                            LOG.debug("Response {} ", req.result().body().toString());

                            if (req.result().body().toString().equals("Inserted into Database")) {

                                handler.reply("Discovery Successful" + "Added into Database");

                            } else if (req.result().body().toString().equals("Duplicate IP address")) {

                                handler.reply("Discovery Failed" + " IP Already Inserted in DB");
                            }

                        });

                    } else {
                        handler.reply("Discovery Failed");
                    }
                }
                else {
                    handler.reply("Ping Discovery Failed");
                }
        } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });
        startPromise.complete();

    }
}