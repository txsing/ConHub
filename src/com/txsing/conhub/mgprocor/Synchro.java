/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.mgprocor;

import com.txsing.conhub.dao.ContainerDao;
import com.txsing.conhub.dao.ImageDao;
import com.txsing.conhub.dao.JsonDao;
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

    private List<List<String>> repoDBLst;

    private static Synchro theOne;
    protected boolean SIGNAL_SYNC_REPO = true;

    //constructor
    private Synchro() {
        Connection conn = DBConnector.connectPostgres();
        this.repoDBLst = RepoTagDAO
                .getRepoListFromDB(Constants.CONHUB_DEFAULT_REGISTRY, conn);
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
        List<String> imageDBLst = ImageDao.getImageLstFromDB(conn);
        List<String> imageDKLst = ImageDao.getImageLstFromDocker();

        List<String> insertLst = new ArrayList<>();

        for (String imageId : imageDKLst) {
            if (!imgConIDContains(imageDBLst, imageId)
                    && !(insertLst.contains(imageId))) {
                ImageDao.syncNewImageIntoDB(imageId, conn);
                insertLst.add(imageId);
            }
        }

        for (String imgId : imageDBLst) {
            if (!imgConIDContains(imageDKLst, imgId)) {
                ImageDao.deleteImageFromDB(imgId, conn);
                imageDBLst.remove(imgId);
            }
        }
        //imageDBLst.addAll(insertLst);
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
                ImageDao.syncNewImageIntoDB(imageID, conn);
                logger.log(Level.INFO, "INSERT IMAGE INTO DB: {0}", imageID.substring(0, 12));
            }
            if (eventKind.equals("ENTRY_DELETE")) {
                ImageDao.deleteImageFromDB(imageID, conn);
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
        List<String> conDBLst = ContainerDao.getContainerLstFromDB(conn);

        List<String> conDKLst = ContainerDao.getContainerLstFromDocker();

        List<String> insertLst = new ArrayList<>();

        for (String conId : conDKLst) {
            if (!imgConIDContains(conDBLst, conId)) {
                ContainerDao.insertNewContainerIntoDB(conId, conn);
                insertLst.add(conId);
            }
        }

        for (String conId : conDBLst) {
            if (!imgConIDContains(conDKLst, conId)) {
                ContainerDao.deleteContainerFromDB(conId, conn);
                conDBLst.remove(conId);
            }
        }
        //conDBLst.addAll(insertLst);
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
                ContainerDao.insertNewContainerIntoDB(containerID, conn);
                logger.log(Level.INFO, "INSERT IMAGE INTO DB: {0}", containerID.substring(0, 12));
            }
            if (eventKind.equals("ENTRY_DELETE")) {
                ContainerDao.deleteContainerFromDB(containerID, conn);
                logger.log(Level.INFO, "DELEFROM IMAGE FROM DB: {0}", containerID.substring(0, 12));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.log(Level.INFO, "SYNC CON: docker insect {0}", containerID);
    }



    /**
     * *
     * sync the whole Repo (default registry dockerhub) only called under the
     * case where docker pull a new image ("new" here in terms of new repo:tag
     * name while the imageid already exists).
     * e,g,. when you already have image "busybox:1.0" stored, then you pull "busybox:1.25".
     * busybox:1.0 and busybox:1.25 actually refer to the same image (same content),
     * so when you docker pull busybox:1.25, syncRepo will be triggered.
     */
    public void syncRepo() {
        syncRepo(Constants.CONHUB_DEFAULT_REGISTRY);
    }

    private void syncRepo(String regName) {
        try {
            Logger logger = Logger.getLogger("com.txsing.conhub.mgprocor");
            Connection conn = DBConnector.connectPostgres();

            JSONObject repoJSONObject = JsonDao.getRepoJSONInfo();

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

    /**
     * *
     * the full length of imageid or conid is 64(long id). However "docker
     * ps/images" will only return the starting part of the id (short id).
     *
     * @param ids
     * @param longOrShortId
     * @return
     */
    private boolean imgConIDContains(List<String> ids, String longOrShortId) {
        for (String id : ids) {
            if (longOrShortId.startsWith(id) || id.startsWith(longOrShortId)) {
                return true;
            }
        }
        return false;
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
