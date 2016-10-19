/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.dao;

import com.txsing.conhub.mgprocor.CmdExecutor;
import com.txsing.conhub.ult.Helper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 *
 * @author txsing
 */
public class LayerDAO {

    public static List<String> getLayerIDList(String shortOrLongImageID) throws IOException {
        List<String> layerIDLst = new ArrayList<>();
        List<String> layerIDLstAftProcess = new ArrayList<>();

        String[] cmdParaArray = {"docker", "history", shortOrLongImageID, "-q", "--no-trunc"};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CmdExecutor.executeNonInteractiveShellCMD(cmdParaArray, baos);
        String layerIDLstString = baos.toString();
        baos.close();

        if (!layerIDLstString.equals("")) {
            layerIDLst = Arrays.asList(layerIDLstString.split("\n"));
        }

        for (String layer : layerIDLst) {
            if (!layer.equals("<missing>")) {
                layerIDLstAftProcess.add(layer.substring(7)); //trim "sha256"
            }
        }

        return layerIDLstAftProcess;
    }

    public static void insertLayersIntoDB(List<String> layerIDLst,
            Connection conn) throws SQLException {
        Logger logger = Helper.getConHubLogger();
        String sql = null;
        Boolean flag;
        try {
            Statement stmt = conn.createStatement();
            String rootLayer = layerIDLst.get(layerIDLst.size() - 1);
            sql = "SELECT count(L.layerid) FROM layers L WHERE L.layerid = '" + rootLayer + "'";
            ResultSet rset = stmt.executeQuery(sql);
            flag = false;
            if (rset.next()) {
                if (rset.getInt(1) == 0) {
                    flag = true;
                }
            }
            if (flag) {
                sql = "INSERT INTO Layers VALUES('" + rootLayer + "', null)";
                stmt.executeUpdate(sql);
                
                logger.info("Root layer inserted: "
                        .concat(rootLayer)
                        .concat("\nLayer list length: ")
                        .concat(layerIDLst.size() + ""));
            }
            sql = "INSERT INTO layers(\"layerid\", \"parent\") SELECT '"
                    + layerIDLst.get(layerIDLst.size() - 1) + "', null "
                    + "WHERE NOT EXISTS(SELECT layerid, parent FROM layers WHERE layerid = '"
                    + layerIDLst.get(layerIDLst.size() - 1) + "');";

            stmt.executeUpdate(sql);

            for (int i = layerIDLst.size() - 2; i >= 0; i--) {
                String layerid = layerIDLst.get(i);
                String layerParent = layerIDLst.get(i + 1);
                sql = "SELECT count(L.layerid) FROM layers L WHERE L.layerid = '" + layerid + "'";
                ResultSet rs = stmt.executeQuery(sql);
                flag = false;
                if (rs.next()) {
                    if (rs.getInt(1) == 0) {
                        flag = true;
                    }
                }
//                System.err.println("LOG(DEBUG): Insert: "+layerid.substring(0,4)
//                        +","+layerParent.substring(0, 4)
//                        +","+flag);   
                if (flag) {
                    sql = "INSERT INTO Layers VALUES('" + layerid + "', '"
                            + layerParent + "')";
                    stmt.executeUpdate(sql);
                    if (layerParent == null) {
                        logger.info("Wrong layer insertion: "
                                .concat(layerid)
                                .concat(", ")
                                .concat(layerParent));
                    }

                }
//                sql = "INSERT INTO layers(\"layerid\", \"parent\") SELECT '"
//                        + layerid + "', '" + layerParent + "' "
//                        + "WHERE NOT EXISTS(SELECT layerid, parent FROM layers WHERE layerid = '"
//                        + layerid + "');";
//                stmt.executeUpdate(sql);
            }
        } catch (Exception e) {
            System.err.println("LOG(DEBUG): Possible problematic SQL(InsLay): \n    " + sql);
            throw (e);
        }
    }
}
