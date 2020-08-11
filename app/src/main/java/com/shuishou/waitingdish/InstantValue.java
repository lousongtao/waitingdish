package com.shuishou.waitingdish;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public final class InstantValue {
    public static final DateFormat DFYMDHMS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final String DOLLAR = "$";
    public static final String DOLLARSPACE = "$ ";
    public static final String NULLSTRING = "";
    public static final String SPACESTRING = " ";
    public static final String SLLASHSTRING = " ";
    public static final String ENTERSTRING = "\n";

    public static final String FORMAT_DOUBLE = "%.2f";


    public static final int DISPLAY_DISH_WIDTH = 240;
    public static final int DISPLAY_DISH_HEIGHT = 300;


    public static final String FORMAT_DOUBLE_2DECIMAL = "%.2f";

    public static String URL_TOMCAT = null;
    public static int QUERY_FREQUENCY = 30; //Default 30 seconds

    public static final String LOCAL_CATALOG_ERRORLOG = "/data/data/com.shuishou.waitingdish/errorlog/";
    public static final String FILE_SERVERURL = "/data/data/com.shuishou.waitingdish/serverconfig";
    public static final String FILE_CONFIGINFO = "/data/data/com.shuishou.waitingdish/configinfo";
    public static final String ERRORLOGPATH = "/data/data/com.shuishou.waitingdish/errorlog/";

    public static final String CONFIGINFO_QUERYFREQUENCY = "QUERYFREQUENCY";

    public static final String SERVER_CATEGORY_UPGRADEAPK = "upgradeApk";
    public static final String LOCAL_CATEGORY_UPGRADEAPK = "/data/user/0/com.shuishou.waitingdish/files/";

}
