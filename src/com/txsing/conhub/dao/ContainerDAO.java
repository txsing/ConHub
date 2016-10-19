/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.dao;

import com.txsing.conhub.mgprocor.CmdExecutor;
import com.txsing.conhub.object.Container;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.simple.parser.ParseException;

/**
 *
 * @author txsing
 */
public class ContainerDAO {

    public static void deleteContainerFromDB(String conID, Connection conn) throws SQLException {
        String sql = null;
        try {
            sql = "DELETE FROM CONTAINERS WHERE " + "conid = '"
                    + conID + "'";
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (Exception e) {
            System.err.println("LOG(DEBUG): Possible problematic SQL(DelCon): \n    " + sql);
            throw (e);
        }
        System.out.println("LOG(INFO): DEL CON: " + conID.substring(0, 12));
    }

    public static void insertNewContainerIntoDB(String containerID, Connection conn) 
            throws SQLException, ParseException, IOException{
        String sql = null;
        try {
            Container newContainer = new Container(JsonDAO
                    .getImageAndConJSONInfo(containerID));

            sql = "INSERT INTO CONTAINERS VALUES(" + "'"
                    + newContainer.getContainerID() + "', " + "'"
                    + newContainer.getImageID() + "', " + "'"
                    + newContainer.getStartCommand() + "', " + "'"
                    + newContainer.getCreateDate() + "', " + "'"
                    + newContainer.getPorts() + "', " + "'"
                    + newContainer.getName() + "', " + "'"
                    + newContainer.getBuilderID() + "'" + ")";

            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (SQLException e) {
            System.err.println("LOG(DEBUG): Possible problematic SQL(InsCon): \n    " + sql);
            throw (e);
        }
        System.out.println("LOG(INFO): INSERT CON: " + containerID.substring(0, 12));
    }

    public static List<String> getContainerLstFromDB(Connection conn) throws SQLException {
        List<String> conLst = new ArrayList<>();
        
        String sql = "SELECT conid FROM containers";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            conLst.add(rs.getString(1));
        }
        stmt.close();

        return conLst;
    }

    public static List<String> getContainerLstFromDocker() throws IOException {
        List<String> conDKLst = new ArrayList<>();
        
        String[] cmdParaArray = {"docker", "ps", "-a", "-q","--no-trunc"};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CmdExecutor.executeNonInteractiveShellCMD(cmdParaArray, baos);
        String conidLst = baos.toString();
        baos.close();

        if (!conidLst.equals("")) {
            conDKLst = Arrays.asList(conidLst.split("\n"));
        }
        return conDKLst;
    }
}
