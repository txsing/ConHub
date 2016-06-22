/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.MGProcor;

import com.txsing.conhub.MGProcor.*;
import com.txsing.conhub.ult.*;
import com.txsing.conhub.object.*;
import java.sql.*;
import java.io.ByteArrayOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author txsing
 */
public class Synchro {

    public static void syncImamge(String imageID, String eventKind) {
        Logger logger = Logger.getLogger("com.txsing.conhub.mgprocor");
        Connection conn = DBConnector.connectPostgres();
        
        if (eventKind.equals("ENTRY_CREATE")) {
            insertNewImageIntoDB(imageID, conn);
            logger.log(Level.INFO, "INSERT IMAGE INTO DB: {0}", imageID.substring(0, 12));
        }
        if (eventKind.equals("ENTRY_DELETE")) {
            deleteImageFromDB(imageID, conn);
            logger.log(Level.INFO, "DELEFROM IMAGE FROM DB: {0}", imageID.substring(0, 12));
        }
    }

    public static void syncContainer(String containerID, String eventKind) {
        Logger logger = Logger.getLogger("com.txsing.conhub.mgprocor");
        Connection conn = DBConnector.connectPostgres();
        
        if (eventKind.equals("ENTRY_CREATE")) {
            insertNewContainerIntoDB(containerID, conn);
            logger.log(Level.INFO, "INSERT IMAGE INTO DB: {0}", containerID.substring(0, 12));
        }
        if (eventKind.equals("ENTRY_DELETE")) {
            deleteContainerFromDB(containerID, conn);
            logger.log(Level.INFO, "DELEFROM IMAGE FROM DB: {0}", containerID.substring(0, 12));
        }
        
        System.out.println("SYNC CON: docker insect " + containerID);
    }

    public static void syncRepo() {

    }

    private static boolean insertNewImageIntoDB(String imageID, Connection conn) {
        try {
            Image newImage = new Image(getJSONInfo(imageID));

            String sql = "INSERT INTO IMAGES VALUES ("
                    + "'" + newImage.getImageID() + "', "
                    + "'" + newImage.getParentImageID() + "', "
                    + "'" + newImage.getDockerFileID() + "', "
                    + newImage.getSize() + ", "
                    + "'" + newImage.getAuthor() + "', "
                    + "'" + newImage.getBuilderID() + "'"
                    + ")";

            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    private static boolean deleteImageFromDB(String imageID, Connection conn) {
        try {
            String sql = "DELETE FROM IMAGES WHERE"
                    + "imageid = '" + imageID + "'";

            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    private static boolean insertNewContainerIntoDB(String containerID, Connection conn) {
        try {
            Container newContainer = new Container(getJSONInfo(containerID));

            String sql = "INSERT INTO CONTAINERS VALUES("
                    + "'" + newContainer.getContainerID() + "', "
                    + "'" + newContainer.getImageID() + "', "
                    + "'" + newContainer.getStartCommand() + "', "
                    + "'" + newContainer.getCreateDate() + "', "
                    + "'" + newContainer.getStatus() + "', "
                    + "'" + newContainer.getPorts() + "', "
                    + "'" + newContainer.getName() + "', "
                    + "'" + newContainer.getBuilderID() + "'"
                    + ")";
            //System.err.println("generated SQL" + sql);
            Statement stmt = conn.createStatement();

            stmt.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean deleteContainerFromDB(String imageID, Connection conn) {
        try {
            String sql = "DELETE FROM CONTAINERS WHERE "
                    + "conid = '" + imageID + "'";
            //System.err.println("SQL: "+sql);
            
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    private static JSONObject getJSONInfo(String ID) {
        JSONObject jsonObject;
        String[] cmdParaArray = {"docker", "inspect", ID};
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CmdExecutor.executeNonInteractiveDockerCMD(cmdParaArray, baos);
            String imageInfoJsonString = baos.toString();

            //remove the firstCotd pair of bracket, [ {xxxxx} ]
            imageInfoJsonString = imageInfoJsonString.substring(1, imageInfoJsonString.lastIndexOf(']'));
            //System.err.println(imageInfoJsonString);
            
            baos.close();
            jsonObject = (JSONObject) new JSONParser()
                    .parse(imageInfoJsonString);
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
