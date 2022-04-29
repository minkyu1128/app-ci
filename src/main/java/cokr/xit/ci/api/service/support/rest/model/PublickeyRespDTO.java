package cokr.xit.ci.api.service.support.rest.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class PublickeyRespDTO implements TransDTO {

    @JsonAlias({"rsp_cd"})
    private String rspCd;      //dataBody 정상처리여부 (P000 성공, 이외 모두 오류)

    @JsonAlias({"res_msg"})
    private String resMsg;     //rsp_cd가 "EAPI"로 시작될 경우 오류 메시지 세팅

    @JsonAlias({"result_cd"})
    private String resultCd;   //rsp_cd가 P000일 때 상세결과코드(0000:공개키발급, 0001:필수입력값 오류, 0003:공개키 발급 대상 회원사 아님, 0099: 기타오류)

    @JsonAlias({"site_code"})
    private String siteCode;   //사이트코드

    @JsonAlias({"key_version"})
    private String keyVersion; //공개키 버전

    @JsonAlias({"public_key"})
    private String publicKey; //공개키

    @JsonAlias({"valid_dtim"})
    private String validDtim; //공개키 만료일시(YYYYMMDDHH24MISS)




}
