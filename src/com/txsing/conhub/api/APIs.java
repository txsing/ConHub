/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.api;

import com.txsing.conhub.exceptions.IDNotFoundException;
import java.sql.*;
import com.txsing.conhub.exceptions.*;
import com.txsing.conhub.ult.*;
import com.txsing.conhub.dao.*;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author txsing
 */
public class APIs {

    public static String getParentalImageID(String imageID, Connection conn)
            throws IDNotFoundException, SQLException, IOException {

        String parentalImgID = null;

        String fullImageID = Helper.getFullID(imageID, ImageDAO.getImageLstFromDocker());

        String sql = "WITH RECURSIVE parents(id, visited) AS("
                + " SELECT parent, ARRAY[]::varchar[] FROM layers WHERE layerid = '" + fullImageID + "'"
                + " UNION"
                + " SELECT layers.parent, (visited || parents.id) FROM parents, layers WHERE layers.layerid = parents.id"
                + " AND parents.id NOT IN (SELECT imageid FROM images)"
                + " AND NOT parents.id = ANY(visited))"
                + " SELECT imageid FROM parents, images WHERE images.imageid = parents.id;";

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                parentalImgID = rs.getString(1);
            }
        } catch (SQLException e) {
            System.err.println("LOG(DEBUG): Possible problematic SQL(gPaImg): \n    " + sql);
            throw e;
        }
        return parentalImgID;
    }

    public static List<String> getParentalImageIDsList(String imageID, Connection conn)
            throws IDNotFoundException, SQLException, IOException {

        String fullImageID = Helper.getFullID(imageID, ImageDAO.getImageLstFromDocker());
        List<String> parentIDLst = new ArrayList<>();

        String sql = "WITH RECURSIVE parents(id, visited) AS("
                + " SELECT parent, ARRAY[]::varchar[] FROM layers WHERE layerid = '" + fullImageID + "'"
                + " UNION"
                + " SELECT layers.parent, (visited || parents.id) FROM parents, layers WHERE layers.layerid = parents.id"
                + " AND NOT parents.id = ANY(visited))"
                + " SELECT imageid FROM parents, images WHERE images.imageid = parents.id;";

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String parentalImgID = rs.getString(1);
                parentIDLst.add(parentalImgID);
            }
        } catch (SQLException e) {
            System.err.println("LOG(DEBUG): Possible problematic SQL(gPImgL): \n    " + sql);
            throw e;
        }

        return parentIDLst;
    }

    public static List<String> getChildImgList(String imageID, Connection conn) 
            throws IDNotFoundException, SQLException, IOException {
        List<String> childsIDLst = new ArrayList<>();
        String fullImageID = Helper.getFullID(imageID, ImageDAO.getImageLstFromDocker());
        
        try {
            String sql = "WITH RECURSIVE childs(id, visited) AS("
                    + " SELECT layerid, ARRAY[]::varchar[] FROM layers WHERE parent = '" + fullImageID + "'"
                    + " UNION"
                    + " SELECT layers.layerid, (visited || childs.id) FROM childs, layers WHERE layers.parent = childs.id"
                    + " AND NOT childs.id = ANY(visited))"
                    + " SELECT imageid FROM childs, images WHERE images.imageid = childs.id;";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String parentalImgID = rs.getString(1);
                childsIDLst.add(parentalImgID);
            }
        } catch (SQLException e){
            System.err.println(e.getMessage());
        }
        return childsIDLst;
    }

    public static String getLayerIntersection(String imageID1, String imageID2)
            throws IDNotFoundException, IOException {
        String fullid1 = Helper.getFullID(imageID1, ImageDAO.getImageLstFromDocker());
        String fullid2 = Helper.getFullID(imageID2, ImageDAO.getImageLstFromDocker());

        List<String> layersLst1 = LayerDAO.getLayerIDList(fullid1);
        List<String> layersLst2 = LayerDAO.getLayerIDList(fullid2);

        for (String layerid1 : layersLst1) {
            if (layersLst2.contains(layerid1)) {
                return layerid1;
            }
        }
        return null;
    }

    public static String getImageIntersection(String imageID1, String imageID2, Connection conn)
            throws IDNotFoundException, SQLException, IOException {
        List<String> imageLst1 = APIs.getParentalImageIDsList(imageID1, conn);
        List<String> imageLst2 = APIs.getParentalImageIDsList(imageID2, conn);

        for (String id : imageLst1) {
            if (imageLst2.contains(id)) {
                return id;
            }
        }
        return null;
    }

    public static boolean tag(String[] idList, String label) {
        return false;
    }
}
