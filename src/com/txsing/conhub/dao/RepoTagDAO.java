/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONObject;

/**
 *
 * @author txsing
 */
public class RepoTagDAO {

    public static Map<String, List> convertJsonToRepoMap(JSONObject repoJSONObject, String regName) {
        Map<String, List> repoMap = new HashMap<>();
        for (Object repokey : repoJSONObject.keySet()) {
            List<String> taglist = new ArrayList<>();
            String repoName = (String) repokey;
            String repoFullString = regName + ":" + repoName;

            JSONObject tagJSONObject = (JSONObject) repoJSONObject
                    .get(repoName);
            for (Object tagkey : tagJSONObject.keySet()) {
                String tag = (String) tagkey;
                if (!tag.contains("@")) {
                    String tagAfterProcess
                            = tag.substring(tag.indexOf(":") + 1);
                    String imageID = tagJSONObject.get(tag).toString();
                    imageID = imageID.substring(imageID.indexOf("sha") + 7);
                    taglist.add(tagAfterProcess.concat(":")
                            .concat(imageID));
                }

            }
            repoMap.put(repoFullString, taglist);
        }
        return repoMap;
    }

    public static List<String> getImageTagListFromDB(String repoid, Connection conn) throws SQLException {
        String sql = "SELECT tag FROM tags WHERE repoid = '" + repoid + "'";
        try {
            List<String> resultLst = new ArrayList<>();
            ResultSet rs;
            try (Statement stmt = conn.createStatement()) {
                rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    resultLst.add(rs.getString(1));
                }
            }
            rs.close();
            return resultLst;
        } catch (SQLException e) {
            System.err.println("LOG(DEBUG): Possible problematic SQL(gTfDB): \n    " + sql);
            //System.err.println(e.getMessage());
            throw e;
        }
    }

    public static List<List<String>> getRepoListFromDB(String registryName,
            Connection conn) throws SQLException {
        String getRepoSQL;
        getRepoSQL
                = "SELECT repoid, reponame, regname".concat(" FROM repositories WHERE")
                + " regname = '" + registryName + "'";
        try {
            ResultSet repoRs;
            List<List<String>> result;
            try (Statement stmt = conn.createStatement()) {
                repoRs = stmt.executeQuery(getRepoSQL);
                List<String> repoLst = new ArrayList<>();
                List<String> repoIDLst = new ArrayList<>();
                while (repoRs.next()) {
                    //regname:reponame
                    repoLst.add(repoRs.getString(3) + ":" + repoRs.getString(2));
                    //repoid
                    repoIDLst.add(repoRs.getString(1));
                }
                result = new ArrayList<>();
                result.add(repoLst);
                result.add(repoIDLst);
            }
            repoRs.close();
            return result;
        } catch (SQLException e) {
            System.err.println("LOG(DEBUG): Possible problematic SQL(gRPfDB): \n    " + getRepoSQL);
            throw e;
        }
    }

    /**
     * *
     *
     * @param conn
     * @param repoName
     * @param regName
     * @return the id of the inserted repo.
     * @throws java.sql.SQLException
     */
    public static String insertNewRepoIntoDB(Connection conn, String repoName,
            String regName) throws SQLException {
        String sql = null;
        try {
            SimpleDateFormat formater = new SimpleDateFormat("YYMMddHHmmssS");
            String repoID = formater.format(Calendar.getInstance().getTime());
            repoID = "RP" + repoID;
            sql = "INSERT INTO repositories VALUES('" + repoID + "', '"
                    + repoName + "', '" + regName + "')";
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
            }
            return repoID;
        } catch (SQLException e) {
            System.err.println("LOG(DEBUG): Possible problematic SQL(InsRP): \n    " + sql);
            //System.err.println(e.getMessage());
            throw e;
        }
    }

    public static void deleteRepoFromDB(Connection conn, String repoName,
            String regName) throws SQLException {
        String sql = null;
        try {
            sql = "DELETE FROM repositories WHERE regName = '" + regName + "' AND "
                    + "repoName = '" + repoName + "'";
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
            }
        } catch (SQLException e) {
            System.err.println("LOG(DEBUG): Possible problematic SQL(delRP): \n    " + sql);
            throw e;
        }
    }

    public static boolean insertNewTagIntoDB(Connection conn, String tag,
            String imageID, String repoID) throws SQLException {
        String sql = "INSERT INTO tags VALUES('" + tag + "', '" + imageID
                + "', '" + repoID + "')";
        try {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
            }
            return true;
        } catch (SQLException e) {
            System.err.println("LOG(DEBUG): Possible problematic SQL(InsTag): \n    " + sql);
            //System.err.println(e.getMessage());
            throw e;
        }
    }

    public static void deleteTagFromDB(Connection conn, String tag, String repoID) throws SQLException {
        String sql = null;
        try {
            sql = "DELETE FROM tags WHERE repoid = '" + repoID + "' AND "
                    + "tag = '" + tag + "'";
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
            }
        } catch (SQLException e) {
            System.err.println("LOG(DEBUG): Possible problematic SQL(delTag): \n    " + sql);
            throw e;
        }
    }

    public static String getRepoID(Connection conn, String regName, String repoName) throws SQLException {
        String sql = "SELECT repoid FROM repositories WHERE reponame = '"
                + repoName + "' AND regname = '"
                + regName + "'";
        String repoID = null;
        try {
            Statement stmt = conn.createStatement();
            ResultSet repoRs = stmt.executeQuery(sql);
            while (repoRs.next()) {
                repoID = repoRs.getString(1);
            }
        } catch (SQLException e) {
            System.err.println("LOG(DEBUG): Possible problematic SQL(gRPID): \n    " + sql);
            throw e;
        }
        return repoID;
    }

}
