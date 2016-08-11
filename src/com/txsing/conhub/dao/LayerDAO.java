/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.dao;

import com.txsing.conhub.mgprocor.CmdExecutor;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author txsing
 */
public class LayerDAO {

    public static List<String> getLayerIDList(String shortOrLongImageID) {
        List<String> layerIDLst = new ArrayList<>();
        List<String> layerIDLstAftProcess = new ArrayList<>();

        String[] cmdParaArray = {"docker", "history", shortOrLongImageID, "-q", "--no-trunc"};
        try {
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
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return layerIDLstAftProcess;
    }

    public static boolean insertLayersIntoDB(List<String> layerIDLst,
            Connection conn) {
        try {
            Statement stmt = conn.createStatement();
            String sql = null;
            
            sql = "INSERT INTO layers VALUES ('"
                    + layerIDLst.get(layerIDLst.size()-1) + "', null)";
            stmt.executeUpdate(sql);
            
            for (int i = layerIDLst.size() - 2; i >=0; i--) {
                String layerid = layerIDLst.get(i);
                String layerParent = layerIDLst.get(i + 1);
                sql = "INSERT INTO layers VALUES ('"
                        + layerid + "', '"
                        + layerParent + "')";
                stmt.executeUpdate(sql);
                System.err.println(sql);
            }

            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return false;
    }
}
