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
public class volumeDAO {
    private String name;
    private String driver;
    private String mountpoint;
    
    public volumeDAO(JSONObject jsob){
        this.name = jsob.get("Name")+"";
        this.driver = jsob.get("Driver") + "";
        this.mountpoint = jsob.get("Mountpoint") + "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getMountpoint() {
        return mountpoint;
    }

    public void setMountpoint(String mountpoint) {
        this.mountpoint = mountpoint;
    }
    
    
}
