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
import com.txsing.conhub.exceptions.DBConnectException;
import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnector {

    public static Connection connectPostgres(String url, String user, String passwd) 
            throws DBConnectException {
        Connection conn;
        try{
            Class.forName(Constants.DB_POSTGRES_DRIVER).newInstance();
            conn = DriverManager.getConnection(url, user, passwd);
        }catch(Exception e){
            throw new DBConnectException("LOG(DEBUG): "+e.getMessage());
        }
        return conn;
    }

    //default
    public static Connection connectPostgres() throws DBConnectException {
        Connection conn = null;
        conn = connectPostgres(
                Constants.DB_POSTGRES_URL,
                Constants.DB_POSTGRES_USER,
                Constants.DB_POSTGRES_PASSWORD);
        return conn;
    }
}
