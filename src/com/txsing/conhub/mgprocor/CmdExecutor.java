/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.mgprocor;

import com.txsing.conhub.app.*;
import com.txsing.conhub.exceptions.DBConnectException;
import com.txsing.conhub.exceptions.IDNotFoundException;
import com.txsing.conhub.ult.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import javax.swing.JFrame;

/**
 *
 * @author txsing
 */
public class CmdExecutor {

    public static List<String> execute(String cmd) {
        return routeAndExecute(cmd);
    }

    public static List<String> routeAndExecute(String cmd) {
        List<String> result = new ArrayList<>();;
        if (cmd.startsWith("docker")) {
            result = sendCmdToDocker(cmd);
        } else if (cmd.toLowerCase().startsWith("select")) {
            result = sendCmdToDB(cmd);
        } else if (cmd.startsWith("conviz")) {
            String[] args = cmd.split(" ");
            ConViz frame = new ConViz(args[1], args[2]);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(400, 600);
            frame.setVisible(true);
        } else if (cmd.startsWith("conr")) {
            //ConR frame = new ConR(xxx);
            //return null;
        } else if (cmd.matches("\\s+")) {
        } else if (cmd.equals("quit")) {
            System.out.println("Goodbye");
            System.exit(0);
        } else {
            System.err.println("LOG(ERROR): Unsupported Commands");
        }
        result.add(Constants.CONHUB_RES_SEPARATOR);
        return result;
    }

    /**
     * send SQL queries to Database
     */
    public static List<String> sendCmdToDB(String cmd) {
        List<String> result = null;
        try {
            Connection conn = DBConnector.connectPostgres();
            cmd = Parser.parseCQL(conn, cmd);
            if (!cmd.startsWith("TAG: ")) {
//                String[] cmdArrays = {"psql", "-U", "txsing",
//                    "-d", "consql", "-c", cmd};
//                result = executeNonInteractiveShellCMD(cmdArrays, System.out);
              result = executeSQL(conn, cmd);
            } else {
                result = new ArrayList<>();
                result.add(cmd);
            }
            conn.close();
        } catch (IDNotFoundException e) {
            System.err.println(e.getMessage());
            System.err.println("LOG(ERROR): failed to execute CQL");
        } catch (DBConnectException e) {
            System.err.println(e.getMessage());
            System.err.println("LOG(ERROR): failed to connect DB");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("LOG(ERROR): failed to execute CQL");
        }finally{
            return result;
        }
    }

    public static List<String> executeSQL(Connection conn, String sql) throws SQLException {
        List<String> resultStringList = new ArrayList<>();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        
        //print headers
        StringBuilder headline = new StringBuilder();
        //String headline = "";
        for (int i = 1; i <= columnCount; i++) {
            headline.append(String.format("# %-12s", rsmd.getColumnLabel(i)));
        }
        resultStringList.add(headline.toString().toUpperCase());

        //print values
        while (rs.next()) {
            StringBuilder line = new StringBuilder();
            for (int i = 1; i <= columnCount; i++) {
                line.append(String.format("| %-12s", rs.getString(i)));
            }
            resultStringList.add(line.toString());
        }
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
                return executeNonInteractiveShellCMD(cmdParaArray, System.out);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * this function behaves like a bash shell, receiving and execute commands,
     * return exactly the same result as real terminal bash shell returns.
     *
     * @param cmdParaArray
     * @param os
     * @return
     */
    public static List<String> executeNonInteractiveShellCMD(
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
                os.write((line + "\n").getBytes());
                os.flush();
                //System.out.println(line);                
                if (!cmdParaArray[1].equals("pull") && !reader.ready()) {
                    break;
                }
            }
            is.close();
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

    private static boolean isIntervativeCmd(String[] cmdParaArray) {
        //e,g,. docker attach/exec
        if (Constants.DOCKER_INTERACTIVE_CMD.contains(cmdParaArray[1])) {
            return true;
        } else if (cmdParaArray[1].equals("run")) {
            List<String> tmpList = Arrays.asList(cmdParaArray);

            //"docker run -d -i tomcat bash" is not interactive
            if (tmpList.contains("-d")) {
                return false;
            }

            //"docker run -i tomcat bash" is interactive
            if (tmpList.contains("-i")) {
                return true;
            }

            //docker run hello-world
            return false;
        } else {
            return false;
        }
    }
}
