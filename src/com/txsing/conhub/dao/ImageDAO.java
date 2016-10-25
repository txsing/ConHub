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
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.simple.parser.ParseException;

/**
 *
 * @author txsing
 */
public class ImageDAO {

    public static boolean deleteImageFromDB(String imageID, Connection conn) throws SQLException{
        String sql = null;
        try {
            sql = "DELETE FROM LAYERS WHERE" + " layerid = '"
                    + imageID + "'";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            return true;
        } catch (SQLException e) {
            System.err.println("LOG(DEBUG): Possible problematic SQL(DelImg): \n    " + sql);
            //System.err.println(e.getMessage());
            throw e;
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
            Connection conn) 
            throws SQLException, IOException, ParseException {
        return syncNewImageIntoDB(imageID, Constants.CONHUB_DEFAULT_REGISTRY,
                conn);
    }

    public static boolean syncNewImageIntoDB(String imageID,
            String regName, Connection conn) 
            throws SQLException, IOException, ParseException {
        String sql = null;
        Image newImage = null;

        /* ###### Insert Layers ###### */
        List<String> layerList = LayerDAO.getLayerIDList(imageID);
        
        LayerDAO.insertLayersIntoDB(layerList, conn);
     
        /* ###### Insert New Image #### */
        try {
            newImage = new Image(JsonDAO.getImageAndConJSONInfo(imageID));
            sql = "INSERT INTO IMAGES VALUES (" + "'"
                    + newImage.getImageID() + "', " + "'"
                    //+ newImage.getParentImageID() + "', " + "'"
                    + newImage.getDockerFileID() + "', "
                    + newImage.getSize() + ", " + "'"
                    + newImage.getAuthor() + "')";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            System.out.println("LOG(INFO): INSERT IMAGE: " + imageID.substring(0, 12));
        } catch (SQLException e) {
            System.err.println("LOG(DEBUG): Possible problematic SQL(SynImg): \n    " + sql);
            throw e;
        }

        /* ###### Insert Corresponding REPO ###### */
        Synchro synchro = Synchro.getInstance();
        String repoID;
        String repoString = regName + ":" + newImage.getRepo(); //reg:repo

        List<List<String>> repoDBLst = synchro.getRepoDBLst();
        int index = repoDBLst.get(0).indexOf(repoString);

        if (index == -1) {  //new repo, new tag
            repoID = RepoTagDAO.insertNewRepoIntoDB(conn, newImage.getRepo(), regName);
            synchro.repoDBLstAdd(repoString, repoID);
            System.out.print("LOG(INFO): INSERT REPO&TAG: ");
        } else {    //existing repo, new tag
            repoID = repoDBLst.get(1).get(index);
            System.out.print("LOG(INFO): INSERT TAG: ");
        }

        /* ###### Insert Corresponding TAG ###### */
        RepoTagDAO.insertNewTagIntoDB(conn, newImage.getTag(), newImage.getImageID(), repoID);

        System.out.println(newImage.getRepo() + ":" + newImage.getTag());
        return true;
    }

    public static List<String> getImageLstFromDB(Connection conn) throws SQLException {
        List<String> imageDBLst = new ArrayList<>();
        
        String sql = "SELECT imageid FROM images";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            imageDBLst.add(rs.getString(1));
        }
        stmt.close();

        return imageDBLst;
    }

    public static List<String> getImageLstFromDocker() throws IOException {
        List<String> imageDKLst = new ArrayList<>();
        List<String> imageDKLstAftProcessing = new ArrayList<>();
        String[] cmdParaArray = {"docker", "images", "-q", "--no-trunc"};
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CmdExecutor.executeNonInteractiveShellCMD(cmdParaArray, baos);
        String imageidLst = baos.toString();
        baos.close();
        
        if (!imageidLst.equals("")) {
            imageDKLst = Arrays.asList(imageidLst.split("\n"));
        }

        for (String imgid : imageDKLst) {
            String imageidAftProcessing = imgid.substring(7);
            //oringal imageid list can contains duplicate ids (the same image with two different repo:tag)
            if (!imageDKLstAftProcessing.contains(imageidAftProcessing)) {
                imageDKLstAftProcessing.add(imgid.substring(7));
            }
        }

        return imageDKLstAftProcessing;
    }

}
