/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.dao;

import com.txsing.conhub.mgprocor.CmdExecutor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        CmdExecutor.executeNonInteractiveDockerCMD(cmdParaArray, baos);
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
        String sql = null;
        try {
            Statement stmt = conn.createStatement();

            sql = "INSERT INTO layers(\"layerid\", \"parent\") SELECT '"
                    + layerIDLst.get(layerIDLst.size() - 1) + "', null "
                    + "WHERE NOT EXISTS(SELECT layerid, parent FROM layers WHERE layerid = '"
                    + layerIDLst.get(layerIDLst.size() - 1) + "');";
            stmt.executeUpdate(sql);
            //System.out.println(sql);

            for (int i = layerIDLst.size() - 2; i >= 0; i--) {
                String layerid = layerIDLst.get(i);
                String layerParent = layerIDLst.get(i + 1);

                sql = "INSERT INTO layers(\"layerid\", \"parent\") SELECT '"
                        + layerid + "', '" + layerParent + "' "
                        + "WHERE NOT EXISTS(SELECT layerid, parent FROM layers WHERE layerid = '"
                        + layerid + "');";
                //System.out.println(sql);
                stmt.executeUpdate(sql);
            }
        } catch (Exception e) {
            System.err.println("LOG(DEBUG): Possible problematic SQL(InsLay): \n    " + sql);
            throw (e);
        }
    }
}
