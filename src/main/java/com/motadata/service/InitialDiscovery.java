package com.motadata.service;

import com.motadata.interpreter.Discovery;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InitialDiscovery {
    public static final Logger LOG = LoggerFactory.getLogger(Discovery.class);
    public HashMap<String, String> Fping(JsonObject jsonObject) throws Exception {
        HashMap<String, String> myMap = new HashMap<>();
        ArrayList<String> commandList = new ArrayList<>();

        commandList.add("fping");

        commandList.add("-q");

        commandList.add("-c");

        commandList.add("3");

        commandList.add("-t");

        commandList.add("500");

        String ip = jsonObject.getString("host");

        commandList.add(ip);

        ProcessBuilder build = new ProcessBuilder(commandList);

        Process process = build.start();

        // to read the output
        BufferedReader input = new BufferedReader(new InputStreamReader
                (process.getInputStream()));

        BufferedReader Error = new BufferedReader(new InputStreamReader
                (process.getErrorStream()));

        String s;

        while ((s = input.readLine()) != null) {
            LOG.debug(s);
        }

        LOG.debug("error (if any): ");
        while ((s = Error.readLine()) != null) {
            LOG.debug(s);

            String[] s1 = s.split(":");

            String[] s2 = s1[1].split(",");

            String[] s3 = s2[0].split("=");

            if (s2.length == 1) {

                String[] loss = s3[1].split("/");

                myMap.put("packetxmt", loss[0]);


                myMap.put("packetrcv", loss[1]);

            }

        }
        return myMap;
    }
}
