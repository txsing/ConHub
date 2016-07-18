/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.object;

import org.json.simple.JSONObject;

/**
 *
 * @author txsing
 */
public class Image {
    private String imageID;
    private String repo = "";
    private String tag = "";
    private String parentImageID = "";
    private String dockerFileID = "";
    private int size;
    private String author = "";
    private String builderID = "";
    
    public Image(JSONObject imageJSONObject){
        //get image Id
        String imageid = imageJSONObject.get("Id").toString();
        this.imageID = imageid.substring(imageid.indexOf("sha256:")+7);
        
        String repotags = imageJSONObject.get("RepoTags").toString();
        String[] repotagsArray = repotags.split(":");
        this.repo = repotagsArray[0];
        this.tag = repotagsArray[1];
        
        //get image author; can be null;
        if(imageJSONObject.containsKey("Author")){
            this.author = imageJSONObject.get("Author").toString();
        }
        
        //get image size
        this.size = Integer.parseInt(imageJSONObject.get("Size").toString());
    }

    public String getImageID() {
        return imageID;
    }

    public String getParentImageID() {
        return parentImageID;
    }

    public String getDockerFileID() {
        return dockerFileID;
    }

    public String getAuthor() {
        return author;
    }

    public String getBuilderID() {
        return builderID;
    }
    
    public int getSize(){
        return size;
    }
    
    public String getRepo(){
        return repo;
    }
    
    public String getTag(){
        return tag;
    }
}
