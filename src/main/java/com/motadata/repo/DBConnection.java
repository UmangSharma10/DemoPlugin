package com.motadata.repo;

public class DBConnection {
    DBConnection() throws ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
    }
}
