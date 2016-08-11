/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.api;

import java.sql.*;
import com.txsing.conhub.ult.*;
import com.txsing.conhub.dao.*;
import java.util.*;

/**
 *
 * @author txsing
 */
public class APIs {

    public static String getParentalImageID(String imageID,
            Connection conn) {
        String fullImageID = null;
        try {
            fullImageID = Helper.getFullID(imageID, ImageDAO.getImageLstFromDocker());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        System.err.println(fullImageID);
        if(fullImageID == null){
            return null;
        }
        
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
                String parentalImgID = rs.getString(1);
                return parentalImgID;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        
        return null;
    }

    public static List<String> getParentalImageIDsList(String fullImageID, Connection conn) {
        List<String> parentIDLst = new ArrayList<String>();
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
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }        
        
        return parentIDLst;
    }
    
    public static String getIntersection(String imageID1, String imageID2){
        List<String> layersLst1 = LayerDAO.getLayerIDList(imageID1);
        List<String> layersLst2 = LayerDAO.getLayerIDList(imageID2);
        
        for(String layerid1 : layersLst1){
            if(layersLst2.contains(layerid1)){
                return layerid1;
            }
        }
        return null;
    }
    
    
}
