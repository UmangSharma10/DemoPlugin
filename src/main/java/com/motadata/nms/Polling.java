package com.motadata.nms;

import io.vertx.core.json.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Polling {
    public String metricDiscovering(JsonObject jsonObject) {
        String result ="";
        try {
            List<String> commands = new ArrayList<>();

            commands.add("/home/umang/GolandProjects/NmsLite/plugin.exe");

            String encodedString = Base64.getEncoder().encodeToString(jsonObject.encode().getBytes(StandardCharsets.UTF_8));

            commands.add(encodedString);

            ProcessBuilder processBuilder = new ProcessBuilder(commands);

            Process process = processBuilder.start();

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(

                    process.getInputStream()));

            String readInput;

            while ((readInput = stdInput.readLine()) != null) {

                result = readInput;

            }
            stdInput.close();
        } catch (IOException e) {

            e.printStackTrace();

        }


        return result;

    }
}
