/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.commands;

import java.util.regex.*;
import java.util.List;
import java.util.ArrayList;
import java.sql.*;
import com.txsing.conhub.ult.*;
/**
 *
 * @author txsing
 */
public class Parser {
	static String user = "postgres";
	static String passwd = "scse";
	static String url = "jdbc:postgresql://localhost/test";

	static String pattern_inter = "(INTERSECTION\\()([\\w]+)(,\\s*)([\\w]+)(\\))";
	static String pattern_child = "(CHILD)(\\()(.{2})(\\))";
	static String pattern_tag = "(TAG\\()(\\w+)(,\\s*)(select.*)";

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
		}
		// execute regular SQL (after parsed from ConSQL)
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
			System.err.println(e.getMessage());
		}
		return null;
	}

	public static void parseExecuteTag(Connection connection, String input) {
		Pattern tagp = Pattern.compile(Constants.PATTERN_TAG);
		Matcher matcher = tagp.matcher(input);
		if (matcher.find()) {
			String sql = matcher.group(4);
			sql = sql.substring(0, sql.length()-1);
			System.err.println(matcher.group(2));
			try {
				Statement statement = connection.createStatement();
				ResultSet rs = statement.executeQuery(sql);
				List<String> ids = new ArrayList<>();
				while (rs.next()) {
					ids.add(rs.getString(1));
				}
				System.err.println(matcher.group(1));
				tag((String[]) ids.toArray(new String[ids.size()]), matcher.group(4));
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}

	}

	public static String parseIntersect(String input) {
		Pattern interp = Pattern.compile(Constants.PATTERN_INTERSECT);
		Matcher matcher = interp.matcher(input);
		String result = input;
		String id1, id2;

		while (matcher.find()) {
			id1 = matcher.group(2);
			id2 = matcher.group(4);
			result = result.replace(matcher.group(), intersect(id1, id2));
		}
		return result;
	}

	public static String parseChild(Connection connection, String input) {
		Pattern childp = Pattern.compile(Constants.PATTERN_CHILD);
		Matcher matcher = childp.matcher(input);
		String result = input;

		int count = 0;
		while (matcher.find()) {
			count++;
			String id = matcher.group(3);
			String childIds[] = child(id);

			try {
				// Connection connection = DBConnector.connectPostgres(url,
				// user, passwd);
				Statement statement = connection.createStatement();

				// create temporary table to store ids of the child images
				String tempTableSQL = "create temp table tmpchildlst" + count + "(ids varchar(64))";
				statement.execute(tempTableSQL);

                            // insert data into the temp table
                            for (String childId : childIds) {
                                String insertSQL = "insert into tmpchildlst" + count + " values(+" + childId + ")";
                                statement.execute(insertSQL);
                            }
				result = result.replace(matcher.group(), "(select * from tmpchildlst" + count + ")");
			} catch (SQLException e) {
				System.err.println(e.getMessage());
			}
		}
		return result;
	}

        
	// Fake API
	static void tag(String[] ids, String label) {
            for (String id : ids) {
                System.out.println(id + ": " + label);
            }
	}

	static String intersect(String id1, String id2) {
		return id1;
	}

	static String[] child(String id) {
		int i = Integer.parseInt(id);
		String result[] = { id, (i + 1) + "", (i + 2) + "" };
		return result;
	}
}

