/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.dao;

import com.txsing.conhub.mgprocor.CmdExecutor;
import com.txsing.conhub.object.Image;
import com.txsing.conhub.ult.Constants;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
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

    public static boolean insertNewImageIntoDB(String imageID, 
            Connection conn){
        return insertNewImageIntoDB(imageID, Constants.CONHUB_DEFAULT_REGISTRY,
                conn);
    }
    
    public static boolean insertNewImageIntoDB(String imageID, 
            String regName ,Connection conn) {
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
            
            String repoID = 
                    RepoTagDAO.insertNewRepoIntoDB(conn, newImage.getRepo(), regName);
            RepoTagDAO.insertNewTagIntoDB(conn, newImage.getTag()
                    , imageID, repoID);
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
    
    public static List<String> getImageLstFromDocker(){
        List<String> imageDKLst;
        String[] cmdParaArray = {"docker", "images", "-q"};
        
        imageDKLst = CmdExecutor.executeInteractiveDockerCMD(cmdParaArray);
        return imageDKLst;
    }

}
