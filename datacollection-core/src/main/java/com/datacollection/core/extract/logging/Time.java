package com.datacollection.core.extract.logging;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Time {
    public static SimpleDateFormat SDFDATETIME = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
    public static SimpleDateFormat SDFDATE = new SimpleDateFormat("yyy-MM-dd");

    public static String getDefault(SimpleDateFormat simpleDateFormat){
        return simpleDateFormat.format(new Date());
    }
}
