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

    public static void main(String[] args) throws Exception {
        Connection conn = DBConnector.connectPostgres();
        
        //Parental List
        
        List<String> result = getParentalImgListTest("a78a",conn);
        for(String id : result)
            System.out.println(id);
        //Intersection
      String result1 = getImageIntersectionTest("a78a", "cd17", conn);
      System.err.println(result1);
        conn.close();
        
    }

    static String getParentalImageIDTest(String shortID, Connection conn) {
        try {
            return APIs.getParentalImageID(shortID, conn);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
    
    static String getImageIntersectionTest(String id1, String id2, Connection conn) {
        try {
            return APIs.getImageIntersection(id1, id2, conn);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
    
    static List<String> getChildImgListTest(String id, Connection conn){
        try {
            return APIs.getChildImgList(id, conn);
        } catch (Exception e) {
        }
        return null;
    }
    
    static List<String> getParentalImgListTest(String id, Connection conn){
        try {
            return APIs.getParentalImageIDsList(id, conn);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

}
