package cokr.xit.ci.api.service.support.rest.model.conf;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "DataBodyCiResp",  description = "아이핀 CI 요청 응답DataBody")
public class DataBodyCiResp {

    @JsonAlias({"rsp_cd"})
    @Schema(required = true, title = "dataBody 응답 코드", example = "P000", description = "dataBody 정상처리여부 (P000 성공, 이외 모두 오류)")
    private String rspCd;

    @JsonAlias({"res_msg"})
    @Schema(required = false, title = "dataBody 응답 메시지", example = " ", description = "rsp_cd가 \"EAP\"로 시작될 경우 오류 메시지 세팅됨")
    private String resMsg;

    @JsonAlias({"result_cd"})
    @Schema(required = false, title = "상세결과코드", example = " ", description = "rsp_cd가 P000일때 상세결과코드(0000:처리완료, 0001:대칭키 기간 만료, 0002:대칭키를 찾을 수 없음, 0003:대칭키를 발급한 회원사 아님, 0004:복호화 오류, 0005:필수입력값 오류(integrity_value, enc_data 내 필수값 확인), 0006:데이터 무결성 오류(hmac값 불일치), 0007:정보요청유형 입력값 오류(info_req_type이 1 아님), 0008:주민번호 유효성 오류(생년월일 유효성 및 숫자 아님), 0009:거래요청시간 포멧오류(req_dtim 자릿수 및 숫자 아님), 0099:기타오류)")
    private String resultCd;

    @JsonAlias({"enc_data"})
    @Schema(required = false, title = "JSON 암호화값", example = " ", description = "P000일때 나감. 응답정보를 회원사에서 요청 시 전달한 대칭키로 암호화한 값")
    private String encData;

    @JsonAlias({"integrity_value"})
    @Schema(required = false, title = "base64 인코딩 값", example = " ", description = "무결성체크를 위해 enc_data를 HMAC처리 후, BASE64 인코딩한 값")
    private String integrityValue;


    @Schema(required = false, title = "JSON 디코딩 값", example = " ", description = "encData 를 디코딩한 값")
    @Setter
    private EncData decEncData;

}
