/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.api;


import java.sql.*;
import com.txsing.conhub.ult.*;
import java.util.List;
/**
 *
 * @author txsing
 */
public class APIsTest {
       public static void main(String[] args) {
           Connection conn = DBConnector.connectPostgres();
           String result = APIs.getParentalImageID("ef5b", conn);
           System.err.println(result);
    }
    
}
