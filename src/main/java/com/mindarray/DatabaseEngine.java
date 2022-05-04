package com.mindarray;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DatabaseEngine extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseEngine.class);

    JsonObject jsonDbdata = new JsonObject();
    JsonObject checkIPJson = new JsonObject();

    @Override
    public void start(Promise<Void> startPromise) {
        LOG.debug("DATABASE ENGINE DEPLOYED");

        EventBus eventBus = vertx.eventBus();

        eventBus.consumer(NmsConstant.DATABASECHECKIP, checkip -> {

            JsonObject result = new JsonObject();

            checkIPJson = (JsonObject) checkip.body();

            System.out.println(checkIPJson);

            String ip = checkIPJson.getString("host").trim();

            vertx.executeBlocking(event -> {
                try {

                    if (!checkIP(ip)) {

                        result.put("status", "Success");

                        event.complete(result);
                    } else {

                        result.put("status", "Failed");

                        result.put("Error", "Check IP FAIlED");

                        event.complete(result);
                    }

                } catch (NullPointerException exception) {

                    LOG.debug("NULL POINTER EXCEPTION");

                } catch (SQLException e) {

                    throw new RuntimeException(e);

                }

            }).onComplete(res -> checkip.reply(result));


        });

        eventBus.consumer("my.request.db", handler -> {

            JsonObject result = new JsonObject();
            jsonDbdata = (JsonObject) handler.body();

            String host = jsonDbdata.getString("host");

            vertx.executeBlocking(Blockinhandler -> {
                try {

                    if (!checkIP(host)) {

                        create(jsonDbdata);

                        result.put("Insertion", "Successful");
                    } else {

                        result.put("Insertion", "Unsuccessful");
                        result.put("Error", "Duplicate IP address");

                    }

                } catch (SQLException e) {
                    result.put("Insertion", "Unsuccessful");
                    result.put("Error", e.getMessage());
                }
                Blockinhandler.complete();
            }).onComplete(handler1 -> handler.reply(result));
        });
        startPromise.complete();
    }

    public Boolean checkIP(String host) throws SQLException {
        Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/DiscoveryTemp", "root", "Mind@123");
        Statement statement = con.createStatement();

        String checkIpvalue = "select host from DiscoveryTemp.Discovery where host='" + host + "'";

        ResultSet resultSet = statement.executeQuery(checkIpvalue);

        return resultSet.next();

    }

    public void create(JsonObject jsonObject) throws SQLException {
        Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/DiscoveryTemp", "root", "Mind@123");
        PreparedStatement discoveryStmt;
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
}
