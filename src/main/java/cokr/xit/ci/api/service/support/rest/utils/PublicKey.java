package cokr.xit.ci.api.service.support.rest.utils;

import cokr.xit.ci.api.service.support.rest.model.conf.DataBodyPubkeyResp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PublicKey {


    private volatile static PublicKey instance;
    private static DataBodyPubkeyResp data;

//    private PubkeyInfo() {
//
//    }
//
//    public static PubkeyInfo getInstance() {
//        if (instance == null)
//            synchronized (PubkeyInfo.class) {
//                if (instance == null)
//                    instance = new PubkeyInfo();
//            }
//
//        return instance;
//    }

    private PublicKey(DataBodyPubkeyResp data) {
        this.data = data;
    }

    public static PublicKey getInstance() {
        return instance;
    }
    public static PublicKey getInstance(DataBodyPubkeyResp data) {
        if(!isValidStat())
            synchronized (PublicKey.class) {
                if(!isValidStat())
                    instance = new PublicKey(data);
            }

        return instance;
    }


    public static boolean isValidStat() {
        if (instance == null)
            return false;
        if (data == null)
            return false;
        if (Long.parseLong(data.getValidDtim()) < Long.parseLong(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))))
            return false;

        return true;
    }


    public DataBodyPubkeyResp getData() {
        return data;
    }

}
