//Tian Xing, txsing@gmail.com, 2016-0818
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
    public static void parseExecuteCQL(Connection connection, String input) {
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
        Pattern tagp = Pattern.compile(Constants.API_TAG);
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
                
                APIs.tag(ids.toArray(new String[ids.size()]));
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

    }

    public static String parseIntersect(String input) {
        Pattern interp = Pattern.compile(Constants.PATTERN_INTERSECT);
        Matcher matcher = interp.matcher(input);
        String result = input;
        String id1, id2 = null;
        try {
            while (matcher.find()) {
                // System.out.println(matcher.group(0));
                id1 = matcher.group(3);
                id2 = matcher.group(5);
                result = result.replace(matcher.group(), "'"+APIs.getIntersection(id1, id2))+"'";
            }
        } catch (Exception e) {
                System.err.println(e.getMessage());
        }
        return result;
    }

    public static String parseChild(Connection conn, String input) {
        Pattern childp = Pattern.compile(Constants.PATTERN_CHILD);
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
                    statement.execute(insertSQL);
                }
                result = result.replace(matcher.group(), "(select * from tempimglst" + count + ")");
                statement.close();
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        return result;
    }
}