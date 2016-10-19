/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.ult;

import java.util.List;
import com.txsing.conhub.exceptions.IDNotFoundException;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author txsing
 */
public class Helper {

    /**
     * *
     *
     * @param shortID
     * @param fullIDLst
     * @return -1: There is no full id can match with given short ID 0: The
     * given short ID is ambiguous. 1: The given short ID can uniquely refer to
     * one full ID.
     * @throws com.txsing.conhub.exceptions.IDNotFoundException
     */
    public static String getFullID(String shortID, List<String> fullIDLst)
            throws IDNotFoundException {
        if (shortID == null || shortID.length() == 0) {
            throw new IDNotFoundException("(ERROR): EMPTY INPUT");
        }

        if (shortID.length() == 64) {
            if (fullIDLst.contains(shortID)) {
                return shortID;
            } else {
                throw new IDNotFoundException("'" + shortID + "' NO MATCH FOUND");
            }
        }
        boolean firstOccur = false;
        String result = null;
        for (String fullid : fullIDLst) {
            if (fullid.startsWith(shortID)) {
                if (!firstOccur) {
                    firstOccur = true;
                    result = fullid;
                } else {
                    throw new IDNotFoundException("AMBIGUOUS ID: " + shortID);
                }
            }
        }
        if (!firstOccur) {
            throw new IDNotFoundException("ID '" + shortID + "' NO MATCH FOUND");
        }
        return result;
    }

    public static Logger getConHubLogger() {
        try {
            //LogManager.getLogManager().reset();
            Logger logger = Logger.getLogger("conhub");
            FileHandler fh = new FileHandler("conhub.log", true);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            logger.addHandler(fh);
            logger.setUseParentHandlers(false);
            return logger;
        }catch(IOException e){
            System.err.println("(ERROR): Failed to init logger");
            System.err.println(e.getMessage());
        }
        return null;
    }
}
