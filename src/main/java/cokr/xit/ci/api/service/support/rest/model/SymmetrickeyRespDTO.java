package cokr.xit.ci.api.service.support.rest.model;

import cokr.xit.ci.api.service.support.rest.model.conf.SymkeyStatInfo;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class SymmetrickeyRespDTO implements TransDTO {

    @JsonAlias({"rsp_cd"})
    private String rspCd;          //dataBody 정상처리여부 (P000 성공, 이외 모두 오류)

    @JsonAlias({"res_msg"})
    private String resMsg;         //rsp_cd가 "EAPI"로 시작될 경우 오류 메시지 세팅

    @JsonAlias({"result_cd"})
    private String resultCd;       //rsp_cd가 P000일 때 상세결과코드(0000:대칭키발급, 0001:공개키기간만료, 0002:공개키를 찾을 수 없음, 0003:공개키를 발급한 회원사 아님, 0004:복호화 오류, 0005:필수입력값 오류(key_version, key_info 필수값 확인), 0006:대칭키 등록 가능 회원사 아님, 0007:key 중복 오류(현재 및 직전에 사용한 Key 사용 불가), 0008:요청사이트코드와 공개키발급 사이트코드 다름, 0099: 기타오류)

    @JsonAlias({"symkey_stat_info"})
    private SymkeyStatInfo symkeyStatInfo; //JSON값(회원사에 생성되어 있는 대칭키 버전별 유효기간(result_cd 가 0000, 0007일 경우에 나감)


}
