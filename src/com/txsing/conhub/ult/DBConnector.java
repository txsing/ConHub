/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.ult;

/**
 *
 * @author txsing
 */
import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnector {

    public static Connection connectPostgres(String url, String user, String passwd) throws Exception {
        Class.forName(Constants.DB_POSTGRES_DRIVER).newInstance();
        Connection conn = DriverManager.getConnection(url, user, passwd);
        return conn;
    }

    //default
    public static Connection connectPostgres() {
        Connection conn = null;
        try {
            conn = connectPostgres(
                    Constants.DB_POSTGRES_URL,
                    Constants.DB_POSTGRES_USER,
                    Constants.DB_POSTGRES_PASSWORD);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return conn;
    }
}
