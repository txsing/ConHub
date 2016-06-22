/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.synchro;

import org.json.simple.JSONObject;

/**
 *
 * @author vortex
 */
public class convolstDAO {
    private String conid;
    private String name;
    private String source;
    private String destination;
    private String driver;
    private String mode;
    private String rw;
    private String propagation;

    public convolstDAO(JSONObject jsob, String conid){
        this.conid = conid;
        this.name = jsob.get("Name")+"";
        this.source = jsob.get("Source")+"";
        this.destination = jsob.get("Destination")+"";
        this.driver = jsob.get("Driver")+"";
        this.mode = jsob.get("Mode")+"";
        this.rw = jsob.get("RW")+"";
        this.propagation = jsob.get("Propagation")+"";
    }
    
    public String getConid() {
        return conid;
    }

    public void setConid(String conid) {
        this.conid = conid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getRw() {
        return rw;
    }

    public void setRw(String rw) {
        this.rw = rw;
    }

    public String getPropagation() {
        return propagation;
    }

    public void setPropagation(String propagation) {
        this.propagation = propagation;
    }
    
    
}
