/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.api;

import java.sql.*;
import com.txsing.conhub.ult.*;

/**
 *
 * @author txsing
 */
public class APIs {

    public static String getParentalImageID(String fullImageID,
            Connection conn) {
        String sql = "WITH RECURSIVE parents(id) AS("
                + " SELECT parent FROM layers WHERE layerid = '" + fullImageID + "'"
                + " UNION"
                + " SELECT layers.parent FROM parents, layers WHERE layers.layerid = parents.id"
                + " AND parents.id NOT IN (SELECT imageid FROM images)"
                + ") SELECT imageid FROM parents, images WHERE images.imageid = parents.id";
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

    public static String getParentsList(String fullImageID, Connection conn) {
        String sql = "WITH RECURSIVE parents(id) AS("
                + " SELECT parent FROM layers WHERE layerid = '" + fullImageID + "'"
                + " UNION"
                + " SELECT layers.parent FROM parents, layers WHERE layers.layerid = parents.id"
                + ") SELECT imageid FROM parents, images WHERE images.imageid = parents.id";
    }
}
