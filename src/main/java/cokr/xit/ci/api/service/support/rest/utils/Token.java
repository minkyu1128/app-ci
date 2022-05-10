package cokr.xit.ci.api.service.support.rest.utils;

import cokr.xit.ci.api.service.support.rest.model.conf.DataBodyGenerateTokenResp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Token {


    private volatile static Token instance;
    private static DataBodyGenerateTokenResp data;

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

    private Token(DataBodyGenerateTokenResp data) {
        this.data = data;
    }

    public static Token getInstance() {
        return instance;
    }
    public static Token getInstance(DataBodyGenerateTokenResp data) {
        if(!isValidStat())
            synchronized (Token.class) {
                if(!isValidStat())
                    instance = new Token(data);
            }

        return instance;
    }


    public static boolean isValidStat() {
        if (instance == null)
            return false;
        if (data == null)
            return false;
        if (Long.parseLong(data.getExpiredDt()) < Long.parseLong(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))))
            return false;

        return true;
    }


    public DataBodyGenerateTokenResp getData() {
        return data;
    }

}
