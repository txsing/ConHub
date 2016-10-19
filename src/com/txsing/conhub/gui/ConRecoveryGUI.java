/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.gui;

import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 *
 * @author txsing
 */
public class ConRecoveryGUI extends JFrame {

    GridLayout selectCon;
    JTable contable;
    JButton confirmButton;

    public ConRecoveryGUI(Object[][] runningContainers) throws HeadlessException {
        initComponents(runningContainers);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(400, 300);
        this.setVisible(true);
    }

    void initComponents(Object[][] runningContainers) {
        selectCon = new GridLayout(2, 1);
        this.setLayout(selectCon);

        String titles[] = {"conid", "name", "status"};
        contable = new JTable(runningContainers, titles);
        contable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.add(contable);

        confirmButton = new JButton("recovery");
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (contable.getSelectedRow() != -1) {

                }
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
    }

    class SelectedConInfoGUI extends JFrame {

        JLabel conIDL = new JLabel("Bad Container: ");
        JLabel conImgL = new JLabel("Bad Image: ");
        JLabel emailL = new JLabel("maintainer: ");
        JLabel lastStableImgL = new JLabel("Last Stable Image: ");

        JTextField conIDF;
        JTextField conImgF;
        JTextField emailF;
        JTextField lastStableImgF;

        public SelectedConInfoGUI(String ID, String conName, String badImgID, String badImgName,
                String email, String goodImgID, String goodImgName) throws HeadlessException {
            initComponents(ID, conName, badImgID, badImgName, email,goodImgID,goodImgName);
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setSize(300, 400);
            this.setVisible(true);
        }

        void initComponents(String ID, String conName, String badimgID, String badImgName,
                String email, String goodImgID, String goodImgName) {
            conIDF = new JTextField(ID + "/n" + conName);
            conImgF = new JTextField(badimgID + "/n" + badImgName);
        }
    }
}
