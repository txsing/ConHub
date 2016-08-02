/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.dao;

import java.util.List;

/**
 *
 * @author txsing
 */
public class LayerDAOTest {
    public static void main(String[] args){
        List<String> layerLST = LayerDAO.getLayerIDList("f2c");
        for(String layer : layerLST){
            System.out.println(layer);
        }
        //LayerDAO.insertLayersIntoDB(layerLST);
    }
}
