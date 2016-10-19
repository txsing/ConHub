/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.app;

import com.txsing.conhub.api.*;
import java.sql.Connection;
import com.txsing.conhub.ult.*;
import java.util.List;
import javax.swing.JFrame;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

/**
 *
 * @author txsing
 */
public class ConViz extends JFrame {

    public ConViz(String imgID1, String imgID2) {
        super("ConViz");
        try {
            Connection conn = DBConnector.connectPostgres();
            String rootID = APIs.getImageIntersection(imgID1, imgID2, conn);
            //System.err.println("LOG(DEBUG): rootID: " + rootID);

            mxGraph graph = new mxGraph();
            Object dummyNode = graph.getDefaultParent();

            graph.getModel().beginUpdate();
            String imgName = APIs.getImgNameByID(rootID, conn);
            Object rootNode = graph.insertVertex(dummyNode, rootID,
                    imgName + "\n(" + rootID.substring(0, 12) + ")",
                    100, 20, getNodeLength(imgName), 33);
            graph.getModel().endUpdate();

            Object parentNode = rootNode;
            Object curNode = null;
            List<String> parentLst1 = APIs.getParentalImageIDsList(imgID1, conn);
            Boolean flag = true;
            int count = 0;
            for (int i = parentLst1.size() - 1; i >= 0; i--, count++) {
                String curid = parentLst1.get(i);
                if (flag) {
                    if (curid.equals(rootID)) {
                        flag = false;
                    }
                    continue;
                }
                graph.getModel().beginUpdate();
                imgName = APIs.getImgNameByID(curid, conn);
                curNode = graph.insertVertex(dummyNode, curid,
                        imgName + "\n(" + curid.substring(0, 12) + ")",
                        20, 20 + 60 * count, getNodeLength(imgName), 36);
                graph.insertEdge(dummyNode, null, "", parentNode, curNode);
                graph.getModel().endUpdate();

                parentNode = curNode;
            }
            //add the leaf node.
            graph.getModel().beginUpdate();
            imgName = APIs.getImgNameByID(imgID1, conn);
            curNode = graph.insertVertex(dummyNode, imgID1,
                    imgName + "\n(" + imgID1 + ")", 20, 20 + 60 * count,
                    getNodeLength(imgName), 33,"ROUNDED;strokeColor=black;");
            graph.insertEdge(dummyNode, null, "", parentNode, curNode);
            graph.getModel().endUpdate();

            //rest flag & and count, start to draw another list.
            flag = true;
            count = 0;
            parentNode = rootNode;
            List<String> parentLst2 = APIs.getParentalImageIDsList(imgID2, conn);
            //System.err.println("LOG(DEBUG): List2 Length: " + parentLst2.size());
            for (int i = parentLst2.size() - 1; i >= 0; i--, count++) {
                String curid = parentLst2.get(i);
                if (flag) {
                    if (curid.equals(rootID)) {
                        flag = false;
                    }
                    continue;
                }
                graph.getModel().beginUpdate();
                imgName = APIs.getImgNameByID(curid, conn);
                curNode = graph.insertVertex(dummyNode, curid,
                        imgName + "\n(" + curid.substring(0, 12) + ")",
                        180, 20 + 60 * count, getNodeLength(imgName), 36);
                graph.insertEdge(dummyNode, null, "", parentNode, curNode);
                graph.getModel().endUpdate();
                parentNode = curNode;
            }
            //add leaf node
            graph.getModel().beginUpdate();
            imgName = APIs.getImgNameByID(imgID2, conn);
            curNode = graph.insertVertex(dummyNode, imgID2,
                    imgName + "\n(" + imgID2 + ")", 180, 20 + 60 * count,
                    getNodeLength(imgName), 33, "ROUNDED;strokeColor=black;");
            graph.insertEdge(dummyNode, null, "", parentNode, curNode);
            graph.getModel().endUpdate();

            mxGraphComponent graphComponent = new mxGraphComponent(graph);
            getContentPane().add(graphComponent);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    
    private int getNodeLength(String name){
        int len = name.length() > 12 ? name.length() : 12;
        return len * 8;
    }
}
