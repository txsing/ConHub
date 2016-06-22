/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.ult;

import java.io.*;
import java.util.Scanner;

/**
 *
 * @author txsing
 */
public class OutputStreamGobbler extends Thread {

    OutputStream os;

    public OutputStreamGobbler(OutputStream os) {
        this.os = os;
    }

    @Override
    public void run() {
        try {
            PrintWriter writer
                    = new PrintWriter(new OutputStreamWriter(os));
            Scanner scanner = new Scanner(System.in);

            while (scanner.hasNext()) {
                String subcmd = scanner.nextLine();
                if (subcmd.equals("exit")) {
                    break;
                }
                writer.println(subcmd);
                writer.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
