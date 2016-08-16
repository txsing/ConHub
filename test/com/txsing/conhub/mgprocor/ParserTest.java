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
        parserChildTest("select reponame, tag from repositories, tags, images "
                + "where images.imageid IN child(42118e3df429f09ca581a9deb3df274601930e428e452f7e4e9f1833c56a100a) "
                + "AND tags.imageid = images.imageid AND tags.repoid = repositories.repoid");

    }

    static void parserChildTest(String sql) {
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
