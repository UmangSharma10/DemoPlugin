package com.mindarray;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

import static com.mindarray.Constant.*;

public class DatabaseEngine extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseEngine.class);


    @Override
    public void start(Promise<Void> startPromise) {

        LOGGER.debug("DATABASE ENGINE DEPLOYED");

        EventBus eventBus = Bootstrap.vertx.eventBus();

        JsonObject result1 = new JsonObject();

        eventBus.<JsonObject>localConsumer(EVENTBUS_DATABASE, handler ->{
           switch (handler.body().getString(METHOD)){
               case EVENTBUS_CHECK_CREDNAME:

                   JsonObject userCredData = handler.body();

                   Bootstrap.vertx.executeBlocking(event -> {
                       try {
                            String tableName  = "credentialsTable";
                            String columnName = "cred_name";
                           if (Boolean.FALSE.equals(checkName(tableName, columnName, userCredData.getString(Constant.CRED_NAME)))) {

                               result1.put(Constant.STATUS, Constant.SUCCESS);

                               event.complete(result1);

                           } else {

                               result1.put(Constant.STATUS, Constant.FAILED);

                               result1.put(Constant.ERROR, "CRED.NAME NOT UNIQUE");

                               event.fail(result1.encode());
                           }

                       } catch (Exception exception) {

                           LOGGER.error(exception.getMessage());

                       }
                   }).onComplete(res -> {

                       if (res.succeeded()) {

                           handler.reply(result1);

                       } else {
                           handler.fail(-1, res.cause().getMessage());
                       }

                   });
                   break;

               case EVENTBUS_INSERTCRED:
                   JsonObject jsonDbData = handler.body();
                   vertx.executeBlocking(blockinhandler -> {

                       try {

                           if (Boolean.FALSE.equals(checkName("credentialsTable", "cred_name", jsonDbData.getString(Constant.CRED_NAME)))) {

                               insertIntoCredDB(jsonDbData);

                               LOGGER.info("Data Inserted");

                               result1.put(Constant.DB_STATUS_INSERTION, Constant.SUCCESS);

                               String credName = jsonDbData.getString(Constant.CRED_NAME);

                               long credID = getCredProfile(credName);

                               result1.put(Constant.CRED_PROFILE, credID);

                               blockinhandler.complete(result1);

                           } else {

                               result1.put(Constant.DB_STATUS_INSERTION, Constant.FAILED);

                               result1.put(Constant.ERROR, "Duplicate cred.name");

                               blockinhandler.fail(result1.encode());

                           }

                       } catch (Exception exception) {

                           result1.put(Constant.DB_STATUS_INSERTION, Constant.FAILED);

                           result1.put(Constant.ERROR, exception.getMessage());

                           blockinhandler.fail(result1.encode());
                       }


                   }).onComplete(handler1 -> {
                       if (handler1.succeeded()) {

                           handler.reply(handler1.result());

                       } else {

                           handler.fail(-1, handler1.cause().getMessage());

                       }
                   });
                   break;

               case EVENTBUS_CHECKID_CRED:
                   JsonObject jsondbData  = handler.body();

                   var id = jsondbData.getString(CRED_ID);

                   long longId = Long.parseLong(id);

                   JsonObject checkcredData = new JsonObject().put(Constant.CRED_ID, longId);

                   Bootstrap.vertx.executeBlocking(event -> {

                       try {
                           String tablename = "credentialsTable";
                           String columnName = "credentialsTable_id";

                           if (Boolean.TRUE.equals(checkId(tablename, columnName, checkcredData.getLong(Constant.CRED_ID)))) {

                               result1.put(Constant.STATUS, Constant.SUCCESS);

                               event.complete(result1);

                           } else {
                               result1.put(Constant.STATUS, Constant.FAILED);

                               result1.put(Constant.ERROR, "WRONG ID");

                               event.fail(result1.encode());
                           }

                       } catch (Exception exception) {
                           LOGGER.error(exception.getMessage());

                       }
                   }).onComplete(res -> {
                       if (res.succeeded()) {
                           handler.reply(result1);
                       } else {
                           handler.fail(-1, res.cause().getMessage());
                       }
                   });
                   break;

               case EVENTBUS_DELETECRED:

                   JsonObject jsondeleteData  = handler.body();

                   var deleteId = jsondeleteData.getString(CRED_ID);

                   long longid = Long.parseLong(deleteId);

                   JsonObject deleteObject = new JsonObject().put(Constant.CRED_ID, longid);

                   vertx.executeBlocking(blockinhandler -> {

                       JsonObject result = new JsonObject();

                       try {
                           String tableName = "credentialsTable";
                           String columnName= "credentialsTable_id";

                           if (Boolean.TRUE.equals(checkId(tableName, columnName,deleteObject.getLong(Constant.CRED_ID)))) {

                               if (Boolean.FALSE.equals(checkId("discoveryTable","cred_profile", deleteObject.getLong(Constant.CRED_ID)))) {

                                   boolean value = delete(tableName, columnName, deleteObject.getLong(Constant.CRED_ID));

                                   if (value){

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

                   break;

               case EVENTBUS_UPDATE_CRED:
                   JsonObject updataData = handler.body();

                   vertx.executeBlocking(blockinhandler -> {

                       JsonObject result = new JsonObject();

                       try {

                           if (Boolean.TRUE.equals(checkId("credentialsTable", "credentialsTable_id", updataData.getLong(Constant.CRED_ID)))) {

                               update("credentialsTable", updataData);

                               result.put(Constant.DB_STATUS_UPDATE, Constant.SUCCESS);

                               blockinhandler.complete(result);

                           } else {

                               result.put(Constant.DB_STATUS_UPDATE, Constant.FAILED);

                               result.put(Constant.ERROR, "CRED ID DOESNT EXIST IN CRED DB");

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
                   break;

               case EVENTBUS_GETCREDBYID:
                   JsonObject getData = handler.body();
                   String getId = getData.getString(CRED_ID);
                   long longgetid = Long.parseLong(getId);
                   JsonObject getJsonById = new JsonObject().put(Constant.CRED_ID, longgetid);
                   vertx.executeBlocking(blockinhandler -> {

                       try {
                           String tableName = "credentialsTable";
                           String columnName= "credentialsTable_id";
                           if (Boolean.TRUE.equals(checkId(tableName, columnName , getJsonById.getLong(Constant.CRED_ID)))) {

                               JsonObject value = getByID(tableName , columnName, getJsonById.getLong(Constant.CRED_ID));

                               result1.put(Constant.STATUS, Constant.SUCCESS);

                               result1.put(RESULT, value);

                               blockinhandler.complete(result1);

                           } else {

                               result1.put(Constant.STATUS, Constant.FAILED);

                               result1.put(Constant.ERROR, "Wrong Credential ID");

                               blockinhandler.fail(result1.encode());

                           }

                       } catch (Exception exception) {

                           result1.put(Constant.STATUS, Constant.FAILED);

                           result1.put(Constant.ERROR, exception.getMessage());

                           blockinhandler.fail(result1.encode());
                       }

                   }).onComplete(handler1 -> {
                       if (handler1.succeeded()) {
                           handler.reply(handler1.result());
                       } else {
                           handler.fail(-1, handler1.cause().getMessage());
                       }
                   });
                   break;

               case EVENTBUS_GETALLCRED:
                   vertx.executeBlocking(blockinhandler -> {

                       try {

                           JsonArray value = getAllCred();

                           result1.put(Constant.STATUS, Constant.SUCCESS);

                           result1.put(RESULT, value);

                           blockinhandler.complete(result1);


                       } catch (Exception exception) {

                           result1.put(Constant.STATUS, Constant.FAILED);

                           result1.put(Constant.ERROR, exception.getMessage());

                           blockinhandler.fail(result1.encode());
                       }

                       blockinhandler.complete(result1);

                   }).onComplete(handler1 -> {
                       if (handler1.succeeded()) {
                           handler.reply(handler1.result());
                       } else {
                           handler.fail(-1, handler1.cause().getMessage());
                       }
                   });

                   break;

               case EVENTBUS_CHECK_DISNAME:

                   JsonObject userDisData = handler.body();

                   Bootstrap.vertx.executeBlocking(event -> {
                       try {
                           if (Boolean.FALSE.equals(checkName("discoveryTable", "dis_name", userDisData.getString(Constant.DIS_NAME)))) {
                               result1.put(Constant.STATUS, Constant.SUCCESS);

                               event.complete(result1);
                           } else {

                               result1.put(Constant.STATUS, Constant.FAILED);

                               result1.put(Constant.ERROR, "DIS.NAME NOT UNIQUE");

                               event.fail(result1.encode());
                           }

                       } catch (Exception exception) {
                           LOGGER.error(exception.getMessage());

                       }
                   }).onComplete(res -> {
                       if (res.succeeded()) {
                           handler.reply(res.result());
                       } else {
                           handler.fail(-1, res.cause().getMessage());
                       }

                   });
                   break;

               case EVENTBUS_INSERTDISCOVERY:

                   JsonObject insertDisData = handler.body();

                   vertx.executeBlocking(blockinhandler -> {

                       try {

                           if (Boolean.FALSE.equals(checkName("discoveryTable", "dis_name", insertDisData.getString(Constant.DIS_NAME)))) {

                               insertIntoDisDB(insertDisData);

                               result1.put(Constant.DB_STATUS_INSERTION, Constant.SUCCESS);

                               String disName = insertDisData.getString(Constant.DIS_NAME);

                               long disID = getDisProfile(disName);

                               result1.put(DIS_ID, disID);

                               blockinhandler.complete(result1);

                           } else {

                               result1.put(Constant.DB_STATUS_INSERTION, Constant.FAILED);

                               result1.put(Constant.ERROR, "Duplicate dis.name");

                               blockinhandler.fail(result1.encode());
                           }

                       } catch (Exception exception) {

                           result1.put(Constant.DB_STATUS_INSERTION, Constant.FAILED);

                           result1.put(Constant.ERROR, exception.getMessage());

                           blockinhandler.fail(result1.encode());
                       }

                   }).onComplete(handler1 -> {
                       if (handler1.succeeded()) {

                           handler.reply(handler1.result());

                       } else {

                           handler.fail(-1, handler1.cause().getMessage());

                       }
                   });
                   break;

               case EVENTBUS_CHECKID_DISCOVERY:

                   var disId = handler.body().getString(DIS_ID);

                   long disidL = Long.parseLong(disId);

                   JsonObject checkDisData = new JsonObject().put(DIS_ID, disidL);

                   Bootstrap.vertx.executeBlocking(event -> {

                       try {

                           if (Boolean.TRUE.equals(checkId("discoveryTable", "discoveryTable_id", checkDisData.getLong(DIS_ID)))) {

                               result1.put(Constant.STATUS, Constant.SUCCESS);

                               event.complete(result1);

                           } else {
                               result1.put(Constant.STATUS, Constant.FAILED);

                               result1.put(Constant.ERROR, "WRONG ID");

                               event.fail(result1.encode());
                           }

                       } catch (Exception exception) {
                           LOGGER.error(exception.getMessage());

                       }
                   }).onComplete(res -> {
                       if (res.succeeded()) {
                           handler.reply(result1);
                       } else {
                           handler.fail(-1, res.cause().getMessage());
                       }
                   });

                   break;

               case EVENTBUS_DELETEDIS:
                   var delid = handler.body().getString(DIS_ID);

                   long delidL = Long.parseLong(delid);

                   JsonObject delDisData = new JsonObject().put(DIS_ID, delidL);

                   vertx.executeBlocking(blockinhandler -> {

                       JsonObject result = new JsonObject();

                       try {

                           if (Boolean.TRUE.equals(checkId("discoveryTable", "discoveryTable_id", delDisData.getLong(DIS_ID)))) {

                               boolean value = delete("discoveryTable", "discoveryTable_id", delDisData.getLong(DIS_ID));

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
                   break;

               case EVENTBUS_UPDATE_DIS:
                   JsonObject updateDisData = handler.body();

                   vertx.executeBlocking(blockinhandler -> {

                       JsonObject result = new JsonObject();

                       try {

                           if (Boolean.TRUE.equals(checkId("discoveryTable","cred_profile",updateDisData.getLong(Constant.CRED_PROFILE)))) {

                               update("discoveryTable",updateDisData);

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
                   break;

               case EVENTBUS_GETDISCOVERY:
                   String getDisid = handler.body().getString(DIS_ID);

                   long getdisLong = Long.parseLong(getDisid);

                   JsonObject getDisById = new JsonObject().put(DIS_ID, getdisLong);

                   vertx.executeBlocking(blockinhandler -> {

                       try {

                           if (Boolean.TRUE.equals(checkId("discoveryTable", "discoveryTable_id", getDisById.getLong(DIS_ID)))) {

                               JsonObject value = getByDisID(getDisById.getLong(DIS_ID));

                               result1.put(Constant.STATUS, Constant.SUCCESS);

                               result1.put("Result", value);

                               blockinhandler.complete(result1);

                           } else {

                               result1.put(Constant.STATUS, Constant.FAILED);

                               result1.put(Constant.ERROR, "Wrong Discovery ID");

                               blockinhandler.fail(result1.encode());

                           }

                       } catch (Exception exception) {

                           result1.put(Constant.STATUS, Constant.FAILED);

                           result1.put(Constant.ERROR, exception.getMessage());

                           blockinhandler.fail(result1.encode());
                       }


                   }).onComplete(handler1 -> {
                       if (handler1.succeeded()) {
                           handler.reply(handler1.result());
                       } else {
                           handler.fail(-1, handler1.cause().getMessage());
                       }
                   });
                   break;

               case EVENTBUS_GETALLDIS:
                   Bootstrap.vertx.executeBlocking(blockinhandler -> {

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
                           handler.reply(handler1.result());
                       } else {
                           handler.fail(-1, handler1.cause().getMessage());
                       }
                   });

                   break;

               case EVENTBUS_RUN_DISCOVERY:
                   String runId = handler.body().getString(DIS_ID);

                   long runIdL = Long.parseLong(runId);

                   JsonObject runDataById = new JsonObject().put(DIS_ID, runIdL);

                   Bootstrap.vertx.<JsonObject>executeBlocking(blockinghandler -> {

                       try {

                           if (Boolean.TRUE.equals(checkId("discoveryTable","discoveryTable_id",runDataById.getLong(DIS_ID)))) {

                               JsonObject discoveryStatus = checkDiscoveryStatus(runDataById.getLong(DIS_ID));

                               if (discoveryStatus.getString("discovery").equals("false")) {

                                   JsonObject value = getRundiscoveryQuery(runDataById.getLong(DIS_ID));

                                   value.put(CATEGORY, "discovery");
                                   LOGGER.debug(value.encode());

                                   result1.put(Constant.STATUS, Constant.SUCCESS);

                                   blockinghandler.complete(value);

                               } else {
                                   result1.put(Constant.STATUS, Constant.FAILED);

                                   result1.put(Constant.ERROR, "Already Discovered");

                                   blockinghandler.fail(result1.encode());


                               }


                           } else {

                               result1.put(Constant.STATUS, Constant.FAILED);

                               result1.put(Constant.ERROR, "Wrong Discovery ID");

                               blockinghandler.fail(result1.encode());


                           }

                       } catch (Exception exception) {

                           result1.put(Constant.STATUS, Constant.FAILED);

                           result1.put(Constant.ERROR, exception.getMessage());

                           blockinghandler.fail(result1.encode());
                       }


                   }).onComplete(asyncResult -> {
                       if (asyncResult.succeeded()) {
                           JsonObject value = asyncResult.result();
                           eventBus.<JsonObject>request(Constant.EVENTBUS_DISCOVERY, value, discovery -> {
                               LOGGER.debug("Response {} ", discovery.result().body());
                               //data return in json
                               JsonObject result = discovery.result().body();

                               if (!result.containsKey(Constant.ERROR)) {
                                   handler.reply(result);
                               } else {
                                   handler.fail(-1, asyncResult.cause().getMessage());
                               }

                           });
                       } else {
                           handler.fail(-1, asyncResult.cause().getMessage());

                       }
                   });

                   break;

           }
        });



        eventBus.<JsonObject>consumer(Constant.EVENTBUS_PROVISION, provisionHandler ->
        {
            String discoveryid = provisionHandler.body().getString(DIS_ID);

            long disIDL = Long.parseLong(discoveryid);

            JsonObject getProById = new JsonObject().put(DIS_ID, disIDL);

            Bootstrap.vertx.<JsonObject>executeBlocking(provisionBlocking -> {

                JsonObject result = new JsonObject();

                try {
                    if (Boolean.TRUE.equals(checkId("discoveryTable", "discoveryTable_id", getProById.getLong(DIS_ID)))) {

                        JsonObject discoveryStatus = checkDiscoveryStatus(getProById.getLong(DIS_ID));

                        if (discoveryStatus.getString("discovery").equals("true")) {

                            JsonObject value = getRunProvisionQuery(getProById.getLong(DIS_ID));
                            String tableName = "provisionTable";
                            String columnName="monitorName";

                            if (Boolean.FALSE.equals(checkName(tableName, columnName, value.getString(Constant.DIS_NAME)))) {

                                insertIntoProDB(value);

                                result.put("Provision", Constant.SUCCESS);

                                String disName = value.getString(Constant.DIS_NAME);

                                long disID = getProProfile(disName);

                                result.put("monitorID", disID);

                                value.put("monitorID", disID);

                                provisionBlocking.complete(value);
                            }
                            else {
                                result.put("Provision", Constant.SUCCESS);

                                String disName = value.getString(Constant.DIS_NAME);

                                long disID = getProProfile(disName);

                                result.put("monitorID", disID);

                                value.put("monitorID", disID);

                                provisionBlocking.complete(value);
                            }
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

                    if (checkMetricProfile(monitID, metricType)) {
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


        //subroute Monitor

        eventBus.<String>consumer(Constant.EVENTBUS_CHECK_MONITOR, apiDisId -> {
            JsonObject result = new JsonObject();

            var id = apiDisId.body();

            long longid = Long.parseLong(id);

            JsonObject userMonitorData = new JsonObject().put(Constant.MONITOR_ID, longid);

            Bootstrap.vertx.executeBlocking(event -> {

                try {
                    String tableName = "provisionTable";

                    String columnname = "id";

                    if (Boolean.TRUE.equals(checkId(tableName, columnname, userMonitorData.getLong(Constant.MONITOR_ID)))) {

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

        eventBus.<String>consumer(Constant.EVENTBUS_GET_MONITOR_BY_ID, getallData -> {
            String id = getallData.body();

            long longid = Long.parseLong(id);

            JsonObject getJsonById = new JsonObject().put(Constant.MONITOR_ID, longid);



            Bootstrap.vertx.<JsonObject>executeBlocking(blockinghandler ->{
                JsonObject result = new JsonObject();
                try {

                    String tableName = "provisionTable";

                    String columnname = "id";

                    if (Boolean.TRUE.equals(checkId(tableName, columnname, getJsonById.getLong(Constant.MONITOR_ID)))) {

                        JsonArray value = getMonitorData(getJsonById.getLong(MONITOR_ID));

                        result.put(Constant.STATUS, Constant.SUCCESS);

                        result.put("Result", value);

                        blockinghandler.complete(result);

                    } else {

                        result.put(Constant.STATUS, Constant.FAILED);

                        result.put(Constant.ERROR, "Wrong Monitor ID");

                        blockinghandler.fail(result.encode());

                    }

                } catch (Exception exception) {

                    result.put(Constant.STATUS, Constant.FAILED);

                    result.put(Constant.ERROR, exception.getMessage());

                    blockinghandler.fail(result.encode());
                }


            }).onComplete(handler1 -> {
                if (handler1.succeeded()) {
                    getallData.reply(handler1.result());
                } else {
                    getallData.fail(-1, handler1.cause().getMessage());
                }
            });
        });

        startPromise.complete();
    }


    public Boolean checkName(String tablename, String column, String name){
        boolean result = false;
        if (tablename  == null || column == null || name == null) {
            return result;
        } else {
            try (Connection connection = getConnection()) {
                var statement = connection.createStatement();
                var query = "select *  from " + tablename + " where " + column + "=\"" + name + "\"";
                var resultSet = statement.executeQuery(query);
                result = resultSet.next();

            } catch (Exception exception) {
                LOGGER.error(exception.getCause().getMessage());
            }
        }
        return result;
    }
    public Boolean checkId(String tablename, String column, Long id){
        boolean result = false;
        if (tablename  == null || column == null || id == null) {
            return result;
        } else {
                try (Connection connection = getConnection()) {
                    var statement = connection.createStatement();
                    var query = "select *  from " + tablename + " where " + column + "=\"" + id + "\"";
                    var resultSet = statement.executeQuery(query);
                    result = resultSet.next();

                } catch (Exception exception) {
                    LOGGER.error(exception.getCause().getMessage());
                }
            }
        return result;
    }

    public JsonArray getMonitorData(Long monitorId){
        JsonArray arrayResult = new JsonArray();

        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            String getById = "select * from provisionTable as p Natural join dumpAllData as d where p.id='" + monitorId + "'order by d.did desc limit 5";
            ResultSet resultSet = statement.executeQuery(getById);
            while (resultSet.next()) {
                JsonObject result = new JsonObject();
                long monId = resultSet.getLong("id");
                String ip = resultSet.getString("ipAddress");
                String type = resultSet.getString("metricType");
                String timestamp = resultSet.getString("timeStamp");
                String group = resultSet.getString("metricGroup");
                String value = resultSet.getString("value");

                result.put(MONITOR_ID, monId);
                result.put(Constant.IP_ADDRESS, ip);
                result.put(Constant.METRIC_TYPE, type);
                result.put("metricGroup", group);
                result.put("value", value);
                result.put("timeStamp", timestamp);

                arrayResult.add(result);
            }


        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
        }
        return arrayResult;
    }

    public void insertIntoDumpData(JsonObject dumpData){
        try (Connection connection = getConnection()) {

            PreparedStatement discoveryStmt;
            String insertUserSql = "INSERT INTO DiscoveryTemp.dumpAllData(monitorId, metricType, metricGroup, value)"
                    + "VALUES(?,?,?,?)";
            discoveryStmt = connection.prepareStatement(insertUserSql);

            discoveryStmt.setLong(1, dumpData.getLong("monitorId"));
            discoveryStmt.setString(2, dumpData.getString("metric.type"));
            discoveryStmt.setString(3, dumpData.getString("metricGroup"));
            discoveryStmt.setString(4, dumpData.getString("value"));

            discoveryStmt.execute();
            LOGGER.info("dataDumped");
        } catch (SQLException exception) {
            LOGGER.error(exception.getMessage());
        }
    }

    public void insertIntoUserMetricData(Long id, String metricType) {

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

    public JsonObject checkDiscoveryStatus(Long id) {
        JsonObject result = new JsonObject();
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();

            String checkIpvalue = "select discovery from DiscoveryTemp.discoveryTable where discoveryTable_id='" + id + "'";

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

    public JsonObject getMonitorQuery(Long id, String metricType) {
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

    public JsonObject getRunProvisionQuery(Long id) {
        JsonObject result = new JsonObject();
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            String getById = "select * from DiscoveryTemp.discoveryTable as d join DiscoveryTemp.credentialsTable as c on d.cred_profile = c.credentialsTable_id where d.discoveryTable_id='" + id + "'";
            ResultSet resultSet = statement.executeQuery(getById);
            if (resultSet.next()) {

                String disName = resultSet.getString("dis_name");
                String username = resultSet.getString("user");
                String password = resultSet.getString("password");
                String community = resultSet.getString("community");
                String version = resultSet.getString("version");
                String ip = resultSet.getString("ip_address");
                String type = resultSet.getString("metric_type");
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

    public JsonObject getRundiscoveryQuery(Long id) {
        JsonObject result = new JsonObject();
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            String getById = "select * from DiscoveryTemp.discoveryTable as d join DiscoveryTemp.credentialsTable as c on d.cred_profile = c.credentialsTable_id where d.discoveryTable_id='" + id + "'";
            ResultSet resultSet = statement.executeQuery(getById);
            if (resultSet.next()) {

                String username = resultSet.getString("user");
                String password = resultSet.getString("password");
                String community = resultSet.getString("community");
                String version = resultSet.getString("version");
                String ip = resultSet.getString("ip_address");
                String type = resultSet.getString("metric_type");
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

        return !result;
    }

    public JsonArray getAllCred() {
        JsonArray arrayResult = new JsonArray();

        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            String getById = "select * from DiscoveryTemp.credentialsTable";
            ResultSet resultSet = statement.executeQuery(getById);
            while (resultSet.next()) {
                JsonObject result = new JsonObject();
                long credId = resultSet.getLong("credentialsTable_id");
                String protocol = resultSet.getString("protocol");
                String username = resultSet.getString("user");
                String password = resultSet.getString("password");
                String community = resultSet.getString("community");
                String version = resultSet.getString("version");
                String credName = resultSet.getString("cred_name");

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
                long disId = resultSet.getLong("discoveryTable_id");
                String ip = resultSet.getString("ip_address");
                String type = resultSet.getString("metric_type");
                Long credProfile = resultSet.getLong("cred_profile");
                int port = resultSet.getInt("port");
                String disName = resultSet.getString("dis_name");

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

    public void update(String table, JsonObject updateDb){
        updateDb.remove(METHOD);
        var query = new StringBuilder();
        query.append("Update ").append(table).append(" set ");
        updateDb.stream().forEach(value -> {
            var column = value.getKey();
            var data = updateDb.getValue(column);
            if (column.contains(".")) {
                column = column.replace(".", "_");
            }
            query.append(column).append("=");
            if (data instanceof String) {
                query.append("\"").append(data).append("\",");
            } else {
                query.append(data).append(",");
            }
        });
        query.setLength(query.length() - 1);
        query.append(" where ").append(table).append("_id=\"").append(updateDb.getString(table + ".id")).append("\";");

        try (Connection connection = getConnection()) {
            var statement = connection.createStatement();
            statement.executeUpdate(query.toString());

        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
        }
    }

    public void updateDiscovery(Long id) {

        try (Connection connection = getConnection()) {
            PreparedStatement discoveryStmt;
            String updateUserSql = "UPDATE DiscoveryTemp.discoveryTable SET discovery = true WHERE discoveryTable_id = ?";
            discoveryStmt = connection.prepareStatement(updateUserSql);
            discoveryStmt.setLong(1, id);
            discoveryStmt.executeUpdate();
        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
        }


    }

    public JsonObject getByDisID(Long id) {
        JsonObject result = new JsonObject();
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            String getById = "select * from DiscoveryTemp.discoveryTable where discoveryTable_id='" + id + "'";
            ResultSet resultSet = statement.executeQuery(getById);
            if (resultSet.next()) {
                long disId = resultSet.getLong("discoveryTable_id");
                String ip = resultSet.getString("ip_address");
                String type = resultSet.getString("metric_type");
                Long credProfile = resultSet.getLong("cred_profile");
                int port = resultSet.getInt("port");
                String disName = resultSet.getString("dis_name");

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

    public JsonObject getByID(String tablename, String columnName, Long id) {
        JsonObject result = new JsonObject();
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            String getById = "select * from " + tablename + " where " + columnName + " = " + id + "";
            ResultSet resultSet = statement.executeQuery(getById);
            if (resultSet.next()) {
                long credId = resultSet.getLong("credentialsTable_id");
                String protocol = resultSet.getString("protocol");
                String username = resultSet.getString("user");
                String password = resultSet.getString("password");
                String community = resultSet.getString("community");
                String version = resultSet.getString("version");
                String credName = resultSet.getString("cred_name");

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
            String getID = "select credentialsTable_id from DiscoveryTemp.credentialsTable where cred_name='" + name + "'";
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
            String getID = "select discoveryTable_id from discoveryTable where dis_name='" + name + "'";
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

    public Boolean checkDisId(Long id) {

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

    public void insertIntoCredDB(JsonObject credData) {
        try (Connection connection = getConnection()) {

            PreparedStatement discoveryStmt;
            String insertUserSql = "INSERT INTO DiscoveryTemp.credentialsTable(protocol,user,password,community,version,cred_name)"
                    + "VALUES(?,?,?,?,?,?)";
            discoveryStmt = connection.prepareStatement(insertUserSql);


            discoveryStmt.setString(1, credData.getString("protocol"));

            discoveryStmt.setString(2, credData.getString("user"));

            discoveryStmt.setString(3, credData.getString("password"));

            discoveryStmt.setString(4, credData.getString("community"));

            discoveryStmt.setString(5, credData.getString("version"));

            discoveryStmt.setString(6, credData.getString("cred.name"));

            discoveryStmt.execute();
        } catch (SQLException exception) {
            LOGGER.error(exception.getMessage());
        }
    }

    public void insertIntoProDB(JsonObject probData) {
        try (Connection connection = getConnection()) {

            PreparedStatement discoveryStmt;
            String insertUserSql = "INSERT INTO DiscoveryTemp.provisionTable(username,password,community,version,port,ipAddress,metricType,monitorName)"
                    + "VALUES(?,?,?,?,?,?,?,?)";
            discoveryStmt = connection.prepareStatement(insertUserSql);


            discoveryStmt.setString(1, probData.getString("user"));

            discoveryStmt.setString(2, probData.getString("password"));

            discoveryStmt.setString(3, probData.getString("community"));

            discoveryStmt.setString(4, probData.getString("version"));

            discoveryStmt.setInt(5, probData.getInteger("port"));

            discoveryStmt.setString(6, probData.getString("ip.address"));

            discoveryStmt.setString(7, probData.getString("metric.type"));

            discoveryStmt.setString(8, probData.getString("dis.name"));

            discoveryStmt.execute();
        } catch (SQLException exception) {
            LOGGER.error(exception.getMessage());
        }
    }

    public void insertIntoDisDB(JsonObject disData) {
        try (Connection connection = getConnection()) {
            disData.remove(METHOD);
            PreparedStatement discoveryStmt;
            String insertUserSql = "INSERT INTO DiscoveryTemp.discoveryTable(ip_address,metric_type,cred_profile,port,dis_name,discovery)"
                    + "VALUES(?,?,?,?,?,?)";
            discoveryStmt = connection.prepareStatement(insertUserSql);

            boolean discovery = false;


            discoveryStmt.setString(1, disData.getString("ip.address"));

            discoveryStmt.setString(2, disData.getString("metric.type"));

            discoveryStmt.setString(3, disData.getString("cred.profile"));

            discoveryStmt.setInt(4, disData.getInteger("port"));

            discoveryStmt.setString(5, disData.getString("dis.name"));

            discoveryStmt.setBoolean(6, discovery);


            discoveryStmt.execute();
        } catch (SQLException exception) {
            LOGGER.error(exception.getMessage());
        }
    }


    public boolean delete(String tablename, String column, long id) {
        boolean result = false;

        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();

            String value = "delete from " + tablename + " where " + column + " ='" + id + "'";

            int resultSet = statement.executeUpdate(value);

            result = resultSet > 0;


        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
        }

        return result;
    }


    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/DiscoveryTemp", "root", "Mind@123");
    }
}
