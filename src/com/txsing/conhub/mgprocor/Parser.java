/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.mgprocor;

import com.txsing.conhub.api.APIs;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.txsing.conhub.ult.*;

/**
 *
 * @author txsing
 */
public class Parser {

    static String pattern_inter = "(INTERSECTION\\()([\\w]+)(,\\s*)([\\w]+)(\\))";
    static String pattern_child = "(child)(\\()(.{64})(\\))";
    static String pattern_tag = "(TAG\\()(\\w+)(,\\s*)(select.*)";

//    public static void main(String[] args) throws SQLException {
//        String sql1 = "select * from image where imgid1 = INTERSECTION(12, 34) and imgid2 = INTERSECTION(13,14);";
//        String sql2 = "select * from img where img.id IN CHILD(11) and img.id not in CHILD(09)";
//
//        String sql4 = "select container.conid,image.imageid "
//                + "from container,image "
//                + "where image.imageid in "
//                + "CHILD(INTERSECTION(12,22)) "
//                + "and image.imageid=container.imageid";
//        String sql3 = "TAG(test," + sql4 + ")";
//        Connection connection = DBConnector.connectPostgres();
//        parseExecuteConSQL(connection, sql3);
//        connection.close();
//    }

    public static void parseExecuteConSQL(Connection connection, String input) {
        if (input.contains("INTERSECTION(")) {
            input = parseIntersect(input);
            System.out.println(input);
        }
        if (input.contains("CHILD(")) {
            input = parseChild(connection, input);
            System.out.println(input);
        }
        // parse and execute conSQL starting with TAG function
        if (input.startsWith("TAG(")) {
            parseExecuteTag(connection, input);
        } // execute regular SQL (after parsed from ConSQL)
        else {
            executeSQL(connection, input);
        }
    }

    public static ResultSet executeSQL(Connection connection, String sql) {
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void parseExecuteTag(Connection connection, String input) {
        Pattern tagp = Pattern.compile(pattern_tag);
        Matcher matcher = tagp.matcher(input);
        if (matcher.find()) {
            String sql = matcher.group(4);
            sql = sql.substring(0, sql.length() - 1);
            System.err.println(matcher.group(2));
            try {
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(sql);
                List<String> ids = new ArrayList<String>();
                while (rs.next()) {
                    ids.add(rs.getString(1));
                }
                System.err.println(matcher.group(1));
                tag((String[]) ids.toArray(new String[ids.size()]), matcher.group(4));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static String parseIntersect(String input) {
        Pattern interp = Pattern.compile(pattern_inter);
        Matcher matcher = interp.matcher(input);
        String result = input;
        String id1, id2 = null;

        while (matcher.find()) {
            // System.out.println(matcher.group(0));
            id1 = matcher.group(2);
            id2 = matcher.group(4);
            result = result.replace(matcher.group(), intersect(id1, id2));
        }
        return result;
    }

    public static String parseChild(Connection conn, String input) {
        Pattern childp = Pattern.compile(pattern_child);
        Matcher matcher = childp.matcher(input);
        String result = input;

        int count = 0;
        while (matcher.find()) {
            
            try {
                count++;
                String id = matcher.group(3);
                
                String childIds[] = APIs.getChildImgList(id, conn).toArray(new String[0]);
                
                Statement statement = conn.createStatement();
                // create temporary table to store ids of the child images
                String tempTableSQL = "create temp table tempimglst" + count + "(ids varchar(64))";
                statement.execute(tempTableSQL);

                // insert data into the temp table
                for (int i = 0; i < childIds.length; i++) {
                    String insertSQL = "insert into tempimglst" + count + " values('" + childIds[i] + "')";
                    System.err.println(insertSQL);
                    statement.execute(insertSQL);
                }
                result = result.replace(matcher.group(), "(select * from tempimglst" + count + ")");
                System.err.println(matcher.group());
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        return result;
    }

    // Fake API
    static void tag(String[] ids, String label) {
        for (int i = 0; i < ids.length; i++) {
            System.out.println(ids[i] + ": " + label);
        }
    }

    static String intersect(String id1, String id2) {
        return id1;
    }

    static String[] child(String id) {
        int i = Integer.parseInt(id);
        String result[] = {id, (i + 1) + "", (i + 2) + ""};
        return result;
    }
}
