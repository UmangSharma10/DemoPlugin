package com.mindarray;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

import static com.mindarray.Constant.DIS_ID;

public class DatabaseEngine extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseEngine.class);


    @Override
    public void start(Promise<Void> startPromise) {


        LOGGER.debug("DATABASE ENGINE DEPLOYED");

        EventBus eventBus = Bootstrap.vertx.eventBus();

        //subrouteCred
        eventBus.<JsonObject>consumer(Constant.EVENTBUS_CHECK_CREDNAME, apiCred -> {
            JsonObject result = new JsonObject();
            JsonObject userCredData = apiCred.body();
            Bootstrap.vertx.executeBlocking(event -> {
                try {
                    if (!checkCredName(userCredData.getString(Constant.CRED_NAME))) {
                        result.put(Constant.STATUS, Constant.SUCCESS);

                        event.complete(result);
                    } else {

                        result.put(Constant.STATUS, Constant.FAILED);

                        result.put(Constant.ERROR, "CRED.NAME NOT UNIQUE");

                        event.complete(result);
                    }

                } catch (Exception exception) {
                    LOGGER.error(exception.getMessage());

                }
            }).onComplete(res -> {
                apiCred.reply(result);
            });
        });

        eventBus.<JsonObject>consumer(Constant.EVENTBUS_INSERTCRED, handler -> {

            JsonObject jsonDbData = handler.body();

            vertx.executeBlocking(blockinhandler -> {

                JsonObject result = new JsonObject();

                try {

                    if (!checkCredName(jsonDbData.getString(Constant.CRED_NAME))) {

                        insertIntoCredDB(jsonDbData);

                        result.put(Constant.DB_STATUS_INSERTION, Constant.SUCCESS);

                        String credName = jsonDbData.getString(Constant.CRED_NAME);

                        long credID = getCredProfile(credName);

                        result.put(Constant.CRED_PROFILE, credID);

                    } else {

                        result.put(Constant.DB_STATUS_INSERTION, Constant.FAILED);

                        result.put(Constant.ERROR, "Duplicate cred.name");

                    }

                } catch (Exception exception) {

                    result.put(Constant.DB_STATUS_INSERTION, Constant.FAILED);

                    result.put(Constant.ERROR, exception.getMessage());
                }

                blockinhandler.complete(result);

            }).onComplete(handler1 -> handler.reply(handler1.result()));

        });

        eventBus.<String>consumer(Constant.EVENTBUS_DELETEDIS, handler -> {

            var id = handler.body();

            long longid = Long.parseLong(id);

            JsonObject jsonDbData = new JsonObject().put(Constant.CRED_ID, longid);

            vertx.executeBlocking(blockinhandler -> {

                JsonObject result = new JsonObject();

                try {

                    if (checkDisId(jsonDbData.getLong(Constant.CRED_ID))) {

                        boolean value = deleteCredDb(jsonDbData.getLong(Constant.CRED_ID));

                        if (value) {
                            result.put(Constant.DB_STATUS_DELETION, Constant.SUCCESS);
                        }
                        else {
                            result.put(Constant.DB_STATUS_DELETION, Constant.FAILED);
                        }

                    } else {

                        result.put(Constant.DB_STATUS_DELETION, Constant.FAILED);

                        result.put(Constant.ERROR, "Wrong ID");

                    }

                } catch (Exception exception) {

                    result.put(Constant.DB_STATUS_DELETION, Constant.FAILED);

                    result.put(Constant.ERROR, exception.getMessage());
                }

                blockinhandler.complete(result);

            }).onComplete(handler1 -> handler.reply(handler1.result()));

        });

        eventBus.<JsonObject>consumer(Constant.EVENTBUS_CHECKID_JSON, apiCredId -> {
            JsonObject result = new JsonObject();

            JsonObject userCredData = apiCredId.body();

            Bootstrap.vertx.executeBlocking(event -> {

                try {

                    if (checkDisId(userCredData.getLong(Constant.CRED_ID))) {

                        result.put(Constant.STATUS, Constant.SUCCESS);

                        event.complete(result);

                    }

                    else

                    {
                        result.put(Constant.STATUS, Constant.FAILED);

                        result.put(Constant.ERROR, "WRONG ID");

                        event.complete(result);
                    }

                } catch (Exception exception) {
                    LOGGER.error(exception.getMessage());

                }
            }).onComplete(res -> {
                if (res.succeeded()) {
                    apiCredId.reply(result);
                }
            });
        });

        eventBus.<String>consumer(Constant.EVENTBUS_CHECKID_CRED, apiCredId -> {
            JsonObject result = new JsonObject();

            var id = apiCredId.body();

            long longid = Long.parseLong(id);

            JsonObject userCredData = new JsonObject().put(Constant.CRED_ID,longid);

            Bootstrap.vertx.executeBlocking(event -> {

                try {

                    if (checkDisId(userCredData.getLong(Constant.CRED_ID))) {

                        result.put(Constant.STATUS, Constant.SUCCESS);

                        event.complete(result);

                    }

                    else

                    {
                        result.put(Constant.STATUS, Constant.FAILED);

                        result.put(Constant.ERROR, "WRONG ID");

                        event.complete(result);
                    }

                } catch (Exception exception) {
                    LOGGER.error(exception.getMessage());

                }
            }).onComplete(res -> {
                if (res.succeeded()) {
                    apiCredId.reply(result);
                }
            });
        });




        //subroutediscovery
        eventBus.<JsonObject>consumer(Constant.EVENTBUS_CHECKID_JSON, apiDisId -> {
            JsonObject result = new JsonObject();

            JsonObject userCredData = apiDisId.body();

            Bootstrap.vertx.executeBlocking(event -> {

                try {
                    System.out.println("data");

                    if (checkDisId(userCredData.getLong(DIS_ID))) {

                        result.put(Constant.STATUS, Constant.SUCCESS);

                        event.complete(result);

                    }

                    else

                    {
                        result.put(Constant.STATUS, Constant.FAILED);

                        result.put(Constant.ERROR, "WRONG ID");

                        event.complete(result);
                    }

                } catch (Exception exception) {
                    LOGGER.error(exception.getMessage());

                }
            }).onComplete(res -> {
                if (res.succeeded()) {
                    apiDisId.reply(result);
                }
            });
        });

        eventBus.<String>consumer(Constant.EVENTBUS_CHECKID_DISCOVERY, apiDisId -> {
            JsonObject result = new JsonObject();

            var id = apiDisId.body();

            long longid = Long.parseLong(id);

            JsonObject userCredData = new JsonObject().put(DIS_ID,longid);

            Bootstrap.vertx.executeBlocking(event -> {

                try {

                    if (checkDisId(userCredData.getLong(DIS_ID))) {

                        result.put(Constant.STATUS, Constant.SUCCESS);

                        event.complete(result);

                    }

                    else

                    {
                        result.put(Constant.STATUS, Constant.FAILED);

                        result.put(Constant.ERROR, "WRONG ID");

                        event.complete(result);
                    }

                } catch (Exception exception) {
                    LOGGER.error(exception.getMessage());

                }
            }).onComplete(res -> {
                if (res.succeeded()) {
                    apiDisId.reply(result);
                }
            });
        });

        eventBus.<JsonObject>consumer(Constant.EVENTBUS_CHECK_DISNAME, apiDis -> {
            JsonObject result = new JsonObject();
            JsonObject userCredData = apiDis.body();
            Bootstrap.vertx.executeBlocking(event -> {
                try {
                    if (!checkDisName(userCredData.getString(Constant.DIS_NAME))) {
                        result.put(Constant.STATUS, Constant.SUCCESS);

                        event.complete(result);
                    } else {

                        result.put(Constant.STATUS, Constant.FAILED);

                        result.put(Constant.ERROR, "DIS.NAME NOT UNIQUE");

                        event.complete(result);
                    }

                } catch (Exception exception) {
                    LOGGER.error(exception.getMessage());

                }
            }).onComplete(res -> {
                apiDis.reply(result);
            });
        });

        eventBus.<JsonObject>consumer(Constant.EVENTBUS_INSERTDISCOVERY, handler -> {

            JsonObject jsonDbData = handler.body();

            vertx.executeBlocking(blockinhandler -> {

                JsonObject result = new JsonObject();

                try {

                    if (!checkDisName(jsonDbData.getString(Constant.DIS_NAME))) {

                        insertIntoDisDB(jsonDbData);

                        result.put(Constant.DB_STATUS_INSERTION, Constant.SUCCESS);

                        String disName = jsonDbData.getString(Constant.DIS_NAME);

                        long disID = getDisProfile(disName);

                        result.put(DIS_ID, disID);

                    } else {

                        result.put(Constant.DB_STATUS_INSERTION, Constant.FAILED);

                        result.put(Constant.ERROR, "Duplicate dis.name");

                    }

                } catch (Exception exception) {

                    result.put(Constant.DB_STATUS_INSERTION, Constant.FAILED);

                    result.put(Constant.ERROR, exception.getMessage());
                }

                blockinhandler.complete(result);

            }).onComplete(handler1 -> handler.reply(handler1.result()));

        });

        eventBus.<JsonObject>consumer(Constant.EVENTBUS_UPDATE, handler -> {

            JsonObject jsonDbData = handler.body();

            vertx.executeBlocking(blockinhandler -> {

                JsonObject result = new JsonObject();

                try {

                    if (checkCredId(jsonDbData.getLong(Constant.CRED_PROFILE))) {

                        updateIntoDisDB(jsonDbData);

                        result.put(Constant.DB_STATUS_UPDATE, Constant.SUCCESS);

                    } else {

                        result.put(Constant.DB_STATUS_UPDATE, Constant.FAILED);

                        result.put(Constant.ERROR, "CRED PROFILE DOESNT EXIST IN CRED DB");

                    }

                } catch (Exception exception) {

                    result.put(Constant.DB_STATUS_UPDATE, Constant.FAILED);

                    result.put(Constant.ERROR, exception.getMessage());
                }

                blockinhandler.complete(result);

            }).onComplete(handler1 -> handler.reply(handler1.result()));

        });

        eventBus.<String>consumer(Constant.EVENTBUS_DELETECRED, handler -> {

            var id = handler.body();

            long longid = Long.parseLong(id);

            JsonObject jsonDbData = new JsonObject().put(DIS_ID, longid);

            vertx.executeBlocking(blockinhandler -> {

                JsonObject result = new JsonObject();

                try {

                    if (checkDisId(jsonDbData.getLong(DIS_ID))) {

                        boolean value = deleteDisDb(jsonDbData.getLong(DIS_ID));

                        if (value) {
                            result.put(Constant.DB_STATUS_DELETION, Constant.SUCCESS);
                        }
                        else {
                            result.put(Constant.DB_STATUS_DELETION, Constant.FAILED);
                        }

                    } else {

                        result.put(Constant.DB_STATUS_DELETION, Constant.FAILED);

                        result.put(Constant.ERROR, "Wrong ID");

                    }

                } catch (Exception exception) {

                    result.put(Constant.DB_STATUS_DELETION, Constant.FAILED);

                    result.put(Constant.ERROR, exception.getMessage());
                }

                blockinhandler.complete(result);

            }).onComplete(handler1 -> handler.reply(handler1.result()));

        });


        eventBus.<String>consumer(Constant.EVENTBUS_GETDISCOVERY, getIdhandler ->{
            String id = getIdhandler.body();
            long longid = Long.parseLong(id);
            JsonObject getJsonById = new JsonObject().put(DIS_ID, longid);
            vertx.executeBlocking(blockinhandler -> {

                JsonObject result = new JsonObject();

                try {

                    if (checkDisId(getJsonById.getLong(DIS_ID))) {

                        JsonObject value = getByID(getJsonById.getLong(DIS_ID));

                        result.put(Constant.STATUS, Constant.SUCCESS);

                        result.put("Result", value);

                    } else {

                        result.put(Constant.STATUS, Constant.FAILED);

                        result.put(Constant.ERROR, "Wrong Discovery ID");

                    }

                } catch (Exception exception) {

                    result.put(Constant.STATUS, Constant.FAILED);

                    result.put(Constant.ERROR, exception.getMessage());
                }

                blockinhandler.complete(result);

            }).onComplete(handler1 -> getIdhandler.reply(handler1.result()));



        });

        eventBus.<String>consumer(Constant.EVENTBUS_GETALL, getIdhandler ->{
            vertx.executeBlocking(blockinhandler -> {

                JsonObject result = new JsonObject();

                try {

                        JsonArray value = getAll();

                        result.put(Constant.STATUS, Constant.SUCCESS);

                        result.put("Result", value);


                } catch (Exception exception) {

                    result.put(Constant.STATUS, Constant.FAILED);

                    result.put(Constant.ERROR, exception.getMessage());
                }

                blockinhandler.complete(result);

            }).onComplete(handler1 -> getIdhandler.reply(handler1.result()));



        });

        //
        eventBus.<JsonObject>consumer(Constant.EVENTBUS_CHECKIP, checkip -> {

            JsonObject result = new JsonObject();

            JsonObject checkIPJson = checkip.body();

            vertx.executeBlocking(event -> {
                try {

                    if (!checkIP(checkIPJson.getString(Constant.IP_ADDRESS))) {

                        result.put(Constant.STATUS, Constant.SUCCESS);

                        event.complete(result);
                    } else {

                        result.put(Constant.STATUS, Constant.FAILED);

                        result.put(Constant.ERROR, "IP ALREADY DISCOVERED");

                        event.complete(result);
                    }

                } catch (NullPointerException exception) {

                    LOGGER.debug("NULL POINTER EXCEPTION");

                } catch (SQLException exception) {

                    LOGGER.debug("SQL EXCEPTION");

                }

            }).onComplete(res -> checkip.reply(result));


        });

        eventBus.<JsonObject>consumer(Constant.EVENTBUS_INSERTDB, handler -> {

            JsonObject jsonDbData = handler.body();

            vertx.executeBlocking(blockinhandler -> {

                JsonObject result = new JsonObject();

                try {

                    if (!checkIP(jsonDbData.getString(Constant.IP_ADDRESS))) {

                        insertIntoDB(jsonDbData);

                        result.put(Constant.DB_STATUS_INSERTION, Constant.SUCCESS);

                    } else {

                        result.put(Constant.DB_STATUS_INSERTION, Constant.FAILED);

                        result.put(Constant.ERROR, "Duplicate IP address");

                    }

                } catch (SQLException e) {

                    result.put(Constant.DB_STATUS_INSERTION, Constant.FAILED);

                    result.put(Constant.ERROR, e.getMessage());
                }

                blockinhandler.complete(result);

            }).onComplete(handler1 -> handler.reply(handler1.result()));

        });

        startPromise.complete();
    }


    public JsonArray getAll(){
        JsonArray arrayResult = new JsonArray();

        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            String getById = "select * from DiscoveryTemp.discoveryTable";
            ResultSet resultSet = statement.executeQuery(getById);
            while (resultSet.next()) {
                JsonObject result = new JsonObject();
                long disId = resultSet.getLong("id");
                String ip = resultSet.getString("ipAddress");
                String type = resultSet.getString("metricType");
                Long credProfile = resultSet.getLong("credProfile");
                int port = resultSet.getInt("port");
                String disName = resultSet.getString("disName");

                result.put(DIS_ID, disId);
                result.put(Constant.IP_ADDRESS , ip);
                result.put(Constant.METRIC_TYPE , type);
                result.put("cred.profile", credProfile);
                result.put(Constant.PORT, port);
                result.put(Constant.DIS_NAME, disName);

                arrayResult.add(result);
            }


        }catch (Exception exception){
            LOGGER.error(exception.getMessage());
        }
        return arrayResult;
    }

    public void updateIntoDisDB(JsonObject updateDb){

        try (Connection connection = getConnection()){
            PreparedStatement discoveryStmt;
            String updateUserSql = "UPDATE DiscoveryTemp.discoveryTable SET ipAddress = ?,credProfile = ? , disName = ? WHERE ID = ?";
            discoveryStmt = connection.prepareStatement(updateUserSql);
            discoveryStmt.setString(1, updateDb.getString("ip.address"));

            discoveryStmt.setString(2, updateDb.getString("cred.profile"));

            discoveryStmt.setString(3, updateDb.getString("dis.name"));

            discoveryStmt.setLong(4 , updateDb.getLong(DIS_ID));


            discoveryStmt.executeUpdate();

        } catch (Exception exception) {
            LOGGER.error("Error");
        }

    }
    public JsonObject getByID(long id){
        JsonObject result = new JsonObject();
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            String getById = "select * from DiscoveryTemp.discoveryTable where id='" + id + "'";
            ResultSet resultSet = statement.executeQuery(getById);
            if(resultSet.next()) {
                long disId = resultSet.getLong("id");
                String ip = resultSet.getString("ipAddress");
                String type = resultSet.getString("metricType");
                Long credProfile = resultSet.getLong("credProfile");
                int port = resultSet.getInt("port");
                String disName = resultSet.getString("disName");

                result.put(DIS_ID, disId);
                result.put(Constant.IP_ADDRESS , ip);
                result.put(Constant.METRIC_TYPE , type);
                result.put("cred.profile", credProfile);
                result.put(Constant.PORT, port);
                result.put(Constant.DIS_NAME, disName);
            }


        }catch (Exception exception){
            LOGGER.error(exception.getMessage());
        }
        return result;
    }

    public Long getCredProfile(String name) {
        long result = 0;
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            String getID = "select id from DiscoveryTemp.credentialsTable where credName='" + name + "'";
            ResultSet resultSet = statement.executeQuery(getID);
            if (resultSet.next()) {
                result = Long.parseLong(resultSet.getString(1));
            }
        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
        }
        return result;
    }

    public Long getDisProfile(String name) {
        long result = 0;
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            String getID = "select id from DiscoveryTemp.discoveryTable where disName='" + name + "'";
            ResultSet resultSet = statement.executeQuery(getID);
            if (resultSet.next()) {
                result = Long.parseLong(resultSet.getString(1));
            }
        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
        }
        return result;
    }

    public Boolean checkCredId(long id) {
        boolean result = false;
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();

            String checkIpvalue = "select id from DiscoveryTemp.credentialsTable where id='" + id + "'";

            ResultSet resultSet = statement.executeQuery(checkIpvalue);

            result = resultSet.next();

        } catch (Exception exception) {

            LOGGER.error(exception.getMessage());

        }

        return result;
    }
    public Boolean checkDisId(long id) {
        boolean result = false;
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();

            String checkIpvalue = "select id from DiscoveryTemp.discoveryTable where id='" + id + "'";

            ResultSet resultSet = statement.executeQuery(checkIpvalue);

            result = resultSet.next();

        } catch (Exception exception) {

            LOGGER.error(exception.getMessage());

        }

        return result;
    }

    public Boolean checkDisName(String name) {
        boolean result = false;
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();

            String checkIpvalue = "select disName from DiscoveryTemp.discoveryTable where disName='" + name + "'";

            ResultSet resultSet = statement.executeQuery(checkIpvalue);

            result = resultSet.next();

        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
        }

        return result;
    }

    public Boolean checkCredName(String name) {
        boolean result = false;
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();

            String checkIpvalue = "select credName from DiscoveryTemp.credentialsTable where credName='" + name + "'";

            ResultSet resultSet = statement.executeQuery(checkIpvalue);

            result = resultSet.next();

        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
        }

        return result;
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

    public void insertIntoCredDB(JsonObject jsonObject) {
        try (Connection connection = getConnection()) {

            PreparedStatement discoveryStmt;
            String insertUserSql = "INSERT INTO DiscoveryTemp.credentialsTable(protocol,username,password,community,version,credName)"
                    + "VALUES(?,?,?,?,?,?)";
            discoveryStmt = connection.prepareStatement(insertUserSql);

            // discoveryStmt.setString(1, jsonObject.getString("id"));

            discoveryStmt.setString(1, jsonObject.getString("protocol"));

            discoveryStmt.setString(2, jsonObject.getString("user"));

            discoveryStmt.setString(3, jsonObject.getString("password"));

            discoveryStmt.setString(4, jsonObject.getString("community"));

            discoveryStmt.setString(5, jsonObject.getString("version"));

            discoveryStmt.setString(6, jsonObject.getString("cred.name"));

            discoveryStmt.execute();
        } catch (SQLException exception) {
            LOGGER.error(exception.getMessage());
        }
    }

    public void insertIntoDisDB(JsonObject jsonObject) {
        try (Connection connection = getConnection()) {

            PreparedStatement discoveryStmt;
            String insertUserSql = "INSERT INTO DiscoveryTemp.discoveryTable(ipAddress,metricType,credProfile,port,disName)"
                    + "VALUES(?,?,?,?,?)";
            discoveryStmt = connection.prepareStatement(insertUserSql);


            discoveryStmt.setString(1, jsonObject.getString("ip.address"));

            discoveryStmt.setString(2, jsonObject.getString("metric.type"));

            discoveryStmt.setString(3, jsonObject.getString("cred.profile"));

            discoveryStmt.setInt(4, jsonObject.getInteger("port"));

            discoveryStmt.setString(5, jsonObject.getString("dis.name"));


            discoveryStmt.execute();
        } catch (SQLException exception) {
            LOGGER.error(exception.getMessage());
        }
    }

    public boolean deleteDisDb(long id) {
        boolean result = false;

        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();

            String value = "delete from DiscoveryTemp.discoveryTable where id ='" + id + "'";

            int resultSet = statement.executeUpdate(value);

            result = resultSet > 0;



        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
        }

        return result;
    }

    public boolean deleteCredDb(long id) {
        boolean result = false;

        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();

            String value = "delete from DiscoveryTemp.credentialsTable where id ='" + id + "'";

            int resultSet = statement.executeUpdate(value);

            result = resultSet > 0;



        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
        }

        return result;
    }

    public void insertIntoDB(JsonObject jsonObject) throws SQLException {
        try (Connection connection = getConnection()) {

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
