/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.mgprocor;

import com.txsing.conhub.ult.DBConnector;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author txsing
 */
public class ParserTest {

    public static void main(String[] args) {
//        parseChildTest("select reponame, tag from repositories, tags, images "
//                + "where images.imageid IN child(42118e3) "
//                + "AND tags.imageid = images.imageid AND tags.repoid = repositories.repoid");
        Connection conn = DBConnector.connectPostgres();
        String output = Parser.parseCQL(conn, "select I.imageid from Images I where I.imageid in child(intersection(ef5b,7dbdd))");
        System.err.println(output);

    }

    static void parseIntesectionTest(String sql){
        try {
            String newSQL = Parser.parseIntersect(sql);
            System.out.println(newSQL);        
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }       
    }
    static void parseChildTest(String sql) {
        try {
            Connection conn = DBConnector.connectPostgres();
            Statement stmt = conn.createStatement();
            String newSQL = Parser.parseChild(conn, sql);
            System.out.println(newSQL);
            ResultSet rs = stmt.executeQuery(newSQL);
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }
            rs.close();
            stmt.close();
            conn.close();           
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
