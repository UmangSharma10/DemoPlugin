package com.motadata.nms;

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
    Polling polling = new Polling();
    DiscoveryRepo repo = new DiscoveryRepo();
    JsonObject jsonObject = new JsonObject();

    Validation validation = new Validation();

    public Discovery() throws SQLException, ClassNotFoundException {
    }

    @Override
    public void start(Promise<Void> startPromise) {

        LOG.debug("Discovery Deployed");
        EventBus eventBus = vertx.eventBus();

        eventBus.consumer("my.request.address", handler -> {

            jsonObject = (JsonObject) handler.body();

            Boolean result = false;

            String device = jsonObject.getString("device").trim();
                    String port = jsonObject.getString("port").trim();
                            String user = jsonObject.getString("user").trim();
                                    String password = jsonObject.getString("password").trim();
            String ip = jsonObject.getString("host").trim();

            JsonObject trimData = new JsonObject();
            trimData.put("host", ip);
            trimData.put("device", device);
            trimData.put("user", user);
            trimData.put("password", password);
            trimData.put("port", port);



        if(validation.isValidIp(ip)) {
            try {

                if (!repo.checkIP(ip)) {

                    result = initialDiscovery.pingAvailiblity(ip);

                } else {

                    handler.reply("Already Discovered");

                    jsonObject.put("category", "polling");

                    polling.metricDiscovering(trimData);
                    //not metric type but polling

                    jsonObject.remove("category");
                }


                if (result) {
                    jsonObject.put("category", "discovery");
                    String outcome = polling.metricDiscovering(trimData);
                    jsonObject.remove("category");

                    if (outcome.equals("success")) {
                        eventBus.request("my.request.db", trimData, req -> {

                            LOG.debug("Response {} ", req.result().body().toString());

                            if (req.result().body().toString().equals("Inserted into Database")) {

                                handler.reply("Discovery Successful" + " Credentials Added into Database");

                            } else if (req.result().body().toString().equals("Duplicate IP address")) {

                                handler.reply("Discovery Failed" + " IP Already Inserted in DB");
                            }

                        });

                    } else {
                        handler.reply("Discovery Failed");
                    }
                } else {
                    handler.reply("Ping Discovery Failed");
                }
            } catch (NullPointerException e) {
                LOG.debug("Null Pointer Exception");
            } catch (RuntimeException e) {
                LOG.debug("Runtime Exception");
            } catch (Exception e) {
                LOG.debug(e.toString());
            }
        }
        else {
            handler.reply("Ip address not valid");
        }
        });
        startPromise.complete();

    }


}

