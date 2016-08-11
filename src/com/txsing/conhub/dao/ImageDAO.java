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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author txsing
 */
public class ImageDAO {

    public static boolean deleteImageFromDB(String imageID, Connection conn) {
        try {
            String sql = "DELETE FROM LAYERS WHERE" + "layerid = '"
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

    public static boolean syncNewImageIntoDB(String shortImageID,
            String regName, Connection conn) {
        try {
            /* ###### Insert Layers ###### */
            LayerDAO.insertLayersIntoDB(LayerDAO.getLayerIDList(shortImageID)
                    , conn);
            
            /* ###### Insert New Image #### */
            Logger logger = Logger.getLogger("logFile");
            Image newImage = new Image(JsonDAO.getImageAndConJSONInfo(shortImageID));
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
            logger.log(Level.INFO, "SYNC IMG: docker insert {0}", shortImageID);
            
            /* ###### Insert Corresponding REPO ###### */
            Synchro synchro = Synchro.getInstance();
            String repoID;
            String repoString = regName + ":" + newImage.getRepo(); //reg:repo

            List<List<String>> repoDBLst = synchro.getRepoDBLst();
            int index = repoDBLst.get(0).indexOf(repoString);

            if (index == -1) {  //new repo, new tag
                repoID = RepoTagDAO.insertNewRepoIntoDB(conn, newImage.getRepo()
                        , regName);
                synchro.repoDBLstAdd(repoString, repoID);
            } else {  //existing repo, new tag
                repoID = repoDBLst.get(1).get(index);
            }
            
            RepoTagDAO.insertNewTagIntoDB(conn, newImage.getTag()
                    , newImage.getImageID(), repoID);                        
            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
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
            System.err.println(e.getMessage());
        }
        return imageDBLst;
    }

    public static List<String> getImageLstFromDocker() {
        List<String> imageDKLst = new ArrayList<>();
        List<String> imageDKLstAftProcessing = new ArrayList<>();
        String[] cmdParaArray = {"docker", "images", "-q", "--no-trunc"};
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CmdExecutor.executeNonInteractiveDockerCMD(cmdParaArray, baos);
            String imageidLst = baos.toString();
            baos.close();
            if (!imageidLst.equals("")) {
                imageDKLst = Arrays.asList(imageidLst.split("\n"));
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        for(String imgid : imageDKLst){
            imageDKLstAftProcessing.add(imgid.substring(7));
        }
        
        return imageDKLstAftProcessing;
    }

}
