/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.MGProcor;

import java.util.*;
import com.txsing.conhub.ult.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author txsing
 */
public class main {

    public static void main(String[] args) {
//        Scanner scanner = new Scanner(System.in);
//        while (scanner.hasNext()) {
//            String cmd = scanner.nextLine();
//            List<String> output = executeCMD(cmd);
//            for (String str : output) {
//                System.out.println(str);
//            }
//        }
//        scanner.close();
        Synchro.syncRepo();
    }
    
    public static List<String> executeCMD(String cmd) {
        return CmdExecutor.execute(cmd);
    }
    
    public void setLogger() {
        try {
            Logger logger = Logger.getLogger("com.txsing.conhub.mgprocor");
            FileHandler fileHandler = new FileHandler("mgprocor.log");
            fileHandler.setLevel(Level.FINER);

            logger.addHandler(fileHandler);
        } catch (Exception e) {

        }
    }

    public static void startFileWatchService() {
        try {
            DockerFileWatcher imageWatcher
                    = new DockerFileWatcher(Constants.DOCKER_PATH_IMAGE,
                            "image", false);
            imageWatcher.start();

            DockerFileWatcher containerWatcher
                    = new DockerFileWatcher(Constants.DOCKER_PATH_CONTAINER,
                            "container", false);
            containerWatcher.start();

            DockerFileWatcher repoWatcher
                    = new DockerFileWatcher(Constants.DOCKER_PATH_REPOSITORY, "repo", false);
            repoWatcher.start();

        } catch (Exception e) {
            System.err.println("Failed to start monitor service!");
            e.printStackTrace();
        }
    }
}
