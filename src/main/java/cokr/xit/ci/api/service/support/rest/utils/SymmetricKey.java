package cokr.xit.ci.api.service.support.rest.utils;

import cokr.xit.ci.api.service.support.rest.model.conf.DataBodySymkeyResp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SymmetricKey {


    private volatile static SymmetricKey instance;
    private static DataBodySymkeyResp data;
    //    private static String key;
//    private static String version;
    private static Long expireDt;

//    private SymkeyInfo() {
//
//    }
//
//    public static SymkeyInfo getInstance() {
//        if (instance == null)
//            synchronized (SymkeyInfo.class) {
//                if (instance == null)
//                    instance = new SymkeyInfo();
//            }
//
//        return instance;
//    }

    private SymmetricKey(DataBodySymkeyResp data) {
        this.data = data;
//        this.expireDt = Long.parseLong(LocalDateTime.now().plusMonths(5L).format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        this.expireDt = Long.parseLong(LocalDateTime.now().plusDays(1L).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
    }

    public static SymmetricKey getInstance() {

        return instance;
    }
    public static SymmetricKey getInstance(DataBodySymkeyResp data) {
        if(!isValidStat())
            synchronized (SymmetricKey.class) {
                if(!isValidStat())
                    instance = new SymmetricKey(data);
            }

        return instance;
    }


    public static boolean isValidStat() {
        if (instance == null)
            return false;
        if (data == null)
            return false;
        if (expireDt < Long.parseLong(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))))
            return false;

        return true;
    }


    public DataBodySymkeyResp getData() {
        return data;
    }

    public Long getExpireDe(){
        return expireDt;
    }
}
