package com.mindarray.utility;

import com.mindarray.APIServer;
import com.mindarray.Constant;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

public class Utility {
    private static final Logger LOGGER = LoggerFactory.getLogger(APIServer.class);

    public void trimData(JsonObject userData) {

        if (userData.containsKey(Constant.IP_ADDRESS) && userData.getString(Constant.IP_ADDRESS) != null) {

            userData.put(Constant.IP_ADDRESS, userData.getString(Constant.IP_ADDRESS).trim());
        }
        if (userData.containsKey(Constant.USER) && userData.getString(Constant.USER) != null) {

            userData.put(Constant.USER, userData.getString(Constant.USER).trim());
        }
        if (userData.containsKey(Constant.PASSWORD) && userData.getString(Constant.PASSWORD) != null) {

            userData.put(Constant.PASSWORD, userData.getString(Constant.PASSWORD).trim());
        }
        if (userData.containsKey(Constant.METRIC_TYPE) && userData.getString(Constant.METRIC_TYPE) != null) {

            userData.put(Constant.METRIC_TYPE, userData.getString(Constant.METRIC_TYPE).trim());
        }
        if (userData.containsKey(Constant.PORT) && userData.getString(Constant.PORT) != null) {

            userData.put(Constant.PORT, userData.getString(Constant.PORT));
        }
        if (userData.containsKey(Constant.COMMUNITY) && userData.getString(Constant.COMMUNITY) != null) {

            userData.put(Constant.COMMUNITY, userData.getString(Constant.COMMUNITY).trim());
        }
        if (userData.containsKey(Constant.VERSION) && userData.getString(Constant.VERSION) != null) {

            userData.put(Constant.VERSION, userData.getString(Constant.VERSION).trim());
        }
    }

    public JsonObject pingAvailiblity(String ip) throws Exception {
        JsonObject ping = new JsonObject();
        HashMap<String, String> myMap = new HashMap<>();
        ArrayList<String> commandList = new ArrayList<>();

        commandList.add("fping");

        commandList.add("-q");

        commandList.add("-c");

        commandList.add("3");

        commandList.add("-t");

        commandList.add("1000");

        commandList.add(ip);

        ProcessBuilder build = new ProcessBuilder(commandList);

        Process process = build.start();

        // to read the output
        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));

        BufferedReader Error = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        String readPing;

        while ((readPing = input.readLine()) != null) {
            LOGGER.debug(readPing);
        }

        LOGGER.debug("error (if any): ");
        while ((readPing = Error.readLine()) != null) {
            LOGGER.debug(readPing);

            String[] s1 = readPing.split(":");

            String[] s2 = s1[1].split(",");

            String[] s3 = s2[0].split("=");

            if (s2.length == 2) {

                String[] loss = s3[1].split("/");

                myMap.put("packetxmt", loss[0]);
                myMap.put("packetrcv", loss[1]);

            } else if (s2.length == 1) {
                myMap.put("packetrcv", "0");

            }

        }
        if (myMap.get("packetrcv").equals("3")) {
            ping.put(Constant.STATUS, Constant.UP);
        } else {
            ping.put(Constant.STATUS, Constant.DOWN);
        }
        input.close();
        Error.close();


        return ping;
    }


    public JsonObject spawning(JsonObject pluginJson) {
        JsonObject result = new JsonObject();
        try {
            List<String> commands = new ArrayList<>();

            commands.add("/home/umang/GolandProjects/NmsLite/plugin.exe");

            String encodedString = Base64.getEncoder().encodeToString(pluginJson.encode().getBytes(StandardCharsets.UTF_8));

            commands.add(encodedString);

            ProcessBuilder processBuilder = new ProcessBuilder(commands);

            Process process = processBuilder.start();

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String readInput;
            String decoder;

            while ((readInput = stdInput.readLine()) != null) {
                byte[] decodedBytes = Base64.getDecoder().decode(readInput);
                decoder = new String(decodedBytes);
                result = new JsonObject(decoder);

            }
            while ((readInput = stdError.readLine()) != null) {
                byte[] decodedBytes = Base64.getDecoder().decode(readInput);
                decoder = new String(decodedBytes);
                result = new JsonObject(decoder);

            }
            result.remove("category");
            stdInput.close();
            stdError.close();
        } catch (IOException exception) {

            LOGGER.debug("IOEXCEPTION");

        }


        return result;

    }

}



