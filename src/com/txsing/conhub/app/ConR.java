/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.app;

/**
 *
 * @author txsing
 */
import com.txsing.conhub.api.*;
import com.txsing.conhub.ult.DBConnector;
import java.sql.Connection;

/**
 *
 * @author txsing
 */
public class ConR {

    String badImgID;
    String badImgName;
    String badImgAuthor;
    String safeImgID;
    String safeImgName;
    String conID;
    String conName;

    public ConR(String conID) {
        try {
            System.out.println("\n");
            init(conID);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

    }

    void init(String conID) throws Exception {
        Connection conn = DBConnector.connectPostgres();
        String[] conInfo = APIs.getConInfo(conID, conn);

        this.conID = conInfo[0];
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%15s", "BUGGY CONTAINER"));
        sb.append(String.format(": %-13s", this.conID.substring(0, 12)));
        //sb.append("\n");

        this.conName = conInfo[5];
        //sb.append(String.format("%15s", " "));
        sb.append(String.format("%-15s", "(" + this.conName + ")"));
        sb.append("\n");

        this.badImgID = conInfo[1];
        sb.append(String.format("%15s", "BUGGY IMAGE"));
        sb.append(String.format(": %-13s", this.badImgID.substring(0, 12)));
        //sb.append("\n");

        this.badImgName = APIs.getImgNameByID(badImgID, conn);
        //sb.append(String.format("%15s", " "));
        sb.append(String.format("%-15s", "(" + this.badImgName + ")"));
        sb.append("\n");

        this.badImgAuthor = APIs.getImgInfo(badImgID, conn)[3];
        sb.append(String.format("%15s", "DEVELOPER"));
        sb.append(String.format(": %-13s", this.badImgAuthor));
        sb.append("\n");

        this.safeImgID = APIs.getParentalImageID(badImgID, conn);
        if (safeImgID == null) {
            this.safeImgID = "N/A";
            this.safeImgName = "N/A";
        }else{
            this.safeImgID = safeImgID.substring(0,12);
            this.safeImgName = APIs.getImgNameByID(safeImgID, conn);
        }

        sb.append(String.format("%15s", "LAST SAFE IMAGE"));
        sb.append(String.format(": %-13s", this.safeImgID));
        //sb.append("\n");

        //sb.append(String.format("%15s", " "));
        sb.append(String.format("%-15s", "("+this.safeImgName+")"));
        sb.append("\n");
        
        System.out.println(sb.toString());

    }
}
