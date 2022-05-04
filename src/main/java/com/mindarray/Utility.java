package com.mindarray;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utility {
    private static final Logger LOG = LoggerFactory.getLogger(APIServer.class);

    public JsonObject trimData(JsonObject userData) {
        String device = userData.getString("device");
        String port = userData.getString("port");
        String user = userData.getString("user");
        String password = userData.getString("password");
        String ip = userData.getString("host");

        JsonObject trimData = new JsonObject();

        if (!ip.isEmpty()) {
            trimData.put("host", ip.trim());
        }

        if (!device.isEmpty()) {
            trimData.put("device", device.trim());
        }

        if (!user.isEmpty()) {
            trimData.put("user", user.trim());
        }


        if (!password.isEmpty()) {
            trimData.put("password", password.trim());
        }
        if (!port.isEmpty()) {
            trimData.put("port", port.trim());
        }

        return trimData;

    }

    private static final String IPV4_REGEX =
            "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    private static final Pattern IPv4_PATTERN = Pattern.compile(IPV4_REGEX);


    public boolean isValidIp(String ip) {
        if (ip == null) {
            return false;
        }

        Matcher matcher = IPv4_PATTERN.matcher(ip);

        return matcher.matches();
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
            LOG.debug(readPing);
        }

        LOG.debug("error (if any): ");
        while ((readPing = Error.readLine()) != null) {
            LOG.debug(readPing);

            String[] s1 = readPing.split(":");

            String[] s2 = s1[1].split(",");

            String[] s3 = s2[0].split("=");

            if (s2.length == 2) {

                String[] loss = s3[1].split("/");

                myMap.put("packetxmt", loss[0]);


                myMap.put("packetrcv", loss[1]);

            }

        }
        input.close();
        Error.close();

        if (myMap.get("packetrcv").equals("3")) {
            ping.put("status", "up");
        } else {
            ping.put("status", "down");
        }
        return ping;
    }

    public static final String PORT = "^(0|6[0-5][0-5][0-3][0-5]|[1-5][0-9][0-9][0-9][0-9]|[1-9][0-9]{0,3})$";
    public static final Pattern PoRT = Pattern.compile(PORT);

    public boolean isValidPort(String port) {
        if (port == null) {
            return false;
        }

        Matcher matcher = PoRT.matcher(port);

        return matcher.matches();


    }


    public JsonObject validation(JsonObject validation) {

        JsonObject result = new JsonObject();
        List<String> listErrors = new ArrayList<>();

        if (validation.getString("user").isBlank()) {
            listErrors.add("User is Invalid");
        } else if (validation.getString("password").isBlank()) {
            listErrors.add("password is invalid");
        } else if (validation.getString("port").isBlank()) {
            listErrors.add("port is invalid");
        } else if (!isValidPort(validation.getString("port"))) {
            listErrors.add("port is invalid");
        } else if (validation.getString("device").isBlank()) {
            listErrors.add("device is invalid");
        } else if (validation.getString("host").isBlank()) {
            listErrors.add("ip is invalid");
        } else if (!isValidIp(validation.getString("host"))) {
            listErrors.add("ip is not valid");
        }
        if (listErrors.isEmpty()) {
            result.put("status", "Success");
        } else {
            result.put("status", "failed");
            result.put("error", listErrors);
        }

        return result;

    }

    public JsonObject plugin(JsonObject pluginJson) {
        JsonObject result = new JsonObject();
        pluginJson.put("category", "discovery");
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

            if ((readInput = stdInput.readLine()) != null) {
                result.put("status", readInput);
            }
            if ((readInput = stdError.readLine()) != null) {
                result.put("status", readInput);

            }

            stdInput.close();
            stdError.close();
        } catch (IOException e) {

            e.printStackTrace();

        }
        pluginJson.remove("category");


        return result;

    }

}



