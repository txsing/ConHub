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

    public static void main(String[] args) throws Exception{
//        parseChildTest("select reponame, tag from repositories, tags, images "
//                + "where images.imageid IN child(42118e3) "
//                + "AND tags.imageid = images.imageid AND tags.repoid = repositories.repoid");
        Connection conn = DBConnector.connectPostgres();
        String sql1 = "select R.reponame, T.tag "
                + "from Images I, Tags T, Repositories R "
                + "where I.imageid in CHILD(INTERSECTION('f41a','fef5')) "
                + "and I.imageid = T.imageid "
                + "and T.repoid = R.repoid";
        
        //String sql2 = Parser.parseIntersect(conn, sql1.toLowerCase());
        String sql3 = "TAG('test', select I.imageid "
                + "from Images I "
                + "where I.imageid in CHILD(INTERSECTION('7db','ef5b')))";
        //parseChildTest(sql2);
        Parser.parseCQL(conn, sql3);
    }
    
    static void parseChildTest(String sql) {
        try {
            Connection conn = DBConnector.connectPostgres();
            Statement stmt = conn.createStatement();
            String newSQL = Parser.parseChild(conn, sql);
            System.out.println(newSQL);
            ResultSet rs = stmt.executeQuery(newSQL);
            while (rs.next()) {
                System.out.println(rs.getString(1)+":"+rs.getString(2));
            }
            rs.close();
            stmt.close();
            conn.close();           
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
