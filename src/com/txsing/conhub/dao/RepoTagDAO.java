/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author txsing
 */
public class RepoTagDAO {

    public static boolean insertNewRepoIntoDB(Connection conn, String repoName
            , String regName) {
        String sql = null;
        try {
            SimpleDateFormat formater = new SimpleDateFormat("YYMMddHHmmssS");
            String repoID = formater.format(Calendar.getInstance().getTime());
            repoID = "RP" + repoID;
            sql = "INSERT INTO repositories VALUES('" + repoID + "', '" 
                    + repoName + "', '" + regName + "')";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            return true;
        } catch (Exception e) {
            System.err.println("INSERT REPO: " + sql);
            e.printStackTrace();
        }
        return false;
    }

    public static List<String> getImageTagListFromDB(String repoid
            , Connection conn) {
        String sql = "SELECT tag FROM tags WHERE repoid = '" + repoid + "'";
        try {
            List<String> resultLst = new ArrayList<>();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                resultLst.add(rs.getString(1));
            }
            stmt.close();
            rs.close();
            return resultLst;
        } catch (Exception e) {
            System.err.println("Get Tag SQL: " + sql);
            e.printStackTrace();
            return null;
        }
    }

    public static List<List<String>> getRepoListFromDB(String registryName
            , Connection conn) {
        String getRepoSQL = getRepoSQL = "SELECT * FROM repositories WHERE" 
                + " regname = '" + registryName + "'";
        try {
            Statement stmt = conn.createStatement();
            ResultSet repoRs = stmt.executeQuery(getRepoSQL);
            List<String> repoLst = new ArrayList<>();
            List<String> repoIDLst = new ArrayList<>();
            while (repoRs.next()) {
                repoLst.add(repoRs.getString(3) + ":" + repoRs.getString(2));
                repoIDLst.add(repoRs.getString(1));
            }
            List<List<String>> result = new ArrayList<>();
            result.add(repoLst);
            result.add(repoIDLst);
            stmt.close();
            repoRs.close();
            return result;
        } catch (Exception e) {
            System.err.println("Get Repo SQL: " + getRepoSQL);
            e.printStackTrace();
            return null;
        }
    }

    public static boolean insertNewTagIntoDB(Connection conn, String tag
            , String imageID, String repoID) {
        String sql = "INSERT INTO tags VALUES('" + tag + "', '" + imageID 
                + "', '" + repoID + "')";
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            return true;
        } catch (Exception e) {
            System.err.println("INSERT TAG: " + sql);
            e.printStackTrace();
        }
        return false;
    }
    
}
