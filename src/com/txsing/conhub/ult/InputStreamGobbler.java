/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.ult;

import java.io.*;

public class InputStreamGobbler extends Thread {

    InputStream is;
    OutputStream os;
    String type;

    public InputStreamGobbler(InputStream is, OutputStream os, String type) {
        this.is = is;
        this.os = os;
        this.type = type;
    }

    @Override
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                os.write((type + line + "\n").getBytes());
                os.flush();
            }
            
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
