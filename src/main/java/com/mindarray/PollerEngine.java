package com.mindarray;

import com.mindarray.utility.Utility;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PollerEngine extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(PollerEngine.class);

    Utility utility = new Utility();

    DatabaseEngine databaseEngine = new DatabaseEngine();

    @Override
    public void start(Promise<Void> startPromise) {
        LOG.debug("POLLER ENGINE DEPLOYED");

        Bootstrap.vertx.eventBus().<JsonObject>consumer(Constant.EVENTBUS_POLLING, polHandler -> {
            JsonObject pollingData = polHandler.body();

            ConcurrentLinkedQueue<JsonObject> queueData = new ConcurrentLinkedQueue<>();

            ConcurrentLinkedQueue<JsonObject> pollingQueue = new ConcurrentLinkedQueue<>();

            HashMap<String, Long> orginal = new HashMap<>();

            HashMap<String, Long> schedulingData = new HashMap<>();

            HashMap<String, JsonObject> contextMap = new HashMap<>();

            Bootstrap.vertx.eventBus().<JsonObject>request(Constant.EVENTBUS_GETMETRIC_FOR_POLLING, pollingData, getData -> {

                JsonObject result = new JsonObject();
                if (getData.succeeded()) {

                    JsonObject entries = getData.result().body();

                    entries.stream().forEach((key) -> {
                        var object = entries.getJsonObject(key.getKey());
                        queueData.add(object);
                        result.put("MonitorId" , object.getLong("monitorId"));
                    });

                    while (!queueData.isEmpty()) {

                        JsonObject data = queueData.poll();

                        if (data != null) {

                            orginal.put(data.getString(Constant.IPANDGROUP), data.getLong("time"));

                            schedulingData.put(data.getString(Constant.IPANDGROUP), data.getLong("time"));

                            contextMap.put(data.getString(Constant.IPANDGROUP), data);
                        }

                    }



                    result.put(Constant.STATUS, Constant.SUCCESS);

                    polHandler.reply(result);

                } else {

                    polHandler.fail(-1, getData.cause().getMessage());

                }

            });





                Bootstrap.vertx.setPeriodic(10000, polhandling -> {

                    for (Map.Entry<String, Long> mapElement : schedulingData.entrySet()) {

                            long time = mapElement.getValue();

                            if (time <= 0) {

                                pollingQueue.add(contextMap.get(mapElement.getKey()));

                                schedulingData.put(mapElement.getKey(), orginal.get(mapElement.getKey()));

                                queueData.add(contextMap.get(mapElement.getKey()));


                            }

                            else

                            {
                                time = time - 10000;

                                schedulingData.put(mapElement.getKey(), time);

                                if (time <= 0) {

                                    pollingQueue.add(contextMap.get(mapElement.getKey()));

                                    schedulingData.put(mapElement.getKey(), orginal.get(mapElement.getKey()));

                                    queueData.add(contextMap.get(mapElement.getKey()));
                                }


                            }
                        }


                });






                    Thread callPlugin = new Thread(() -> {
                while (true){
                    try {

                        if(!pollingQueue.isEmpty()){

                            Iterator<JsonObject> iterator = pollingQueue.iterator();

                            while (iterator.hasNext()){
                                JsonObject value = pollingQueue.poll();

                                if (value != null) {
                                    JsonObject result = utility.spawning(value);
                                    databaseEngine.insertIntoDumpData(result);


                                }


                            }
                        }

                    }

                    catch (Exception exception){

                       LOG.error(exception.getMessage());

                    }

                }
            });

              callPlugin.start();


        });
        startPromise.complete();
    }
}
