package cokr.xit.ci.api.service.support.rest;

import cokr.xit.ci.api.model.ResponseVO;

public interface NiceCiApiStruct {

    /**
     * 1. 토큰발급 API
     */
    ResponseVO generateToken();

    /**
     * 2. 토큰폐기 API
     */
    ResponseVO revokeToken();

    /**
     * 공개키요청 API
     *  -.대칭키 암호화를 위한 공개키 요청
     */
    ResponseVO generatePublickey();

    /**
     * 대칭키요청 API
     *  -.데이터 암호화를 위한 대칭키 요청
     */
    ResponseVO generateSymmetrickey();

    /**
     * CI 조회
     */
    ResponseVO getCi();


}
