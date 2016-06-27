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
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
        syncRepo(Constants.CONHUB_DEFAULT_REGISTRY);
    }

    private static void syncRepo(String regName) {
        try {
            Logger logger = Logger.getLogger("com.txsing.conhub.mgprocor");
            Connection conn = DBConnector.connectPostgres();

            JSONObject repoJSONObject = getRepoJSONInfo();

            List<List<String>> repoDBLst = getRepoListFromDB(regName, conn);

            for (Object repokey : repoJSONObject.keySet()) {
                String repoName = (String) repokey;
                System.err.println(regName + ":" + repoName);
                //e,g,. Ubuntu:14.03, here Ubuntu is the repo name while 14.03 is tag.
                //First check whether Ubuntu Repo is already stored in DB or not,
                //if ture, then check whether tag-"14.03" exists or not. 
                int index = repoDBLst.get(0).indexOf(regName + ":" + repoName);
                if (index != -1) {
                    String repoID = repoDBLst.get(1).get(index);
                    List<String> tagDBLst = getImageTagListFromDB(repoID, conn);

                    JSONObject tagJSONObject = (JSONObject) repoJSONObject
                            .get(repoName);

                    for (Object tagkey : tagJSONObject.keySet()) {
                        String tag = (String) tagkey;
                        String tagAfterProcess
                                = tag.substring(tag.indexOf(":") + 1);

                        if (!tagDBLst.contains(tagAfterProcess)) { //14.03 not existed
                            System.err.println("Insert Tag: " + tagAfterProcess);

                            String imageID = tagJSONObject.get(tag).toString();
                            imageID = imageID.substring(imageID.indexOf("sha") + 7);

                            insertNewTagIntoDB(conn, tagAfterProcess,
                                    imageID, repoName);
                        }
                    }
                } else {
                    insertNewRepoIntoDB(conn, repoName, regName);
                    System.err.println("Insert Repo: " + regName + ":" + repoName);
                    JSONObject tagJSONObject = (JSONObject) repoJSONObject
                            .get(repoName);
                    for (Object tagkey : tagJSONObject.keySet()) {
                        String tag = (String) tagkey;
                        String tagAfterProcess
                                = tag.substring(tag.indexOf(":") + 1);

                        System.err.println("Insert Tag: " + tagAfterProcess);

                        String imageID = tagJSONObject.get(tag).toString();
                        imageID = imageID.substring(imageID.indexOf("sha") + 7);

                        insertNewTagIntoDB(conn, tagAfterProcess,
                                imageID, repoName);

                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//####################################REPO DB DAO#############################\\
    private static List<List<String>> getRepoListFromDB(String registryName,
            Connection conn) {
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

    private static boolean insertNewRepoIntoDB(Connection conn,
            String repoName, String regName) {
        String sql = null;
        try {
            SimpleDateFormat formater = new SimpleDateFormat("YYMMddHHmmssS");
            String repoID = formater.format(Calendar.getInstance().getTime());
            repoID = "RP" + repoID;

            sql = "INSERT INTO repositories VALUES('"
                    + repoID + "', '" + repoName + "', '" + regName + "')";

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

//#############################Tag DB DAO#####################################\\
    private static List<String> getImageTagListFromDB(String repoid,
            Connection conn) {
        String sql = "SELECT tag FROM tags WHERE repoid = '"
                + repoid + "'";
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

    private static boolean insertNewTagIntoDB(Connection conn, String tag,
            String imageID, String repoID) {
        String sql = "INSERT INTO tags VALUES('"
                + tag + "', '" + imageID + "', '" + repoID + "')";
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

//################################Image DB DAO################################\\
    private static boolean insertNewImageIntoDB(String imageID,
            Connection conn) {
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
            stmt.close();

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
            stmt.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

//#########################CONTAINER DB DAO###################################\\
    private static boolean insertNewContainerIntoDB(String containerID, Connection conn) {
        try {
            Container newContainer = new Container(
                    getImageAndConJSONInfo(containerID));

            String sql = "INSERT INTO CONTAINERS VALUES("
                    + "'" + newContainer.getContainerID() + "', "
                    + "'" + newContainer.getImageID() + "', "
                    + "'" + newContainer.getStartCommand() + "', "
                    + "'" + newContainer.getCreateDate() + "', "
                    //                  + "'" + newContainer.getStatus() + "', "
                    + "'" + newContainer.getPorts() + "', "
                    + "'" + newContainer.getName() + "', "
                    + "'" + newContainer.getBuilderID() + "'"
                    + ")";
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

    private static boolean deleteContainerFromDB(String imageID,
            Connection conn) {
        try {
            String sql = "DELETE FROM CONTAINERS WHERE "
                    + "conid = '" + imageID + "'";
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

//#########################JSON GETTER################################\\
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
            imageInfoJsonString = imageInfoJsonString.
                    substring(1, imageInfoJsonString.lastIndexOf(']'));
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

        String[] cmdParaArray
                = {"/bin/bash", "-c", "echo scse | sudo -S "
                    + "cat /var/lib/docker/image/aufs/repositories.json"};
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CmdExecutor.executeNonInteractiveDockerCMD(cmdParaArray, baos);
            String imageInfoJsonString = baos.toString();

            //remove the firstCotd pair of bracket, [ {xxxxx} ]
            imageInfoJsonString = imageInfoJsonString.
                    substring(imageInfoJsonString.indexOf("{"));

            //System.err.println(imageInfoJsonString);
            baos.close();
            jsonObject = (JSONObject) new JSONParser()
                    .parse(imageInfoJsonString);
            jsonObject = (JSONObject) jsonObject.get("Repositories");
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
