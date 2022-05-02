package com.motadata.nms;

import io.vertx.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Scheduling extends AbstractVerticle {

    public static final Logger LOG = LoggerFactory.getLogger(Scheduling.class);

    @Override
    public void start() throws Exception {


        long time = vertx.setPeriodic(10000, schedule -> {

        });
    }




}

