//Tian Xing, txsing@gmail.com, 2016-0818
package com.txsing.conhub.mgprocor;

import com.txsing.conhub.api.APIs;
import com.txsing.conhub.exceptions.IDNotFoundException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.txsing.conhub.ult.*;
import java.io.IOException;

/**
 *
 * @author txsing
 */
public class Parser {

    public static String parseCQL(Connection connection, String input)
            throws IDNotFoundException, SQLException, IOException {
        input = input.toLowerCase();
        if (input.contains("intersection(")) {
            input = parseIntersect(connection, input);
        }

        if (input.contains("child(")) {
            input = parseChild(connection, input);
        }

        // parse and execute conSQL starting with TAG function
        if (input.startsWith("tag(")) {
            return parseExecuteTag(connection, input);
        } else { // execute regular SQL (after parsed from ConSQL)
            return input;
        }
    }

    public static String parseExecuteTag(Connection connection, String input) {
        Pattern tagp = Pattern.compile(Constants.API_TAG);
        Matcher matcher = tagp.matcher(input);
        String label = "";
        if (matcher.find()) {
            String sql = matcher.group(4);
            sql = sql.substring(0, sql.length() - 1);
            label = matcher.group(2);

            try {
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(sql);
                List<String> ids = new ArrayList<>();
                while (rs.next()) {
                    ids.add(rs.getString(1));
                }

                APIs.tag(ids.toArray(new String[ids.size()]), label);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        return "TAG: " + label;

    }

    public static String parseIntersect(Connection conn, String input)
            throws IDNotFoundException, SQLException, IOException {
        Pattern interp = Pattern.compile(Constants.PATTERN_INTERSECT);
        Matcher matcher = interp.matcher(input);
        String result = input;
        String id1, id2 = null;

        while (matcher.find()) {
            //System.out.println("LOG(DEBUG): intersect pattern found");
            id1 = matcher.group(4);
            id2 = matcher.group(8);
            result = result.replace(matcher.group(), "'"
                    + APIs.getImageIntersection(id1, id2, conn) + "'");
        }
        return result;
    }

    public static String parseChild(Connection conn, String input)
            throws IDNotFoundException, SQLException, IOException {
        Pattern childp = Pattern.compile(Constants.PATTERN_CHILD);
        Matcher matcher = childp.matcher(input);
        String result = input;

        int count = 0;
        while (matcher.find()) {
            count++;
            //the input can be "child('abc') or child(abc)"
            String id = matcher.group(3); //abc

            if (id.equals("'")) //'abc'
            {
                id = matcher.group(4);
            }
            //System.out.println(id);

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
        }
        return result;
    }
}
