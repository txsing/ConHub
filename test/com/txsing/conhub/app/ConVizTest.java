/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.app;

import javax.swing.JFrame;

/**
 *
 * @author txsing
 */
public class ConVizTest {
    public static void main(String[] agrs){
        ConViz frame = new ConViz("7dbd", "ef5b");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 600);
        frame.setVisible(true);
    }
    
}
