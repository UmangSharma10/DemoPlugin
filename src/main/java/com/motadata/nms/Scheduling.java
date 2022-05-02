package com.motadata.nms;

import io.vertx.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class Scheduling extends AbstractVerticle {

    public static final Logger LOG = LoggerFactory.getLogger(Scheduling.class);

    @Override
    public void start() throws Exception {


        long time = vertx.setPeriodic(10000, schedule -> {

        });
    }

    public static void functionProcess(String Credentials){
        try {
            List<String> commands = new ArrayList<>();
            commands.add("/home/umang/GolandProjects/sample/plugin.exe");
            // String encodedString = Base64.getEncoder().encodeToString(jsonArray.toString().getBytes(StandardCharsets.UTF_8));


            commands.add(Credentials);
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            Process process = processBuilder.start();

            // for reading the output from stream
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            String s;
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




}

