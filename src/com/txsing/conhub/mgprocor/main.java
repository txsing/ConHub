/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.mgprocor;

import java.util.*;
import com.txsing.conhub.ult.*;
import java.io.IOException;

/**
 *
 * @author txsing
 */
public class main {

    public static void main(String[] args) {
        initSystem();
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("conhub=# ");
            while (scanner.hasNext()) {
                String cmd = scanner.nextLine();
                if(cmd.equals("\\q")){
                    System.exit(0);
                }
                List<String> output = executeCMD(cmd);
                if (output != null) {
                    output.stream().forEach((str) -> {
                        System.out.println(str);
                    });
                }
                System.out.print("conhub=# ");
            }
        }
    }

    public static List<String> executeCMD(String cmd) {
        return CmdExecutor.execute(cmd);
    }

    public static void initSystem() {
        Synchro.getInstance().syncAll();
        startFileWatchService();
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
        } catch (IOException e) {
            System.err.println("LOG(ERROR): failed to start monitor service!");
            System.err.println(e.getMessage());
        }
    }
}
