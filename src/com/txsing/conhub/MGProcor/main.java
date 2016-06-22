/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.MGProcor;

import java.util.*;
import com.txsing.conhub.ult.*;

/**
 *
 * @author txsing
 */
public class main {
    public static void main(String[] args) {
       
        try {
            DockerFileWatcher imageWatcher = 
                    new DockerFileWatcher(Constants.DOCKER_PATH_IMAGE, 
                    "image", false);
            imageWatcher.start();
            
            DockerFileWatcher containerWatcher = 
                    new DockerFileWatcher(Constants.DOCKER_PATH_CONTAINER,
                    "container", false);
            containerWatcher.start();
            
        } catch (Exception e) {
            System.err.println("Failed to start monitor service!");
            e.printStackTrace();
        }
        
        //CmdExecutor.getImageJSONInfo("47bcc53f74dc");
        Scanner scanner = new Scanner(System.in);
        
        while (scanner.hasNext()) {
            String cmd = scanner.nextLine();
            List<String> output = executeCMD(cmd);
            for(String str : output){
                System.out.println(str);
            }
        }
        scanner.close();
    }
    
    public static List<String> executeCMD(String cmd){
        return CmdExecutor.execute(cmd);
    }
}
