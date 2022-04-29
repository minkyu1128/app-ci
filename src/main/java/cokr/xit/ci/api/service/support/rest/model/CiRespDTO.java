package cokr.xit.ci.api.service.support.rest.model;

import cokr.xit.ci.api.service.support.rest.model.conf.EncData;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class CiRespDTO implements TransDTO {

    @JsonAlias({"rsp_cd"})
    private String rspCd;          //dataBody 정상처리여부 (P000 성공, 이외 모두 오류)

    @JsonAlias({"res_msg"})
    private String resMsg;         //rsp_cd가 "EAPI"로 시작될 경우 오류 메시지 세팅

    @JsonAlias({"enc_data"})
    private EncData encData;       //JSON암호화값(rsp_cd가 P000일 때 나감) - 응답정보를 회원사에서 요청시 전달한 대칭키로 암호화한 값

    @JsonAlias({"integrity_value"})
    private String integrityValue; //무결성체크를 위해 enc_data를 HMAC처리 후, BASE64 인코딩한 값

}
