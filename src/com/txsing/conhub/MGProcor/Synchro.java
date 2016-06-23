/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.MGProcor;

import com.txsing.conhub.ult.*;
import com.txsing.conhub.object.*;
import java.sql.*;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
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
        try {
            Logger logger = Logger.getLogger("com.txsing.conhub.mgprocor");
            Connection conn = DBConnector.connectPostgres();

            JSONObject repoJSONObject = getRepoJSONInfo();
            Statement stmt = conn.createStatement();

            //get all repo stored in DB
            String getRepoSQL = "SELECT name FROM repositories WHERE "
                    + "regid = '" + Constants.CONHUB_REGISTRY + "'";
            ResultSet repoRs = stmt.executeQuery(getRepoSQL);
            List<String> repoLst = new ArrayList<>();
            while (repoRs.next()) {
                repoLst.add(repoRs.getString("1"));
            }

            for (Object repokey : repoJSONObject.keySet()) {
                String repoName = (String) repokey;
                if (repoLst.contains(repoName)) {
                   List<String> tagDBLst = getImageTagListFromDB(repoName, conn);
                   
                   JSONObject tagJSONObject = (JSONObject)repoJSONObject.get(repoName);
                   for(Object tagkey : tagJSONObject.keySet()){
                       String tag = (String) tagkey;
                       if(!tagDBLst.contains(tag)){
                           String insertNewTagSQL = "INSERT INTO tags VALUES('"
                                   +Constants.CONHUB_REGISTRY+"', '"
                                   +repoName+"', '" +tag+"', '"
                                   +tagJSONObject.get(tag).toString()+"'";
                           stmt.executeUpdate(insertNewTagSQL);
                       }
                   }
                } else {
                    
                }
            }

        } catch (Exception e) {
        }

    }

    private static List<String> getImageTagListFromDB(String repoName, Connection conn) {
        try {
            List<String> resultLst = new ArrayList<>();
            String sql = "SELECT tag FROM tags WHERE reponame = '"
                    + repoName + "' AND regid = '"
                    + Constants.CONHUB_REGISTRY + "'";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                resultLst.add(rs.getString(1));
            }
            
            return resultLst;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    
    private static boolean insertNewImageIntoDB(String imageID, Connection conn) {
        try {
            Image newImage = new Image(getImageAndConJSONInfo(imageID));

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
            Container newContainer = new Container(getImageAndConJSONInfo(containerID));

            String sql = "INSERT INTO CONTAINERS VALUES("
                    + "'" + newContainer.getContainerID() + "', "
                    + "'" + newContainer.getImageID() + "', "
                    + "'" + newContainer.getStartCommand() + "', "
                    + "'" + newContainer.getCreateDate() + "', "
                    //                    + "'" + newContainer.getStatus() + "', "
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

    /**
     * get the json info of a specified container or image.
     *
     * @param ID
     * @return
     */
    private static JSONObject getImageAndConJSONInfo(String ID) {
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

    private static JSONObject getRepoJSONInfo() {
        JSONObject jsonObject;
        String[] cmdParaArray = {"cat", "/var/lib/docker/image/aufs/repositories.json"};
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
