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

                if (res.succeeded()) {

                    apiCred.reply(result);

                } else {
                    apiCred.fail(-1, res.cause().getMessage());
                }

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

                        blockinhandler.complete(result);

                    } else {

                        result.put(Constant.DB_STATUS_INSERTION, Constant.FAILED);

                        result.put(Constant.ERROR, "Duplicate cred.name");

                        blockinhandler.fail(result.encode());

                    }

                } catch (Exception exception) {

                    result.put(Constant.DB_STATUS_INSERTION, Constant.FAILED);

                    result.put(Constant.ERROR, exception.getMessage());

                    blockinhandler.fail(result.encode());
                }


            }).onComplete(handler1 -> {
                if (handler1.succeeded()) {

                    handler.reply(handler1.result());

                } else {

                    handler.fail(-1, handler1.cause().getMessage());

                }


            });

        });

        eventBus.<String>consumer(Constant.EVENTBUS_DELETECRED, handler -> {

            var id = handler.body();

            long longid = Long.parseLong(id);

            JsonObject jsonDbData = new JsonObject().put(Constant.CRED_ID, longid);

            vertx.executeBlocking(blockinhandler -> {

                JsonObject result = new JsonObject();

                try {

                    if (checkCredId(jsonDbData.getLong(Constant.CRED_ID))) {

                        if (checkCredProfile(jsonDbData.getLong(Constant.CRED_ID))) {
                            boolean value = deleteCredDb(jsonDbData.getLong(Constant.CRED_ID));

                            if (value) {

                                result.put(Constant.DB_STATUS_DELETION, Constant.SUCCESS);

                                blockinhandler.complete(result);
                            } else {

                                result.put(Constant.DB_STATUS_DELETION, Constant.FAILED);

                                blockinhandler.fail(result.encode());
                            }
                        } else {
                            result.put(Constant.DB_STATUS_DELETION, Constant.FAILED);

                            result.put(Constant.ERROR, "Already Used in Discovery");

                            blockinhandler.fail(result.encode());

                        }


                    } else {

                        result.put(Constant.DB_STATUS_DELETION, Constant.FAILED);

                        result.put(Constant.ERROR, "Wrong ID");

                        blockinhandler.fail(result.encode());

                    }

                } catch (Exception exception) {

                    result.put(Constant.DB_STATUS_DELETION, Constant.FAILED);

                    result.put(Constant.ERROR, exception.getMessage());

                    blockinhandler.fail(result.encode());
                }


            }).onComplete(handler1 -> {

                if (handler1.succeeded()) {
                    handler.reply(handler1.result());
                } else {
                    handler.fail(-1, handler1.cause().getMessage());
                }

            });

        });

        eventBus.<JsonObject>consumer(Constant.EVENTBUS_CHECKID_JSON, apiCredId -> {
            JsonObject result = new JsonObject();

            JsonObject userCredData = apiCredId.body();

            Bootstrap.vertx.executeBlocking(event -> {

                try {

                    if (checkCredId(userCredData.getLong(Constant.CRED_ID))) {

                        result.put(Constant.STATUS, Constant.SUCCESS);

                        event.complete(result);

                    } else {
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
                } else {
                    apiCredId.fail(-1, res.cause().getMessage());
                }
            });
        });

        eventBus.<String>consumer(Constant.EVENTBUS_CHECKID_CRED, apiCredId -> {
            JsonObject result = new JsonObject();

            var id = apiCredId.body();

            long longid = Long.parseLong(id);

            JsonObject userCredData = new JsonObject().put(Constant.CRED_ID, longid);

            Bootstrap.vertx.executeBlocking(event -> {

                try {

                    if (checkCredId(userCredData.getLong(Constant.CRED_ID))) {

                        result.put(Constant.STATUS, Constant.SUCCESS);

                        event.complete(result);

                    } else {
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
                } else {
                    apiCredId.fail(-1, res.cause().getMessage());
                }
            });
        });

        eventBus.<JsonObject>consumer(Constant.EVENTBUS_UPDATE_CRED, handler -> {

            JsonObject jsonDbData = handler.body();

            vertx.executeBlocking(blockinhandler -> {

                JsonObject result = new JsonObject();

                try {

                    if (checkCredId(jsonDbData.getLong(Constant.CRED_ID))) {

                        updateIntoCredDB(jsonDbData);

                        result.put(Constant.DB_STATUS_UPDATE, Constant.SUCCESS);

                    } else {

                        result.put(Constant.DB_STATUS_UPDATE, Constant.FAILED);

                        result.put(Constant.ERROR, "CRED ID DOESNT EXIST IN CRED DB");

                    }

                } catch (Exception exception) {

                    result.put(Constant.DB_STATUS_UPDATE, Constant.FAILED);

                    result.put(Constant.ERROR, exception.getMessage());
                }

                blockinhandler.complete(result);

            }).onComplete(handler1 -> {
                if (handler1.succeeded()) {
                    handler.reply(handler1.result());
                } else {
                    handler.fail(-1, handler1.cause().getMessage());
                }
            });

        });

        eventBus.<String>consumer(Constant.EVENTBUS_GETCREDBYID, getIdhandler -> {
            String id = getIdhandler.body();
            long longid = Long.parseLong(id);
            JsonObject getJsonById = new JsonObject().put(Constant.CRED_ID, longid);
            vertx.executeBlocking(blockinhandler -> {

                JsonObject result = new JsonObject();

                try {

                    if (checkCredId(getJsonById.getLong(Constant.CRED_ID))) {

                        JsonObject value = getByCredID(getJsonById.getLong(Constant.CRED_ID));

                        result.put(Constant.STATUS, Constant.SUCCESS);

                        result.put("Result", value);

                        blockinhandler.complete(result);

                    } else {

                        result.put(Constant.STATUS, Constant.FAILED);

                        result.put(Constant.ERROR, "Wrong Credential ID");

                        blockinhandler.fail(result.encode());

                    }

                } catch (Exception exception) {

                    result.put(Constant.STATUS, Constant.FAILED);

                    result.put(Constant.ERROR, exception.getMessage());

                    blockinhandler.fail(result.encode());
                }

            }).onComplete(handler1 -> {
                if (handler1.succeeded()) {
                    getIdhandler.reply(handler1.result());
                } else {
                    getIdhandler.fail(-1, handler1.cause().getMessage());
                }
            });


        });

        eventBus.<String>consumer(Constant.EVENTBUS_GETALLCRED, getIdhandler -> vertx.executeBlocking(blockinhandler -> {

            JsonObject result = new JsonObject();

            try {

                JsonArray value = getAllCred();

                result.put(Constant.STATUS, Constant.SUCCESS);

                result.put("Result", value);

                blockinhandler.complete(result);


            } catch (Exception exception) {

                result.put(Constant.STATUS, Constant.FAILED);

                result.put(Constant.ERROR, exception.getMessage());

                blockinhandler.fail(result.encode());
            }

            blockinhandler.complete(result);

        }).onComplete(handler1 -> {
            if (handler1.succeeded()) {
                getIdhandler.reply(handler1.result());
            } else {
                getIdhandler.fail(-1, handler1.cause().getMessage());
            }
        }));


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

                    } else {
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
                } else {
                    apiDisId.fail(-1, res.cause().getMessage());
                }
            });
        });

        eventBus.<String>consumer(Constant.EVENTBUS_CHECKID_DISCOVERY, apiDisId -> {
            JsonObject result = new JsonObject();

            var id = apiDisId.body();

            long longid = Long.parseLong(id);

            JsonObject userCredData = new JsonObject().put(DIS_ID, longid);

            Bootstrap.vertx.executeBlocking(event -> {

                try {

                    if (checkDisId(userCredData.getLong(DIS_ID))) {

                        result.put(Constant.STATUS, Constant.SUCCESS);

                        event.complete(result);

                    } else {
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
                } else {
                    apiDisId.fail(-1, res.cause().getMessage());
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
                if (res.succeeded()) {
                    apiDis.reply(result);
                } else {
                    apiDis.fail(-1, res.cause().getMessage());
                }

            });
        });

        eventBus.<JsonObject>consumer(Constant.EVENTBUS_INSERTDISCOVERY, handler -> {

            JsonObject jsonDbData = handler.body();

            vertx.executeBlocking(blockinhandler -> {

                JsonObject result = new JsonObject();

                try {

                    if (Boolean.FALSE.equals(checkDisName(jsonDbData.getString(Constant.DIS_NAME)))) {

                        insertIntoDisDB(jsonDbData);

                        result.put(Constant.DB_STATUS_INSERTION, Constant.SUCCESS);

                        String disName = jsonDbData.getString(Constant.DIS_NAME);

                        long disID = getDisProfile(disName);

                        result.put(DIS_ID, disID);

                        blockinhandler.complete(result);

                    } else {

                        result.put(Constant.DB_STATUS_INSERTION, Constant.FAILED);

                        result.put(Constant.ERROR, "Duplicate dis.name");

                        blockinhandler.fail(result.encode());
                    }

                } catch (Exception exception) {

                    result.put(Constant.DB_STATUS_INSERTION, Constant.FAILED);

                    result.put(Constant.ERROR, exception.getMessage());

                    blockinhandler.fail(result.encode());
                }

            }).onComplete(handler1 -> {
                if (handler1.succeeded()) {

                    handler.reply(handler1.result());

                } else {

                    handler.fail(-1, handler1.cause().getMessage());

                }
            });
        });

        eventBus.<JsonObject>consumer(Constant.EVENTBUS_UPDATE_DIS, handler -> {

            JsonObject jsonDbData = handler.body();

            vertx.executeBlocking(blockinhandler -> {

                JsonObject result = new JsonObject();

                try {

                    if (Boolean.TRUE.equals(checkCredId(jsonDbData.getLong(Constant.CRED_PROFILE)))) {

                        updateIntoDisDB(jsonDbData);

                        result.put(Constant.DB_STATUS_UPDATE, Constant.SUCCESS);

                        blockinhandler.complete(result);

                    } else {

                        result.put(Constant.DB_STATUS_UPDATE, Constant.FAILED);

                        result.put(Constant.ERROR, "CRED PROFILE DOESNT EXIST IN CRED DB");

                        blockinhandler.fail(result.encode());

                    }

                } catch (Exception exception) {

                    result.put(Constant.DB_STATUS_UPDATE, Constant.FAILED);

                    result.put(Constant.ERROR, exception.getMessage());

                    blockinhandler.fail(result.encode());
                }


            }).onComplete(handler1 -> {
                if (handler1.succeeded()) {

                    handler.reply(handler1.result());

                } else {
                    handler.fail(-1, handler1.cause().getMessage());
                }
            });

        });

        eventBus.<String>consumer(Constant.EVENTBUS_DELETEDIS, handler -> {

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
                            blockinhandler.complete(result);
                        } else {
                            result.put(Constant.DB_STATUS_DELETION, Constant.FAILED);
                            blockinhandler.fail(result.encode());
                        }


                    } else {

                        result.put(Constant.DB_STATUS_DELETION, Constant.FAILED);

                        result.put(Constant.ERROR, "Wrong ID");

                        blockinhandler.fail(result.encode());

                    }

                } catch (Exception exception) {

                    result.put(Constant.DB_STATUS_DELETION, Constant.FAILED);

                    result.put(Constant.ERROR, exception.getMessage());

                    blockinhandler.fail(result.encode());
                }


            }).onComplete(handler1 -> {
                if (handler1.succeeded()) {
                    handler.reply(handler1.result());

                } else {
                    handler.fail(-1, handler1.cause().getMessage());
                }
            });

        });

        eventBus.<String>consumer(Constant.EVENTBUS_GETDISCOVERY, getIdhandler -> {

            String id = getIdhandler.body();

            long longid = Long.parseLong(id);

            JsonObject getJsonById = new JsonObject().put(DIS_ID, longid);

            vertx.executeBlocking(blockinhandler -> {

                JsonObject result = new JsonObject();

                try {

                    if (checkDisId(getJsonById.getLong(DIS_ID))) {

                        JsonObject value = getByDisID(getJsonById.getLong(DIS_ID));

                        result.put(Constant.STATUS, Constant.SUCCESS);

                        result.put("Result", value);

                        blockinhandler.complete(result);

                    } else {

                        result.put(Constant.STATUS, Constant.FAILED);

                        result.put(Constant.ERROR, "Wrong Discovery ID");

                        blockinhandler.fail(result.encode());

                    }

                } catch (Exception exception) {

                    result.put(Constant.STATUS, Constant.FAILED);

                    result.put(Constant.ERROR, exception.getMessage());

                    blockinhandler.fail(result.encode());
                }


            }).onComplete(handler1 -> {
                if (handler1.succeeded()) {
                    getIdhandler.reply(handler1.result());
                } else {
                    getIdhandler.fail(-1, handler1.cause().getMessage());
                }
            });


        });

        eventBus.<String>consumer(Constant.EVENTBUS_GETALLDIS, getIdhandler -> Bootstrap.vertx.executeBlocking(blockinhandler -> {

            JsonObject result = new JsonObject();

            try {

                JsonArray value = getAllDis();

                result.put(Constant.STATUS, Constant.SUCCESS);

                result.put("Result", value);

                blockinhandler.complete(result);


            } catch (Exception exception) {

                result.put(Constant.STATUS, Constant.FAILED);

                result.put(Constant.ERROR, exception.getMessage());

                blockinhandler.fail(result.encode());
            }


        }).onComplete(handler1 -> {
            if (handler1.succeeded()) {
                getIdhandler.reply(handler1.result());
            } else {
                getIdhandler.fail(-1, handler1.cause().getMessage());
            }
        }));

        eventBus.<String>consumer(Constant.EVENTBUS_RUN_DISCOVERY, runhandler -> {

            String id = runhandler.body();

            long longid = Long.parseLong(id);

            JsonObject getJsonById = new JsonObject().put(DIS_ID, longid);

            Bootstrap.vertx.<JsonObject>executeBlocking(blockinghandler -> {

                JsonObject result = new JsonObject();

                try {

                    if (checkDisId(getJsonById.getLong(DIS_ID))) {

                        JsonObject discoveryStatus = checkDiscoveryStatus(getJsonById.getLong(DIS_ID));

                        if (discoveryStatus.getString("discovery").equals("false")) {

                            JsonObject value = getRundiscoveryQuery(getJsonById.getLong(DIS_ID));

                            value.put("category", "discovery");
                            LOGGER.debug(value.encode());

                            result.put(Constant.STATUS, Constant.SUCCESS);

                            blockinghandler.complete(value);

                        } else {
                            result.put(Constant.STATUS, Constant.FAILED);

                            result.put(Constant.ERROR, "Already Discovered");

                            blockinghandler.fail(result.encode());


                        }


                    } else {

                        result.put(Constant.STATUS, Constant.FAILED);

                        result.put(Constant.ERROR, "Wrong Discovery ID");

                        blockinghandler.fail(result.encode());


                    }

                } catch (Exception exception) {

                    result.put(Constant.STATUS, Constant.FAILED);

                    result.put(Constant.ERROR, exception.getMessage());

                    blockinghandler.fail(result.encode());
                }


            }).onComplete(asyncResult -> {
                if (asyncResult.succeeded()) {
                    JsonObject value = asyncResult.result();
                    eventBus.<JsonObject>request(Constant.EVENTBUS_DISCOVERY, value, discovery -> {
                        LOGGER.debug("Response {} ", discovery.result().body());
                        //data return in json
                        JsonObject result = discovery.result().body();

                        if (!result.containsKey(Constant.ERROR)) {
                            runhandler.reply(result);
                        } else {
                            runhandler.reply(result);
                        }

                    });
                } else {
                    runhandler.fail(-1, asyncResult.cause().getMessage());

                }
            });


        });

        eventBus.<String>consumer(Constant.EVENTBUS_PROVISION, provisionHandler -> {
            String id = provisionHandler.body();

            long longid = Long.parseLong(id);

            JsonObject getJsonById = new JsonObject().put(DIS_ID, longid);

            Bootstrap.vertx.<JsonObject>executeBlocking(provisionBlocking -> {

                JsonObject result = new JsonObject();

                try {
                    if (checkDisId(getJsonById.getLong(DIS_ID))) {

                        JsonObject discoveryStatus = checkDiscoveryStatus(getJsonById.getLong(DIS_ID));

                        if (discoveryStatus.getString("discovery").equals("true")) {

                            JsonObject value = getRunProvisionQuery(getJsonById.getLong(DIS_ID));

                            insertIntoProDB(value);

                            result.put("Provision", Constant.SUCCESS);

                            String disName = value.getString(Constant.DIS_NAME);

                            long disID = getProProfile(disName);

                            result.put("monitorID", disID);

                            value.put("monitorID", disID);

                            provisionBlocking.complete(value);

                        } else {
                            result.put(Constant.STATUS, Constant.FAILED);

                            result.put(Constant.ERROR, "Not Discovered");

                            provisionBlocking.fail(result.encode());
                        }
                    } else {
                        result.put(Constant.STATUS, Constant.FAILED);

                        result.put(Constant.ERROR, "Wrong Discovery ID");

                        provisionBlocking.fail(result.encode());
                    }

                } catch (Exception exception) {
                    LOGGER.error(exception.getMessage());

                }


            }).onComplete(onCompleteHandler -> {
                if (onCompleteHandler.succeeded()) {

                    JsonObject resultValue = onCompleteHandler.result();

                    long monitID = resultValue.getLong("monitorID");

                    String metricType = resultValue.getString(Constant.METRIC_TYPE);

                    if (!checkMetricProfile(monitID, metricType)) {
                        insertIntoUserMetricData(monitID, metricType);

                        Bootstrap.vertx.eventBus().<JsonObject>request(Constant.EVENTBUS_POLLING, resultValue, pollingHandler -> {
                            JsonObject entries = pollingHandler.result().body();

                            if (pollingHandler.succeeded()) {

                                provisionHandler.reply(entries);

                            } else {
                                provisionHandler.fail(-1, "failed");
                            }
                        });
                    } else {
                        Bootstrap.vertx.eventBus().<JsonObject>request(Constant.EVENTBUS_POLLING, resultValue, pollingHandler -> {
                            JsonObject entries = pollingHandler.result().body();

                            if (pollingHandler.succeeded()) {

                                provisionHandler.reply(entries);

                            } else {
                                provisionHandler.fail(-1, "failed");
                            }
                        });
                    }
                }
            });


        });

        eventBus.<JsonObject>consumer(Constant.EVENTBUS_GETMETRIC_FOR_POLLING, getData -> {
            JsonObject getMonitorData = getData.body();

            Bootstrap.vertx.<JsonObject>executeBlocking(metricPolling -> {

                Long id = getMonitorData.getLong("monitorID");

                String metricType = getMonitorData.getString(Constant.METRIC_TYPE);

                JsonObject result = getMonitorQuery(id, metricType);

                metricPolling.complete(result);
            }).onComplete(handler -> {
                if (handler.succeeded()) {
                    JsonObject resultValue = handler.result();
                    getData.reply(resultValue);
                } else {
                    getData.fail(-1, handler.cause().getMessage());
                }
            });


        });


        //Discovery
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


    public void insertIntoUserMetricData(long id, String metricType) {

        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            String getmetric = "select  p.id, p.metricType, d.counter, d.scheduleTime from provisionTable as p Natural join defaultmetric as d where p.id='" + id + "' and p.metricType='" + metricType + "'";
            ResultSet resultSet = statement.executeQuery(getmetric);
            while (resultSet.next()) {
                JsonObject result = new JsonObject();
                Long monitorID = resultSet.getLong("id");
                String metricdata = resultSet.getString("metricType");
                String counter = resultSet.getString("counter");
                Long scheduleTime = resultSet.getLong("scheduleTime");

                result.put("monitorId", monitorID);
                result.put("metricType", metricdata);
                result.put("counter", counter);
                result.put("time", scheduleTime);


                PreparedStatement preparedStatement;


                String insertMonitorMetric = "INSERT INTO DiscoveryTemp.monitorMetricTable(monitorID,metricType,metricGroup,Time)"
                        + "VALUES(?,?,?,?)";
                preparedStatement = connection.prepareStatement(insertMonitorMetric);

                preparedStatement.setLong(1, result.getLong("monitorId"));
                preparedStatement.setString(2, result.getString("metricType"));
                preparedStatement.setString(3, result.getString("counter"));
                preparedStatement.setString(4, result.getString("time"));

                preparedStatement.execute();
            }
        } catch (SQLException exception) {
            LOGGER.error(exception.getMessage());
        }
    }

    public JsonObject checkDiscoveryStatus(long id) {
        JsonObject result = new JsonObject();
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();

            String checkIpvalue = "select discovery from DiscoveryTemp.discoveryTable where id='" + id + "'";

            ResultSet resultSet = statement.executeQuery(checkIpvalue);

            if (resultSet.next()) {
                boolean discovery = resultSet.getBoolean("discovery");
                result.put("discovery", discovery);
            }

        } catch (Exception exception) {

            LOGGER.error(exception.getMessage());

        }

        return result;
    }

    public JsonObject getMonitorQuery(long id, String metricType) {
        JsonObject arrayResult = new JsonObject();
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            String getById = "select * from provisionTable as p Natural join defaultmetric as d where p.id='" + id + "' and p.metricType='" + metricType + "'";
            ResultSet resultSet = statement.executeQuery(getById);
            while (resultSet.next()) {
                JsonObject result = new JsonObject();
                Long monitorID = resultSet.getLong("id");
                String metricdata = resultSet.getString("metricType");
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                String community = resultSet.getString("community");
                String version = resultSet.getString("version");
                String ip = resultSet.getString("ipAddress");
                int port = resultSet.getInt("port");
                String counter = resultSet.getString("counter");
                Long scheduleTime = resultSet.getLong("scheduleTime");
                String monitorIDmetricname = monitorID + resultSet.getString("counter");

                result.put("idAndGroup", monitorIDmetricname);
                result.put("monitorId", monitorID);
                result.put(Constant.METRIC_TYPE, metricdata);
                result.put(Constant.USER, username);
                result.put(Constant.PASSWORD, password);
                result.put(Constant.COMMUNITY, community);
                result.put(Constant.VERSION, version);
                result.put(Constant.IP_ADDRESS, ip);
                result.put(Constant.PORT, port);
                result.put("metricGroup", counter);
                result.put("time", scheduleTime);
                result.put("category", "polling");


                arrayResult.put(monitorIDmetricname, result);

            }


        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
        }
        return arrayResult;

    }

    public JsonObject getRunProvisionQuery(long id) {
        JsonObject result = new JsonObject();
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            //select * from discoveryTable as d join credentialsTable as a on d.credProfile = a.id where d.id = 90007 ;
            String getById = "select * from DiscoveryTemp.discoveryTable as d join DiscoveryTemp.credentialsTable as c on d.credProfile = c.id where d.id='" + id + "'";
            ResultSet resultSet = statement.executeQuery(getById);
            if (resultSet.next()) {

                String disName = resultSet.getString("disName");
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                String community = resultSet.getString("community");
                String version = resultSet.getString("version");
                String ip = resultSet.getString("ipAddress");
                String type = resultSet.getString("metricType");
                int port = resultSet.getInt("port");


                result.put(DIS_ID, id);
                result.put(Constant.USER, username);
                result.put(Constant.PASSWORD, password);
                result.put(Constant.COMMUNITY, community);
                result.put(Constant.VERSION, version);
                result.put(Constant.IP_ADDRESS, ip);
                result.put(Constant.METRIC_TYPE, type);
                result.put(Constant.PORT, port);
                result.put(Constant.DIS_NAME, disName);
            }


        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
        }
        return result;

    }

    public JsonObject getRundiscoveryQuery(long id) {
        JsonObject result = new JsonObject();
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            //select * from discoveryTable as d join credentialsTable as a on d.credProfile = a.id where d.id = 90007 ;
            String getById = "select * from DiscoveryTemp.discoveryTable as d join DiscoveryTemp.credentialsTable as c on d.credProfile = c.id where d.id='" + id + "'";
            ResultSet resultSet = statement.executeQuery(getById);
            if (resultSet.next()) {

                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                String community = resultSet.getString("community");
                String version = resultSet.getString("version");
                String ip = resultSet.getString("ipAddress");
                String type = resultSet.getString("metricType");
                int port = resultSet.getInt("port");


                result.put(DIS_ID, id);
                result.put(Constant.USER, username);
                result.put(Constant.PASSWORD, password);
                result.put(Constant.COMMUNITY, community);
                result.put(Constant.VERSION, version);
                result.put(Constant.IP_ADDRESS, ip);
                result.put(Constant.METRIC_TYPE, type);
                result.put(Constant.PORT, port);
            }


        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
        }
        return result;

    }


    public boolean checkMetricProfile(long id, String metrictype) {
        boolean result = false;
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();

            String checkIpvalue = "select monitorID from DiscoveryTemp.monitorMetricTable where monitorID='" + id + "' and metricType='" + metrictype + "'";

            ResultSet resultSet = statement.executeQuery(checkIpvalue);

            result = resultSet.next();

        } catch (Exception exception) {

            LOGGER.error(exception.getMessage());

        }

        return result;
    }

    public boolean checkCredProfile(long id) {
        boolean result = false;
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();

            String checkIpvalue = "select credProfile from DiscoveryTemp.discoveryTable where id='" + id + "'";

            ResultSet resultSet = statement.executeQuery(checkIpvalue);

            result = resultSet.next();

        } catch (Exception exception) {

            LOGGER.error(exception.getMessage());

        }

        return result;
    }

    public JsonArray getAllCred() {
        JsonArray arrayResult = new JsonArray();

        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            String getById = "select * from DiscoveryTemp.credentialsTable";
            ResultSet resultSet = statement.executeQuery(getById);
            while (resultSet.next()) {
                JsonObject result = new JsonObject();
                long credId = resultSet.getLong("id");
                String protocol = resultSet.getString("protocol");
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                String community = resultSet.getString("community");
                String version = resultSet.getString("version");
                String credName = resultSet.getString("credName");

                result.put(Constant.CRED_ID, credId);
                result.put(Constant.PROTOCOL, protocol);
                result.put(Constant.USER, username);
                result.put(Constant.PASSWORD, password);
                result.put(Constant.COMMUNITY, community);
                result.put(Constant.VERSION, version);
                result.put(Constant.CRED_NAME, credName);

                arrayResult.add(result);
            }


        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
        }
        return arrayResult;
    }

    public JsonArray getAllDis() {
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
                result.put(Constant.IP_ADDRESS, ip);
                result.put(Constant.METRIC_TYPE, type);
                result.put("cred.profile", credProfile);
                result.put(Constant.PORT, port);
                result.put(Constant.DIS_NAME, disName);

                arrayResult.add(result);
            }


        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
        }
        return arrayResult;
    }

    public JsonArray getAllMonitorMetric() {
        JsonArray arrayResult = new JsonArray();

        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            String getAll = "select * from DiscoveryTemp.monitorMetricTable";
            ResultSet resultSet = statement.executeQuery(getAll);
            while (resultSet.next()) {
                JsonObject result = new JsonObject();
                long monitorID = resultSet.getLong("monitorID");
                String type = resultSet.getString("metricType");
                String group = resultSet.getString("metricGroup");
                long time = resultSet.getLong("Time");

                result.put("monitorID", monitorID);
                result.put(Constant.METRIC_TYPE, type);
                result.put("metricGroup", group);
                result.put("time", time);
                arrayResult.add(result);
            }


        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
        }
        return arrayResult;
    }

    public void updateIntoMonitorMetricDB(JsonObject updateDb) {

        try (Connection connection = getConnection()) {


            PreparedStatement discoveryStmt;
            String updateUserSql = "UPDATE DiscoveryTemp.monitorMetricTable SET metricType = ?,metricGroup = ? , Time = ? WHERE monitorID = ?";
            discoveryStmt = connection.prepareStatement(updateUserSql);
            discoveryStmt.setString(1, updateDb.getString("metric.type"));

            discoveryStmt.setString(2, updateDb.getString("metricGroup"));

            discoveryStmt.setLong(3, updateDb.getLong("time"));

            discoveryStmt.setLong(4, updateDb.getLong("monitorID"));


            discoveryStmt.executeUpdate();

        } catch (Exception exception) {
            LOGGER.error("Error");
        }

    }

    public void updateIntoDisDB(JsonObject updateDb) {

        try (Connection connection = getConnection()) {


            PreparedStatement discoveryStmt;
            String updateUserSql = "UPDATE DiscoveryTemp.discoveryTable SET ipAddress = ?,credProfile = ? , disName = ? WHERE ID = ?";
            discoveryStmt = connection.prepareStatement(updateUserSql);
            discoveryStmt.setString(1, updateDb.getString("ip.address"));

            discoveryStmt.setString(2, updateDb.getString("cred.profile"));

            discoveryStmt.setString(3, updateDb.getString("dis.name"));

            discoveryStmt.setLong(4, updateDb.getLong(DIS_ID));


            discoveryStmt.executeUpdate();

        } catch (Exception exception) {
            LOGGER.error("Error");
        }

    }

    public void updateIntoCredDB(JsonObject updateDb) {

        try (Connection connection = getConnection()) {


            PreparedStatement discoveryStmt;
            String updateUserSql = "UPDATE DiscoveryTemp.credentialsTable SET username = ?,password = ? , community = ?, version = ?, credName = ? WHERE ID = ?";
            discoveryStmt = connection.prepareStatement(updateUserSql);
            discoveryStmt.setString(1, updateDb.getString(Constant.USER));

            discoveryStmt.setString(2, updateDb.getString(Constant.PASSWORD));

            discoveryStmt.setString(3, updateDb.getString(Constant.COMMUNITY));

            discoveryStmt.setString(4, updateDb.getString(Constant.VERSION));

            discoveryStmt.setString(5, updateDb.getString(Constant.CRED_NAME));

            discoveryStmt.setLong(6, updateDb.getLong(Constant.CRED_ID));


            discoveryStmt.executeUpdate();

        } catch (Exception exception) {
            LOGGER.error("Error");
        }

    }

    public void updateDiscovery(Long id) {

        try (Connection connection = getConnection()) {
            PreparedStatement discoveryStmt;
            String updateUserSql = "UPDATE DiscoveryTemp.discoveryTable SET discovery = true WHERE ID = ?";
            discoveryStmt = connection.prepareStatement(updateUserSql);
            discoveryStmt.setLong(1, id);
            discoveryStmt.executeUpdate();
        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
        }


    }

    public JsonObject getByDisID(long id) {
        JsonObject result = new JsonObject();
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            String getById = "select * from DiscoveryTemp.discoveryTable where id='" + id + "'";
            ResultSet resultSet = statement.executeQuery(getById);
            if (resultSet.next()) {
                long disId = resultSet.getLong("id");
                String ip = resultSet.getString("ipAddress");
                String type = resultSet.getString("metricType");
                Long credProfile = resultSet.getLong("credProfile");
                int port = resultSet.getInt("port");
                String disName = resultSet.getString("disName");

                result.put(DIS_ID, disId);
                result.put(Constant.IP_ADDRESS, ip);
                result.put(Constant.METRIC_TYPE, type);
                result.put("cred.profile", credProfile);
                result.put(Constant.PORT, port);
                result.put(Constant.DIS_NAME, disName);
            }


        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
        }
        return result;
    }

    public JsonObject getByCredID(long id) {
        JsonObject result = new JsonObject();
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            String getById = "select * from DiscoveryTemp.credentialsTable where id='" + id + "'";
            ResultSet resultSet = statement.executeQuery(getById);
            if (resultSet.next()) {
                long credId = resultSet.getLong("id");
                String protocol = resultSet.getString("protocol");
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                String community = resultSet.getString("community");
                String version = resultSet.getString("version");
                String credName = resultSet.getString("credName");

                result.put(Constant.CRED_ID, credId);
                result.put(Constant.PROTOCOL, protocol);
                result.put(Constant.USER, username);
                result.put(Constant.PASSWORD, password);
                result.put(Constant.COMMUNITY, community);
                result.put(Constant.VERSION, version);
                result.put(Constant.CRED_NAME, credName);
            }


        } catch (Exception exception) {
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

    public Long getProProfile(String name) {
        long result = 0;
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            String getID = "select id from DiscoveryTemp.provisionTable where monitorName='" + name + "'";
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

    public void insertIntoProDB(JsonObject jsonObject) {
        try (Connection connection = getConnection()) {

            PreparedStatement discoveryStmt;
            String insertUserSql = "INSERT INTO DiscoveryTemp.provisionTable(username,password,community,version,port,ipAddress,metricType,monitorName)"
                    + "VALUES(?,?,?,?,?,?,?,?)";
            discoveryStmt = connection.prepareStatement(insertUserSql);


            discoveryStmt.setString(1, jsonObject.getString("user"));

            discoveryStmt.setString(2, jsonObject.getString("password"));

            discoveryStmt.setString(3, jsonObject.getString("community"));

            discoveryStmt.setString(4, jsonObject.getString("version"));

            discoveryStmt.setInt(5, jsonObject.getInteger("port"));

            discoveryStmt.setString(6, jsonObject.getString("ip.address"));

            discoveryStmt.setString(7, jsonObject.getString("metric.type"));

            discoveryStmt.setString(8, jsonObject.getString("dis.name"));

            discoveryStmt.execute();
        } catch (SQLException exception) {
            LOGGER.error(exception.getMessage());
        }
    }

    public void insertIntoDisDB(JsonObject jsonObject) {
        try (Connection connection = getConnection()) {

            PreparedStatement discoveryStmt;
            String insertUserSql = "INSERT INTO DiscoveryTemp.discoveryTable(ipAddress,metricType,credProfile,port,disName,discovery)"
                    + "VALUES(?,?,?,?,?,?)";
            discoveryStmt = connection.prepareStatement(insertUserSql);

            boolean discovery = false;


            discoveryStmt.setString(1, jsonObject.getString("ip.address"));

            discoveryStmt.setString(2, jsonObject.getString("metric.type"));

            discoveryStmt.setString(3, jsonObject.getString("cred.profile"));

            discoveryStmt.setInt(4, jsonObject.getInteger("port"));

            discoveryStmt.setString(5, jsonObject.getString("dis.name"));

            discoveryStmt.setBoolean(6, discovery);


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
