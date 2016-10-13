/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.dao;

import com.txsing.conhub.mgprocor.CmdExecutor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author txsing
 */
public class JsonDAO {

    public static JSONObject getRepoJSONInfo() throws IOException, ParseException {
        JSONObject jsonObject;
        String[] cmdParaArray = {"/bin/bash", "-c", "echo scse | sudo -S "
            + "cat /var/lib/docker/image/aufs/repositories.json"};
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
    }

    /**
     * get the json info of a specified container or image.
     *
     * @param ID
     * @return
     */
    public static JSONObject getImageAndConJSONInfo(String ID) throws IOException, ParseException {
        JSONObject jsonObject;
        String[] cmdParaArray = {"docker", "inspect", ID};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CmdExecutor.executeNonInteractiveDockerCMD(cmdParaArray, baos);
        String imageInfoJsonString = baos.toString();

        imageInfoJsonString = imageInfoJsonString.substring(1,
                imageInfoJsonString.lastIndexOf(']'));

        baos.close();
        jsonObject = (JSONObject) new JSONParser().parse(imageInfoJsonString);
        return jsonObject;
    }

}
