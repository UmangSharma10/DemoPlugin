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
import java.util.HashMap;


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

            HashMap<String,String> hashMap;

            try {

               hashMap = initialDiscovery.Fping(jsonObject);

            } catch (Exception e) {

                throw new RuntimeException(e);

            }

            if (hashMap.get("packetrcv").equals("3")) {

                try {

                    repo.Create(credentials);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                handler.reply("Initial Discovery Successful");

            }

            else {

                handler.reply("Discovery Failed");

            }
        });

    }


}