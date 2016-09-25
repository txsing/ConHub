/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.ult;

import java.util.List;

/**
 *
 * @author txsing
 */
public class Helper {
    
    /***
     * 
     * @param shortID
     * @param fullIDLst
     * @return -1: There is no full id can match with given short ID
     *          0: The given short ID is ambiguous.
     *          1: The given short ID can uniquely refer to one full ID.
     * @throws java.lang.Exception
     */
    public static String getFullID(String shortID, List<String> fullIDLst) throws Exception{
        if(shortID == null || shortID.length() == 0){
            throw new Exception("(ERROR): ID EMPTY");
        }
        
        if(shortID.length() == 64){
            if(fullIDLst.contains(shortID))
                return shortID;
            else{
                throw new Exception("(ERROR): '"+shortID+"' NOT FOUND");
            }
        }
        boolean firstOccur = false;
        String result = null;
        for(String fullid : fullIDLst){           
            if(fullid.startsWith(shortID)){
                if(!firstOccur){
                    firstOccur = true;
                    result = fullid;
                }
                else{
                    throw new Exception("(ERROR): AMBIGUOUS ID: "+shortID);
                }
            }
        }
        if(!firstOccur){
            throw new Exception("(ERROR): ID '"+shortID+"' NO MATCH");
        }
        return result;
    }
}
