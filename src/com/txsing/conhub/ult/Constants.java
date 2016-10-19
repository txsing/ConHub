/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.txsing.conhub.ult;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author txsing
 */
public class Constants {
    //separator
    public static final String CONHUB_RES_SEPARATOR = "";
    
    //ConHub default parameters
    public static final String CONHUB_DEFAULT_REGISTRY = "dockerhub";
    //DB parameters
    public static final String DB_POSTGRES_USER = "postgres";
    public static final String DB_POSTGRES_PASSWORD = "postgres";
    public static final String DB_POSTGRES_URL = "jdbc:postgresql://localhost/consql";
    public static final String DB_POSTGRES_DRIVER = "org.postgresql.Driver";
    
    
    //API
    public static final String API_INTER = "intersect";
    public static final String API_CHILD = "child";
    public static final String API_TAG = "tag";
    public static final String API_DIS = "distance";
    
    //Parser Strings
    public static final String PATTERN_INTERSECT = "(intersection)"
            + "(\\()(')(.{1,64})(')(,\\s*)(')([^\\)]{1,64})(')(\\))"; //INTERSECTION('arg1', 'arg2')
    public static final String PATTERN_CHILD = "(child)(\\()(')(.{1,64})(')(\\))"; //CHILD(arg)
    public static final String PATTERN_TAG = "(tag\\()(')([\\w]+)(')(,\\s*)(select.*)"; //TAG(label, sql)
    public static final String PATTERN_DIS = "(distance\\()([\\w]+)(,\\s*)([\\w]+)(\\))";
    
    //Dokcer Interactive Command
    public static final Set DOCKER_INTERACTIVE_CMD = new HashSet<>(
            Arrays.asList(
                    new String[]{"attach", "exec"}));
    
    //Docker AUFS Path
    public static final String DOCKER_HOME_PATH = "/var/lib/docker/"; 
    public static final String DOCKER_PATH_IMAGE = DOCKER_HOME_PATH
            +"image/aufs/imagedb/content/sha256/";
    public static final String DOCKER_PATH_CONTAINER = DOCKER_HOME_PATH
            +"containers/";
    public static final String DOCKER_PATH_REPOSITORY = DOCKER_HOME_PATH
            +"image/aufs/";
}
