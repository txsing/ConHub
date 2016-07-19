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

    public void syncAll() {
        syncImage();
        syncContainer();
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
     * Sync all images in Docker Engine with Database.
     */
    public void syncImage() {
        Connection conn = DBConnector.connectPostgres();
        List<String> imageDBLst = ImageDao.getImageLstFromDB(conn);
        List<String> imageDKLst = ImageDao.getImageLstFromDocker();

        List<String> insertLst = new ArrayList<>();

        for (String imageId : imageDKLst) {
            if (!imgConIDContains(imageDBLst, imageId)) {
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
     * Single Container Sync (Given Container ID)
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
        System.out.println("SYNC CON: docker insect " + containerID);
    }

    /**
     * Sync all containers in Docker Engine with Database.
     */
    public void syncContainer() {
        Connection conn = DBConnector.connectPostgres();
        List<String> conDBLst =  ContainerDao.getContainerLstFromDB(conn);
        
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

    
    
    /***
     * sync the whole Repo (default registry dockerhub)
     */
    public static void syncRepo() {
        syncRepo(Constants.CONHUB_DEFAULT_REGISTRY);
    }

    private static void syncRepo(String regName) {
        try {
            Logger logger = Logger.getLogger("com.txsing.conhub.mgprocor");
            Connection conn = DBConnector.connectPostgres();

            JSONObject repoJSONObject = JsonDao.getRepoJSONInfo();

            List<List<String>> repoDBLst = RepoTagDAO.getRepoListFromDB(regName, conn);

            for (Object repokey : repoJSONObject.keySet()) {
                String repoName = (String) repokey;
                System.err.println(regName + ":" + repoName);
                //e,g,. Ubuntu:14.03, here Ubuntu is the repo name while 14.03 is tag.
                //First check whether Ubuntu Repo is already stored in DB or not,
                //if ture, then check whether tag-"14.03" exists or not. 
                int index = repoDBLst.get(0).indexOf(regName + ":" + repoName);
                if (index != -1) {
                    String repoID = repoDBLst.get(1).get(index);
                    List<String> tagDBLst = RepoTagDAO.getImageTagListFromDB(repoID, conn);

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

                            RepoTagDAO.insertNewTagIntoDB(conn, tagAfterProcess,
                                    imageID, repoName);
                        }
                    }
                } else {
                    RepoTagDAO.insertNewRepoIntoDB(conn, repoName, regName);
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

                        RepoTagDAO.insertNewTagIntoDB(conn, tagAfterProcess,
                                imageID, repoName);

                    }
                }
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    
    
    
    public static Synchro getInstance(){
        if(theOne == null){
            theOne = new Synchro();
        }
        return theOne;
    }
    
    //constructor
    private Synchro(){
        Connection conn = DBConnector.connectPostgres();
        this.repoDBLst = RepoTagDAO
                .getRepoListFromDB(Constants.CONHUB_DEFAULT_REGISTRY, conn);
    }
    
    public void repoDBLstAdd(String repo, String repoID){
        this.repoDBLst.get(0).add(repo);
        this.repoDBLst.get(1).add(repoID);
    }
    
    public void repoDBLstDelete(String repo, String repoID){
        this.repoDBLst.get(0).remove(repo);
        this.repoDBLst.get(1).remove(repoID);
    }
    
    public List<List<String>> getRepoDBLst(){
        return this.repoDBLst;
    }
    
    
    /***
     * the full length of imageid or conid is 64(long id). However "docker ps/images" will
     * only return the starting part of the id (short id).
     * 
     * @param ids
     * @param longOrShortId
     * @return 
     */
    private boolean imgConIDContains(List<String> ids, String longOrShortId){
        for(String id : ids){
            if(longOrShortId.startsWith(id) || id.startsWith(longOrShortId))
                return true;
        }
        return false;
    }
}
