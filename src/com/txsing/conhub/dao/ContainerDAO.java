/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.dao;

import com.txsing.conhub.mgprocor.CmdExecutor;
import com.txsing.conhub.object.Container;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author txsing
 */
public class ContainerDAO {

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
            System.err.println(e.getMessage());
            return false;
        }
    }

    public static boolean insertNewContainerIntoDB(String containerID, Connection conn) {
        try {
            Logger logger = Logger.getLogger("com.txsing.conhub.dao");
            Container newContainer = new Container(JsonDAO
                    .getImageAndConJSONInfo(containerID));
            
            String sql = "INSERT INTO CONTAINERS VALUES(" + "'"
                    + newContainer.getContainerID() + "', " + "'"
                    + newContainer.getImageID() + "', " + "'"
                    + newContainer.getStartCommand() + "', " + "'"
                    + newContainer.getCreateDate() + "', " + "'"
                    + newContainer.getPorts() + "', " + "'"
                    + newContainer.getName() + "', " + "'"
                    + newContainer.getBuilderID() + "'" + ")";
            
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            logger.log(Level.INFO, "SYNC IMG: docker insert {0}", containerID);
            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
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
            System.err.println(e.getMessage());
        }
        return conLst;
    }

    public static List<String> getContainerLstFromDocker() {
        List<String> conDKLst = new ArrayList<>();
        String[] cmdParaArray = {"docker", "ps", "-a", "-q"};
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CmdExecutor.executeNonInteractiveDockerCMD(cmdParaArray, baos);
            String conidLst = baos.toString();
            baos.close();
            
            if (!conidLst.equals("")) {
                conDKLst = Arrays.asList(conidLst.split("\n"));
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return conDKLst;
    }
}
