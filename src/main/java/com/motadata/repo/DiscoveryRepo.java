package com.motadata.repo;

import com.motadata.data.DiscoverCredentials;
import io.vertx.core.AbstractVerticle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DiscoveryRepo extends AbstractVerticle {

    //Class.forName("com.mysql.cj.jdbc.Driver");
    Connection con= DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/DiscoveryTemp","root","Mind@123");
    String insertUserSql = "INSERT INTO DiscoveryTemp.Discovery(device,port,user,password,version,community,host)"
            + "VALUES(?,?,?,?,?,?,?)";

    PreparedStatement stmt = null;
    public DiscoveryRepo() throws SQLException, ClassNotFoundException {

    }


    public void Create(DiscoverCredentials entry) throws SQLException {
        stmt = con.prepareStatement(insertUserSql);

        stmt.setString(1,entry.getMetricType());

        stmt.setInt(2,entry.getPort());

        stmt.setString(3,entry.getUser());

        stmt.setString(4,entry.getPassword());

        stmt.setString(5,entry.getVersion());

        stmt.setString(6,entry.getCommunity());

        stmt.setString(7,entry.getHost());

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
