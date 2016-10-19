/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.mgprocor;

import static com.txsing.conhub.mgprocor.CmdExecutor.executeNonInteractiveShellCMD;
import java.util.List;

/**
 *
 * @author txsing
 */
public class CmdExecutorTest {

    public static void main(String[] args) {
        String cmd = "select R.reponame, T.tag from Images I, Tags T, Repositories R " +
"where I.imageid = T.imageid and T.repoid = R.repoid;";
        String[] cmdArrays = {"psql", "-U", "txsing",
            "-d", "consql", "-c", cmd};
        List<String> result = executeNonInteractiveShellCMD(cmdArrays, System.out);
       for(String e : result){
           System.out.println(e);
       }
    }
}
