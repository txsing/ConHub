/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.synchro;

/**
 *
 * @author vortex
 */
public class GenereicSQLDataDAO {
    public String[] headers;
    public String[][] data;
    public int noOfColumns;
    public int noOfRows;
    
    public GenereicSQLDataDAO(int noOfRows, int noOfColumns){
        this.noOfColumns=noOfColumns;
        this.noOfRows=noOfRows;
        headers=new String[noOfColumns];
        data=new String[noOfRows][noOfColumns];
    }
    
    public void print(int whereToOutput){
//        for(int i=0;i<noOfColumns;i++){
//            new WriteToOutput(whereToOutput).print(headers[i]+"    ");
//            new logging.LogError().log(headers[i]+"    ");
//        }
        for(int i=0;i<noOfRows;i++){
            for(int j=0;j<noOfColumns;j++){
                //new WriteToOutput(whereToOutput).print(headers[j]+"    "+data[i][j]+"    ");
//                new logging.LogError().log(data[i][j]+"    ");
            }
            //new WriteToOutput(whereToOutput).print("");
//            new logging.LogError().log("\n");
        }
    }

    public String[] getHeaders() {
        return headers;
    }

    public void setHeaders(String[] headers) {
        this.headers = headers;
    }

    public String[][] getData() {
        return data;
    }

    public void setData(String[][] data) {
        this.data = data;
    }
    
}
