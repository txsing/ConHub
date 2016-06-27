/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.dao;

import com.txsing.conhub.mgprocor.CmdExecutor;
import com.txsing.conhub.object.Container;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author txsing
 */
public class ContainerDao {

    public static boolean deleteContainerFromDB(String imageID, Connection conn) {
        try {
            String sql = "DELETE FROM CONTAINERS WHERE " + "conid = '"
                    + imageID + "'";
            //System.err.println("SQL: "+sql);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean insertNewContainerIntoDB(String containerID, Connection conn) {
        try {
            Container newContainer = new Container(JsonDao
                    .getImageAndConJSONInfo(containerID));
            String sql = "INSERT INTO CONTAINERS VALUES(" + "'"
                    + newContainer.getContainerID() + "', " + "'"
                    + newContainer.getImageID() + "', " + "'"
                    + newContainer.getStartCommand() + "', " + "'"
                    + newContainer.getCreateDate() + "', " + "'"
                    + newContainer.getPorts() + "', " + "'"
                    + newContainer.getName() + "', " + "'"
                    + newContainer.getBuilderID() + "'" + ")";
            //System.err.println("generated SQL" + sql);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> getContainerLstFromDB(Connection conn) {
        List<String> conLst = new ArrayList<>();
        String sql = "SELECT conid FROM containers";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                conLst.add(rs.getString(1));
            }
            stmt.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return conLst;
    }

    public static List<String> getContainerLstFromDocker() {
        List<String> conDKLst;
        String[] cmdParaArray = {"docker", "ps", "-a", "-q"};

        conDKLst = CmdExecutor.executeInteractiveDockerCMD(cmdParaArray);
        return conDKLst;
    }
}
