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
     */
    public static String getFullID(String shortID, List<String> fullIDLst) throws Exception{
        boolean firstOccur = false;
        String result = null;
        for(String fullid : fullIDLst){
            System.err.println(fullid+"::"+shortID);
            if(fullid.startsWith(shortID)){
                if(!firstOccur){
                    firstOccur = true;
                    result = fullid;
                }
                else{
                    throw new Exception("ID AMBIGUOUS");
                }
            }
        }
        if(!firstOccur){
            throw new Exception("NO MATCH FOUND");
        }
        return result;
    }
}
