/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.mgprocor;

import com.txsing.conhub.dao.ContainerDAO;
import com.txsing.conhub.dao.ImageDAO;
import com.txsing.conhub.dao.JsonDAO;
import com.txsing.conhub.dao.RepoTagDAO;
import com.txsing.conhub.ult.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;

/**
 *
 * @author txsing
 */
public class Synchro {

    private final List<List<String>> repoDBLst;
    private static Synchro theOne;
    protected boolean SIGNAL_SYNC_REPO;

    //constructor
    private Synchro(){
        Connection conn = DBConnector.connectPostgres();
        if(conn == null){
            System.err.println("LOG(ERROR): failed to connect database.");
            System.exit(0);
        }
        this.repoDBLst = RepoTagDAO
                .getRepoListFromDB(Constants.CONHUB_DEFAULT_REGISTRY, conn);
        this.SIGNAL_SYNC_REPO = true;
    }

    public void syncAll() {
        syncImage();
        syncContainer();
    }
    
    /**
     * Sync all images in Docker Engine with Database.
     */
    public void syncImage() {
        Connection conn = DBConnector.connectPostgres();
        List<String> imageDBLst = ImageDAO.getImageLstFromDB(conn);
        List<String> imageDKLst = ImageDAO.getImageLstFromDocker();

        List<String> insertLst = new ArrayList<>();

        for (String imageId : imageDKLst) {
            if (!imageDBLst.contains(imageId)
                    && !(insertLst.contains(imageId))) {
                ImageDAO.syncNewImageIntoDB(imageId, conn);
                insertLst.add(imageId);
            }
        }

        for (String imgId : imageDBLst) {
            if (!imageDKLst.contains(imgId)) {
                ImageDAO.deleteImageFromDB(imgId, conn);
                imageDBLst.remove(imgId);
            }
        }
        
        try {
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(Synchro.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    /**
     * Single Image Sync (Given ImageID)
     *
     * @param imageID
     * @param eventKind
     */
    public static void syncImamge(String imageID, String eventKind) {
        Logger logger = Logger.getLogger("com.txsing.conhub.mgprocor");
        try (Connection conn = DBConnector.connectPostgres()) {
            if (eventKind.equals("ENTRY_CREATE")) {
                ImageDAO.syncNewImageIntoDB(imageID, conn);
                logger.log(Level.INFO, "INSERT IMAGE INTO DB: {0}", imageID.substring(0, 12));
            }
            if (eventKind.equals("ENTRY_DELETE")) {
                ImageDAO.deleteImageFromDB(imageID, conn);
                logger.log(Level.INFO, "DELEFROM IMAGE FROM DB: {0}", imageID.substring(0, 12));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    /**
     * Sync all containers in Docker Engine with Database.
     */
    public void syncContainer() {
        Connection conn = DBConnector.connectPostgres();
        List<String> conDBLst = ContainerDAO.getContainerLstFromDB(conn);

        List<String> conDKLst = ContainerDAO.getContainerLstFromDocker();
        //List<String> insertLst = new ArrayList<>();

        for (String conId : conDKLst) {
            if (conDBLst.contains(conId)) {
                ContainerDAO.insertNewContainerIntoDB(conId, conn);
                //insertLst.add(conId);
            }
        }

        for (String conId : conDBLst) {
            if (!conDKLst.contains(conId)) {
                ContainerDAO.deleteContainerFromDB(conId, conn);
                conDBLst.remove(conId);
            }
        }
        
        try {
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(Synchro.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    /**
     * Single Container Sync (Given Container ID)
     *
     * @param containerID
     * @param eventKind
     */
    public static void syncContainer(String containerID, String eventKind) {
        Logger logger = Logger.getLogger("com.txsing.conhub.mgprocor");
        try (Connection conn = DBConnector.connectPostgres()) {
            if (eventKind.equals("ENTRY_CREATE")) {
                ContainerDAO.insertNewContainerIntoDB(containerID, conn);
                logger.log(Level.INFO, "INSERT IMAGE INTO DB: {0}", containerID.substring(0, 12));
            }
            if (eventKind.equals("ENTRY_DELETE")) {
                ContainerDAO.deleteContainerFromDB(containerID, conn);
                logger.log(Level.INFO, "DELEFROM IMAGE FROM DB: {0}", containerID.substring(0, 12));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.log(Level.INFO, "SYNC CON: docker insect {0}", containerID);
    }



    /**
     * sync the whole Repo (default registry dockerhub). 
     * This method will only be called under the case where docker pull a new 
     * image ("new" here means new repo:tag name while the imageid already exists).
     * e,g,. when you already have image "busybox:1.0" stored, then you pull "busybox:1.25",
     * however, busybox:1.0 and busybox:1.25 actually refer to the same image 
     * (same content/id), in this case, syncRepo will be triggered.
     */
    public void syncRepo() {
        syncRepo(Constants.CONHUB_DEFAULT_REGISTRY);
    }

    private void syncRepo(String regName) {
        try {
            Logger logger = Logger.getLogger("com.txsing.conhub.mgprocor");
            Connection conn = DBConnector.connectPostgres();

            JSONObject repoJSONObject = JsonDAO.getRepoJSONInfo();

            for (Object repokey : repoJSONObject.keySet()) {
                String repoName = (String) repokey;
                String repoFullString = regName + ":" + repoName;

                //e,g,. Ubuntu:14.03, here Ubuntu is the repo name while 14.03 is tag.
                //First check whether repo "Ubuntu" is already stored in DB or not,
                //if ture, then check whether tag "14.03" exists or not. 
                int index = repoDBLst.get(0).indexOf(repoFullString);
                if (index != -1) { //repo already exists
                    String repoID = repoDBLst.get(1).get(index);
                    List<String> tagDBLst = RepoTagDAO.getImageTagListFromDB(repoID, conn);

                    JSONObject tagJSONObject = (JSONObject) repoJSONObject
                            .get(repoName);

                    for (Object tagkey : tagJSONObject.keySet()) {
                        String tag = (String) tagkey;
                        if(tag.contains("@"))
                            continue;
                        String tagAfterProcess
                                = tag.substring(tag.indexOf(":") + 1);

                        if (!tagDBLst.contains(tagAfterProcess)) { //14.03 not existed
                            logger.log(Level.INFO, "INSERT TAG > {0}:{1}", 
                                    new Object[]{repoName, tagAfterProcess});

                            String imageID = tagJSONObject.get(tag).toString();
                            imageID = imageID.substring(imageID.indexOf("sha") + 7);

                            RepoTagDAO.insertNewTagIntoDB(conn, tagAfterProcess,
                                    imageID, repoID);
                        }
                    }
                } else { //new repo new tag
                    String repoID = RepoTagDAO.insertNewRepoIntoDB(conn, repoName, regName);
                    repoDBLstAdd(repoFullString, repoID);
                    logger.log(Level.INFO, "INSERT REPO > {0}:{1}", 
                                    new Object[]{regName, repoName});
                    
                    JSONObject tagJSONObject = (JSONObject) repoJSONObject
                            .get(repoName);
                    for (Object tagkey : tagJSONObject.keySet()) {
                        String tag = (String) tagkey;
                        String tagAfterProcess
                                = tag.substring(tag.indexOf(":") + 1);

                       logger.log(Level.INFO, "INSERT TAG > {0}:{1}", 
                                    new Object[]{repoName, tagAfterProcess});

                        String imageID = tagJSONObject.get(tag).toString();
                        imageID = imageID.substring(imageID.indexOf("sha") + 7);

                        RepoTagDAO.insertNewTagIntoDB(conn, tagAfterProcess,
                                imageID, repoID);

                    }
                }
            }
            conn.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

    public void repoDBLstAdd(String regColonRepo, String repoID) {
        this.repoDBLst.get(0).add(regColonRepo);
        this.repoDBLst.get(1).add(repoID);
    }

    public void repoDBLstDelete(String regColonRepo, String repoID) {
        this.repoDBLst.get(0).remove(regColonRepo);
        this.repoDBLst.get(1).remove(repoID);
    }

    public static Synchro getInstance() {
        if (theOne == null) {
            theOne = new Synchro();
        }
        return theOne;
    }

    public List<List<String>> getRepoDBLst() {
        return this.repoDBLst;
    }
}
