/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.dao;

import com.txsing.conhub.mgprocor.CmdExecutor;
import java.io.ByteArrayOutputStream;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author txsing
 */
public class JsonDAO {

    public static JSONObject getRepoJSONInfo() {
        JSONObject jsonObject;
        String[] cmdParaArray = {"/bin/bash", "-c", "echo scse | sudo -S " 
                + "cat /var/lib/docker/image/aufs/repositories.json"};
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CmdExecutor.executeNonInteractiveDockerCMD(cmdParaArray, baos);
            String imageInfoJsonString = baos.toString();
            //remove the firstCotd pair of bracket, [ {xxxxx} ]
            imageInfoJsonString = imageInfoJsonString.substring(
                    imageInfoJsonString.indexOf("{"));
            //System.err.println(imageInfoJsonString);
            baos.close();
            jsonObject = (JSONObject) new JSONParser().parse(imageInfoJsonString);
            jsonObject = (JSONObject) jsonObject.get("Repositories");
            return jsonObject;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    /**
     * get the json info of a specified container or image.
     *
     * @param ID
     * @return
     */
    public static JSONObject getImageAndConJSONInfo(String ID) {
        JSONObject jsonObject;
        String[] cmdParaArray = {"docker", "inspect", ID};
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CmdExecutor.executeNonInteractiveDockerCMD(cmdParaArray, baos);
            String imageInfoJsonString = baos.toString();
            
            imageInfoJsonString = imageInfoJsonString.substring(1, 
                    imageInfoJsonString.lastIndexOf(']'));
            
            baos.close();
            jsonObject = (JSONObject) new JSONParser().parse(imageInfoJsonString);
            return jsonObject;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
    
}
