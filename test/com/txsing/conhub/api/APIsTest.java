/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.api;

import java.sql.*;
import com.txsing.conhub.ult.*;
import java.util.*;

/**
 *
 * @author txsing
 */
public class APIsTest {

    public static void main(String[] args) {
        Connection conn = DBConnector.connectPostgres();
        String result = getIntersectionTest("ef5b", "7dbd");
        System.err.println(result);
    }
    
    static String getParentalImageIDTest(String shortID, Connection conn){
        try{
           return APIs.getParentalImageID(shortID, conn);
        }catch(Exception e){
            System.err.println(e.getMessage());
            return null;
        }
    }
    
        static String getIntersectionTest(String id1, String id2){
        try{
           return APIs.getIntersection(id1, id2);
        }catch(Exception e){
            System.err.println(e.getMessage());
            return null;
        }
    }
    

}
