package com.mindarray;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DatabaseEngine extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseEngine.class);



    @Override
    public void start(Promise<Void> startPromise) {


        LOGGER.debug("DATABASE ENGINE DEPLOYED");

        EventBus eventBus = vertx.eventBus();

        eventBus.consumer(Constant.EVENTBUS_CHECKIP, checkip -> {

            JsonObject result = new JsonObject();

          JsonObject checkIPJson = new JsonObject(checkip.body().toString());


            //todo: try catch?

            vertx.executeBlocking(event -> {
                try {

                    if (!checkIP(checkIPJson.getString(Constant.IP_ADDRESS))) {

                        result.put(Constant.STATUS, Constant.SUCCESS);

                        event.complete(result);
                    } else {

                        result.put(Constant.STATUS, Constant.FAILED);

                        result.put(Constant.ERROR, "Check IP FAIlED");

                        event.complete(result);
                    }

                } catch (NullPointerException exception) {

                    LOGGER.debug("NULL POINTER EXCEPTION");

                } catch (SQLException exception) {

                  LOGGER.debug("SQL EXCEPTION");

                }

            }).onComplete(res -> checkip.reply(result));


        });

        eventBus.consumer(Constant.EVENTBUS_INSERTDB, handler -> {

            JsonObject jsonDbData = new JsonObject(handler.body().toString());

            vertx.executeBlocking(Blockinhandler -> {

                JsonObject result = new JsonObject();

                try {

                    if (!checkIP(jsonDbData.getString(Constant.IP_ADDRESS))) {

                        insertIntoDB(jsonDbData);

                        result.put(Constant.DB_STATUS, Constant.SUCCESS);

                    } else {

                        result.put(Constant.DB_STATUS, Constant.FAILED);

                        result.put(Constant.ERROR, "Duplicate IP address");

                    }

                } catch (SQLException e) {

                    result.put(Constant.DB_STATUS, Constant.FAILED);

                    result.put(Constant.ERROR, e.getMessage());
                }

                Blockinhandler.complete(result);

            }).onComplete(handler1 -> {
                handler.reply(handler1.result());
            });

        });

        startPromise.complete();
    }

    public Boolean checkIP(String host) throws SQLException {
        boolean result = false;
        try (Connection connection = getConnection()) {


            Statement statement = connection.createStatement();

            String checkIpvalue = "select ipAddress from DiscoveryTemp.Discovery where ipAddress='" + host + "'";

            ResultSet resultSet = statement.executeQuery(checkIpvalue);

            result = resultSet.next();

        } catch (SQLException exception) {

           LOGGER.error(exception.getMessage());

        }
        return result;
    }

    public void insertIntoDB(JsonObject jsonObject) throws SQLException {
        try(Connection connection = getConnection()) {

        PreparedStatement discoveryStmt;
        String insertUserSql = "INSERT INTO DiscoveryTemp.Discovery(metricType,port,user,password,version,community,ipAddress)"
                + "VALUES(?,?,?,?,?,?,?)";
        discoveryStmt = connection.prepareStatement(insertUserSql);

        discoveryStmt.setString(1, jsonObject.getString("metric.type"));

        discoveryStmt.setString(2, jsonObject.getString("port"));

        discoveryStmt.setString(3, jsonObject.getString("user"));

        discoveryStmt.setString(4, jsonObject.getString("password"));

        discoveryStmt.setString(5, jsonObject.getString("version"));

        discoveryStmt.setString(6, jsonObject.getString("community"));

        discoveryStmt.setString(7, jsonObject.getString("ip.address"));

        discoveryStmt.execute();
        } catch (SQLException exception) {
            LOGGER.error(exception.getMessage());
        }
    }
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/DiscoveryTemp", "root", "Mind@123");
    }
}
