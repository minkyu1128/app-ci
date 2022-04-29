package cokr.xit.ci.api.service.support.rest.model.conf;

import cokr.xit.ci.api.service.support.rest.model.TransDTO;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class EncData implements TransDTO {

    @JsonAlias({"result_cd"})
    private String resultCd;        //rsp_cd가 P000일때 상세결과코드(0000:처리완료, 0001:대칭키 기간 만료, 0002:대칭키를 찾을 수 없음, 0003:대칭키를 발급한 회원사 아님, 0004:복호화 오류, 0005:필수입력값 오류(integrity_value, enc_data 내 필수값 확인), 0006:데이터 무결성 오류(hmac값 불일치), 0007:정보요청유형 입력값 오류(info_req_type이 1 아님), 0008:주민번호 유효성 오류(생년월일 유효성 및 숫자 아님), 0009:거래요청시간 포멧오류(req_dtim 자릿수 및 숫자 아님), 0099:기타오류)

    @JsonAlias({"ci1"})
    private String ci1;             //연계정보1(Connection Info로 다른 웹사이트간 고객확인용으로 사용)

    @JsonAlias({"ci2"})
    private String ci2;             //연계정보2(연계정보1의 Key 유출에 대비한 예비값)

    @JsonAlias({"updt_cnt"})
    private String updtCnt;         //갱신횟수(연계정보 Key 유출될 경우 갱신 횟수(초기값 1세팅))

    @JsonAlias({"tx_unique_no"})
    private String txUniqueNo;      //거래고유번호(result_cd가 0000일 경우 NICE에서 제공하는 거래일련번호)

}
