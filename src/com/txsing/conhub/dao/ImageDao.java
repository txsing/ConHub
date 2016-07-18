/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.dao;

import com.txsing.conhub.mgprocor.Synchro;
import com.txsing.conhub.mgprocor.CmdExecutor;
import com.txsing.conhub.object.Image;
import com.txsing.conhub.ult.Constants;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author txsing
 */
public class ImageDao {

    public static boolean deleteImageFromDB(String imageID, Connection conn) {
        try {
            String sql = "DELETE FROM IMAGES WHERE" + "imageid = '"
                    + imageID + "'";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * *
     * Insert new Image into DB, and insert the corresponding repo into DB at
     * the same time
     *
     * @param imageID
     * @param conn
     * @return
     */
    public static boolean syncNewImageIntoDB(String imageID,
            Connection conn) {
        return syncNewImageIntoDB(imageID, Constants.CONHUB_DEFAULT_REGISTRY,
                conn);
    }

    public static boolean syncNewImageIntoDB(String imageID,
            String regName, Connection conn) {
        try {
            Image newImage = new Image(JsonDao.getImageAndConJSONInfo(imageID));
            String sql = "INSERT INTO IMAGES VALUES (" + "'"
                    + newImage.getImageID() + "', " + "'"
                    + newImage.getParentImageID() + "', " + "'"
                    + newImage.getDockerFileID() + "', "
                    + newImage.getSize() + ", " + "'"
                    + newImage.getAuthor() + "', " + "'"
                    + newImage.getBuilderID() + "'" + ")";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();

            //sync corresponding repo
            Synchro synchro = Synchro.getInstance();
            
            String repoID;
            String repoString = regName + ":" + newImage.getRepo(); //reg:repo
            
            List<List<String>> repoDBLst = synchro.getRepoDBLst();
            int index = repoDBLst.get(0).indexOf(repoString);
            
            if (index == -1) {  //new repo, new tag
                repoID = RepoTagDAO.insertNewRepoIntoDB(conn, newImage.getRepo(), regName);
                synchro.repoDBLstAdd(repoString, repoID);
            }else{  //existing repo, new tag
                repoID = repoDBLst.get(1).get(index);
            }
            
            RepoTagDAO.insertNewTagIntoDB(conn, newImage.getTag(), newImage.getImageID(), repoID);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> getImageLstFromDB(Connection conn) {
        List<String> imageDBLst = new ArrayList<>();
        String sql = "SELECT imageid FROM images";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                imageDBLst.add(rs.getString(1));
            }
            stmt.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageDBLst;
    }

    public static List<String> getImageLstFromDocker() {
        List<String> imageDKLst = new ArrayList<>();
        String[] cmdParaArray = {"docker", "images", "-q"};
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CmdExecutor.executeNonInteractiveDockerCMD(cmdParaArray, baos);
            String imageidLst = baos.toString();
            if(!imageidLst.equals("")){
                imageDKLst = Arrays.asList(imageidLst.split("\n"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageDKLst;
    }

}
