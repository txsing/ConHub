/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.dao;

import com.txsing.conhub.ult.DBConnector;
import java.util.List;

/**
 *
 * @author txsing
 */
public class LayerDAOTest {
    public static void main(String[] args) throws Exception{
        List<String> layerLST = LayerDAO.getLayerIDList("6689e9");
//        for(String layer : layerLST){
//            System.out.println(layer);
//        }
        System.err.println(layerLST.size());
        LayerDAO.insertLayersIntoDB(layerLST, DBConnector.connectPostgres());
    }
}
