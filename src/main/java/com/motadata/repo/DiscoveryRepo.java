package com.motadata.repo;

import com.motadata.data.DiscoverCredentials;
import com.motadata.interpreter.Discovery;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.plaf.nimbus.State;
import java.sql.*;

public class DiscoveryRepo extends AbstractVerticle {
    public static final Logger LOG = LoggerFactory.getLogger(Discovery.class);
    Connection con= DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/DiscoveryTemp","root","Mind@123");

    JsonObject jsonObject = new JsonObject();
    @Override
    public void start(Promise<Void> startPromise)  {

        LOG.debug("DiscoveryRepo deployed");

        EventBus eventBus = vertx.eventBus();

        eventBus.consumer("my.request.db", handler ->{

           jsonObject = (JsonObject) handler.body();

            System.out.println("json " + jsonObject);

            try {

                if (!CheckIP(jsonObject)){

                    Create(jsonObject);

                    handler.reply("Inserted into Database");
                }

                else {

                    handler.reply("Duplicate IP address");

                }

            }
            catch (SQLException e) {

                throw new RuntimeException(e);

            }

        });

        startPromise.complete();
    }

    PreparedStatement stmt = null;
    public DiscoveryRepo() throws SQLException, ClassNotFoundException {

    }
    public Boolean CheckIP(JsonObject jsonObject) throws SQLException {

        Statement statement = con.createStatement();

        String checkIpvalue = "select host from DiscoveryTemp.Discovery where host='" + jsonObject.getString("host")+"'";

        ResultSet resultSet = statement.executeQuery(checkIpvalue);

        return resultSet.next();

    }
    public void Create(JsonObject jsonObject) throws SQLException {
        String insertUserSql = "INSERT INTO DiscoveryTemp.Discovery(device,port,user,password,version,community,host)"
                + "VALUES(?,?,?,?,?,?,?)";
        stmt = con.prepareStatement(insertUserSql);

        stmt.setString(1,jsonObject.getString("device"));

        stmt.setString(2,jsonObject.getString("port"));

        stmt.setString(3,jsonObject.getString("user"));

        stmt.setString(4,jsonObject.getString("password"));

        stmt.setString(5,jsonObject.getString("version"));

        stmt.setString(6,jsonObject.getString("community"));

        stmt.setString(7,jsonObject.getString("host"));

        stmt.execute();
    }


    public void Delete(String s) {

    }


    public void update(String s, DiscoverCredentials entry) {

    }


    public DiscoverCredentials read(String s) {
        return null;
    }
}
