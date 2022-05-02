package com.motadata.nms;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DiscoveryRepo extends AbstractVerticle {
    public static final Logger LOG = LoggerFactory.getLogger(DiscoveryRepo.class);
    Connection con = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/DiscoveryTemp", "root", "Mind@123");

    JsonObject jsonObject = new JsonObject();

    @Override
    public void start(Promise<Void> startPromise) {

        LOG.debug("DiscoveryRepo deployed");

        EventBus eventBus = vertx.eventBus();

        eventBus.consumer("my.request.db", handler -> {

            jsonObject = (JsonObject) handler.body();

            System.out.println("json " + jsonObject);

            String host = jsonObject.getString("host");

            try {

                if (!checkIP(host)) {

                    create(jsonObject);

                    handler.reply("Inserted into Database");
                } else {

                    handler.reply("Duplicate IP address");

                }

            } catch (SQLException e) {

                throw new RuntimeException(e);

            }

        });

        startPromise.complete();
    }


    public DiscoveryRepo() throws SQLException, ClassNotFoundException {

    }

    public Boolean checkIP(String host) throws SQLException {

        Statement statement = con.createStatement();

        String checkIpvalue = "select host from DiscoveryTemp.Discovery where host='" + host+ "'";

        ResultSet resultSet = statement.executeQuery(checkIpvalue);

        return resultSet.next();

    }


    public void create(JsonObject jsonObject) throws SQLException {
        PreparedStatement discoveryStmt = null;
        String insertUserSql = "INSERT INTO DiscoveryTemp.Discovery(device,port,user,password,version,community,host)"
                + "VALUES(?,?,?,?,?,?,?)";
        discoveryStmt = con.prepareStatement(insertUserSql);

        discoveryStmt.setString(1, jsonObject.getString("device"));

        discoveryStmt.setString(2, jsonObject.getString("port"));

        discoveryStmt.setString(3, jsonObject.getString("user"));

        discoveryStmt.setString(4, jsonObject.getString("password"));

        discoveryStmt.setString(5, jsonObject.getString("version"));

        discoveryStmt.setString(6, jsonObject.getString("community"));

        discoveryStmt.setString(7, jsonObject.getString("host"));

        discoveryStmt.execute();
    }


    public void delete(String s) {

    }


}
