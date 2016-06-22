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
public class ImageDAO {
    private String imageid;
    private String parent;
    private String fileid; //CURRENTLY NULL. id of dockerfile(null if a. image build from container b. image pulled from repo 
    private String repository;
    private String tag; //needs contemplation
    private long size;
    private String maintainer; //creator of docker file. Field in JSON, can be null also.
    private String builder; //If user A uses file B to build image C : A is the builder
    private String command;
    private String containerid; //for contoimage
    private String workinglabel;
    
    public ImageDAO(){
        
    }
    
    public ImageDAO(JSONObject jsob){
        this.imageid=jsob.get("Id").toString();
        if(this.imageid.contains("sha256:")){
            this.imageid=this.imageid.substring(imageid.indexOf("sha256:")+7);
        }
        
        this.parent=jsob.get("Parent").toString();
        if(this.parent.contains("sha256:")){
            this.parent=this.parent.substring(parent.indexOf("sha256:")+7);
        }
        
        this.fileid=null;
        
        String repotag=jsob.get("RepoTags").toString();
        repotag=repotag.substring(1,repotag.lastIndexOf("]"));
        repotag=repotag.replaceAll("\"", "");
        System.out.println(repotag);
        //multiple tags may occur for a paricular image - picking only latest tag
        if(repotag.length()==0){
            this.repository="";
            this.tag="";
        }
        else {
            this.repository=repotag.substring(repotag.lastIndexOf(",")+1,repotag.lastIndexOf(":"));
            this.tag=repotag.substring(repotag.lastIndexOf(":")+1);
        }
        
//        System.out.println(repository+"  "+tag);
        
        this.size=Integer.parseInt(jsob.get("Size").toString());
        
        this.maintainer=null;
        this.builder=null;
        this.command=null;
        
        this.containerid=jsob.get("Container").toString();
        if(this.containerid.contains("sha256:")){
            this.containerid=this.containerid.substring(containerid.indexOf("sha256:")+7);
        }
        
    }

    public String getImageid() {
        return imageid;
    }

    public String getParent() {
        return parent;
    }

    public String getFileid() {
        return fileid;
    }

    public String getRepository() {
        return repository;
    }

    public String getTag() {
        return tag;
    }

    public long getSize() {
        return size;
    }

    public String getMaintainer() {
        return maintainer;
    }

    public String getBuilder() {
        return builder;
    }

    public String getCommand() {
        return command;
    }

    public String getContainerid() {
        return containerid;
    }

    public String getWorkinglabel() {
        return workinglabel;
    }
    
    
}
