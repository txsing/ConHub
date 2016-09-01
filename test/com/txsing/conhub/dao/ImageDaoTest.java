/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.dao;

import com.txsing.conhub.object.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author txsing
 */
public class ImageDaoTest {
    public static void main(String[] args) throws Exception {
        File jsonfile = new File("txsing.json");
        BufferedReader br = new BufferedReader(new FileReader(jsonfile));
        String imageInfoJsonString = "";
        String line = null;
        while((line = br.readLine() )!=null){
            imageInfoJsonString = imageInfoJsonString + line;
        }
        
        imageInfoJsonString = imageInfoJsonString.substring(1, 
                    imageInfoJsonString.lastIndexOf(']'));
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(imageInfoJsonString);
        Image newImage = new Image(jsonObject);
        
        System.out.println(newImage.getRepo());
        System.err.println(newImage.getTag());
    }
}
