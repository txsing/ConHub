/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.object;

import org.json.simple.*;

/**
 *
 * @author txsing
 */
public class Container {

    String containerID;
    String imageID;
    String startCommand = "";
    String createDate;
    //String status;    
    String ports;
    String name;

    String builderID = "";

    public Container(JSONObject containerJSONObject) {
        this.containerID = containerJSONObject.get("Id").toString();

        String imgid = containerJSONObject.get("Image").toString();
        this.imageID = imgid.substring(imgid.indexOf("sha256:") + 7);

        this.startCommand = ((JSONObject) containerJSONObject.get("Config"))
                .get("Cmd").toString();

        //e,g,. 2016-06-22T07:34:37 (length:19)
        this.createDate = containerJSONObject.get("Created").toString()
                .substring(0,19);

//        this.status = ((JSONObject) containerJSONObject.get("State"))
//                .get("Status").toString();

        //ports could be null
        JSONObject tmpPorts = (JSONObject) containerJSONObject.get("NetworkSettings");
        if(tmpPorts.get("Ports")!=null){
            this.ports = tmpPorts.get("Ports").toString();
        }
        
        //get the name of container, remove the starting '/'
        this.name = containerJSONObject.get("Name").toString().substring(1);
    }

    public String getContainerID() {
        return containerID;
    }

    public String getImageID() {
        return imageID;
    }

    public String getStartCommand() {
        return startCommand;
    }

    public String getCreateDate() {
        return createDate;
    }

//    public String getStatus() {
//        return status;
//    }

    public String getName() {
        return name;
    }

    public String getBuilderID() {
        return builderID;
    }

    public String getPorts() {
        return ports;
    }
}
