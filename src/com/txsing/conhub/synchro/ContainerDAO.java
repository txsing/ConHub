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
public class ContainerDAO {
    private String conid;
    private String imageid;
    private String command;
    private String createdate;
    private String status;
    private long ports;
    private String name;
    private String creator;
    private String workinglabel;

    //parser from JSON
    public ContainerDAO(JSONObject jsob){
        //container id
        this.conid=jsob.get("Id").toString();
        if(this.conid.contains("sha256:")){
            this.conid=this.conid.substring(this.conid.indexOf("sha256:")+7);
        }

        //image id
        this.imageid=jsob.get("Image").toString();
        if(this.imageid.contains("sha256:")){
            this.imageid=this.imageid.substring(this.imageid.indexOf("sha256:")+7);
        }

        //command -- default command to start container
        this.command=((JSONObject)jsob.get("Config")).get("Cmd").toString();

        //creation date in ISO 8601 (retured by default in Docker JSON)
        this.createdate=jsob.get("Created").toString();

        //status
        this.status=((JSONObject) jsob.get("State")).get("Status").toString();
//        JSONObject jsobTemp=(JSONObject) jsob.get("State");
//        System.out.println(jsobTemp.get("Status"));

        //ports -- ignoring for now
        this.ports=-1;

        //Name of container
        this.name=(jsob.get("Name")).toString().substring(1);
        
        //name of creator
        this.creator=((JSONObject)jsob.get("Config")).get("User").toString();
    }

    public ContainerDAO() {
    }

    //getter and setter
    public void setWorkinglabel(String workinglabel) {    
        this.workinglabel = workinglabel;
    }

    public String getWorkinglabel() {
        return workinglabel;
    }
    
    public String getConid() {
        return conid;
    }

    public void setConid(String conid) {
        this.conid = conid;
    }

    public String getImageid() {
        return imageid;
    }

    public void setImageid(String imageid) {
        this.imageid = imageid;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getCreatedate() {
        return createdate;
    }

    public void setCreatedate(String createdate) {
        this.createdate = createdate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getPorts() {
        return ports;
    }

    public void setPorts(long ports) {
        this.ports = ports;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
    
    
}
