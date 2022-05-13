package com.mindarray;

import com.mindarray.utility.Utility;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class PollerEngine extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(PollerEngine.class);

    Utility utility = new Utility();

    @Override
    public void start(Promise<Void> startPromise) {
        LOG.debug("POLLER ENGINE DEPLOYED");

        Bootstrap.vertx.eventBus().<JsonObject>consumer(Constant.EVENTBUS_POLLING, polHandler->{
            JsonObject pollingData = polHandler.body();

            pollingData.put("category", "polling");

            ConcurrentHashMap<String, Long> tempTimeData = new ConcurrentHashMap<>();

            ConcurrentHashMap<String, Long> originalTimeData = new ConcurrentHashMap<>();

            Queue<JsonObject> queueData = new LinkedList<>();
            Queue<JsonObject> pollingQueue = new LinkedList<>();
            Bootstrap.vertx.eventBus().<JsonObject>request(Constant.EVENTBUS_GETMETRIC_FOR_POLLING, pollingData, getData -> {
                if (getData.succeeded()){
                    queueData.add(getData.result().body());
                }
            });
            Bootstrap.vertx.setPeriodic(10000, polhandling ->{
                JsonObject originalResult;
                originalResult = queueData.poll();
                JsonObject duplicateResult = new JsonObject(String.valueOf(originalResult));

                JsonObject finalOriginalResult = originalResult;
                duplicateResult.stream().forEach((key)->{
                    var object = duplicateResult.getJsonObject(key.getKey());
                    var time = object.getLong("time");
                    if(time - 10000 <= 0){

                        Iterator<JsonObject> iterator = queueData.iterator();
                        while (iterator.hasNext()) {
                            pollingQueue.add(object);
                            System.out.println("polling hua :" + key.getKey());
                            duplicateResult.put(key.getKey(), finalOriginalResult.getJsonObject(key.getKey()));
                        }
                    }else{
                        object.put("time",time - 10000);
                        duplicateResult.put(key.getKey(), object);
                    }

                });
            });
            while (true){
                Iterator<JsonObject> iterator = pollingQueue.iterator();
                while (iterator.hasNext()){
                    utility.spawning(JsonObject.mapFrom(queueData));
                }
            }
            startPromise.complete();
    });
}
}
