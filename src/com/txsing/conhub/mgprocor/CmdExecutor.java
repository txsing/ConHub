/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.mgprocor;

import com.txsing.conhub.exceptions.DBConnectException;
import com.txsing.conhub.exceptions.IDNotFoundException;
import com.txsing.conhub.ult.*;
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 *
 * @author txsing
 */
public class CmdExecutor {

    public static List<String> execute(String cmd) {
        return routeAndExecute(cmd);
    }

    public static List<String> routeAndExecute(String cmd) {
        if (cmd.startsWith("docker")) {
            return sendCmdToDocker(cmd);
        } else if (cmd.toLowerCase().startsWith("select")) {
            return sendCmdToDB(cmd);
        }else if(cmd.matches("\\s+")){
            return null;
        } 
        else {
            System.err.println("LOG(ERROR): Unsupported Commands");
            List<String> result = new ArrayList<>();
            result.add(Constants.CONHUB_RES_SEPARATOR);
            return null;
        }
    }

    /**
     * send SQL queries to Database
     */
    public static List<String> sendCmdToDB(String cmd) {
        List<String> result = null;
        try {
            Connection conn = DBConnector.connectPostgres();
            cmd = Parser.parseCQL(conn, cmd);
            if(!cmd.startsWith("TAG: ")){
                result = executeSQL(conn, cmd);
            }else{
                result = new ArrayList<>();
                result.add(cmd);
            }            
            conn.close();
        } catch (IDNotFoundException e) {
            System.err.println(e.getMessage());
            System.err.println("LOG(ERROR): failed to execute CQL");
        } catch (DBConnectException e){
            System.err.println(e.getMessage());
            System.err.println("LOG(ERROR): failed to connect DB");
        } catch (Exception e){
            System.err.println(e.getMessage());
            System.err.println("LOG(ERROR): failed to execute CQL");
        }
        return result;
    }

    public static List<String> executeSQL(Connection conn, String sql) throws SQLException{
        List<String> resultStringList = new ArrayList<>();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            
            //print headers
            String headline = "";
            for (int i = 1; i <= columnCount; i++){
                headline = headline + rsmd.getColumnName(i)+"\t";
            }
            resultStringList.add(headline.toUpperCase());
            
            //print values
            while (rs.next()) {
                String line = "";
                for (int i = 1; i <= columnCount; i++) {
                    line = line + rs.getString(i) + "\t";
                }
                resultStringList.add(line);
            }
        resultStringList.add(Constants.CONHUB_RES_SEPARATOR);
        return resultStringList;
    }

    /**
     * send docker command to Docker Client docker commands can later
     *
     */
    public static List<String> sendCmdToDocker(String cmd) {
        try {
            String[] cmdParaArray = cmd.split(" ");
            if (isIntervativeCmd(cmdParaArray)) {
                return executeInteractiveDockerCMD(cmdParaArray);
            } else {
                return executeNonInteractiveDockerCMD(cmdParaArray, System.out);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> executeNonInteractiveDockerCMD(
            String[] cmdParaArray, OutputStream os) {
        List<String> resultStringList = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder(cmdParaArray)
                    .redirectErrorStream(true);
            Process proc = pb.start();

            InputStream is = proc.getInputStream();

            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(is));

            String line;            
            while ((line = reader.readLine()) != null) {
                os.write((line+"\n").getBytes());
                os.flush();
                //System.out.println(line);                
                if(!cmdParaArray[1].equals("pull") && !reader.ready()){
                    break;
                }
            }
            is.close();
            
            resultStringList.add("------");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultStringList;
    }

    /**
     *
     * @param cmdParaArray
     */
    public static List<String> executeInteractiveDockerCMD(String[] cmdParaArray) {
        List<String> resultStringList = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder(cmdParaArray);
            Process proc = pb.start();

            InputStream es = proc.getErrorStream();
            InputStream is = proc.getInputStream();
            OutputStream os = proc.getOutputStream();

            InputStreamGobbler errorGobbler
                    = new InputStreamGobbler(es, System.out, "ERROR> ");
            InputStreamGobbler readerGobbler
                    = new InputStreamGobbler(is, System.out, "");
            OutputStreamGobbler writerGobbler
                    = new OutputStreamGobbler(os);

            errorGobbler.start();
            readerGobbler.start();
            writerGobbler.start();

            while (writerGobbler.isAlive()) {
                Thread.sleep(3);
            }

            resultStringList.add("---Container Exited---");
            is.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultStringList;
    }
    
    private static boolean isIntervativeCmd(String[] cmdParaArray){
        //e,g,. docker attach/exec
        if(Constants.DOCKER_INTERACTIVE_CMD.contains(cmdParaArray[1])){
            return true;
        }else if(cmdParaArray[1].equals("run")) {
            List<String> tmpList = Arrays.asList(cmdParaArray);
            
            //"docker run -d -i tomcat bash" is not interactive
            if(tmpList.contains("-d"))
                return false;
            
            //"docker run -i tomcat bash" is interactive
            if(tmpList.contains("-i"))
                return true;
            
            //docker run hello-world
            return false;
        }else{
            return false;
        }
    }
}
