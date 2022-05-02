package com.motadata.nms;

import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validation {
    public static final Logger LOG = LoggerFactory.getLogger(Validation.class);
    private static final String INET4ADDRESS = null;

    private static final String IPV4_REGEX =
            "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    private static final Pattern IPv4_PATTERN = Pattern.compile(IPV4_REGEX);

    JsonObject ErrorData = new JsonObject();
    public boolean isValidIp(String ip)
    {
        if (ip == null) {
            return false;
        }

        Matcher matcher = IPv4_PATTERN.matcher(ip);

        return matcher.matches();
    }

    public boolean isValidString(String string) {

        // Regex to check valid username.
        String regex = "^[A-Za-z]$";

        // Compile the ReGex
        Pattern p = Pattern.compile(regex);

        // If the username is empty
        // return false
        if (string == null) {
            return false;
        }

        Matcher m = p.matcher(string);
        if(m.find() && m.group().equals(string))
            System.out.println(string + " is a valid string");
        else
            System.out.println(string + " is not a valid string");

        // Return if the username
        // matched the ReGex
        return m.matches();

    }




}

