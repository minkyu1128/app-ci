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
@Schema(name = "DataBodySymkeyResp",  description = "대칭키 요청 API 응답DataBody")
public class DataBodySymkeyResp {

    @JsonAlias({"rsp_cd"})
    @Schema(required = true, title = "dataBody 응답코드", example = " ", description = "dataBody 정상처리여부 (P000 성공, 이외 모두 오류)")
    private String rspCd;

    @JsonAlias({"res_msg"})
    @Schema(required = false, title = "dataBody 응답메시지", example = " ", description = "rsp_cd가 \"EAPI\"로 시작될 경우 오류 메시지 세팅")
    private String resMsg;

    @JsonAlias({"result_cd"})
    @Schema(required = false, title = "상세결과코드", example = " ", description = "rsp_cd가 P000일 때 상세결과코드(0000:대칭키발급, 0001:공개키기간만료, 0002:공개키를 찾을 수 없음, 0003:공개키를 발급한 회원사 아님, 0004:복호화 오류, 0005:필수입력값 오류(key_version, key_info 필수값 확인), 0006:대칭키 등록 가능 회원사 아님, 0007:key 중복 오류(현재 및 직전에 사용한 Key 사용 불가), 0008:요청사이트코드와 공개키발급 사이트코드 다름, 0099: 기타오류)")
    private String resultCd;       

    @JsonAlias({"symkey_stat_info"})
    @Schema(required = false, title = "JSON값", example = " ", description = "JSON값(회원사에 생성되어 있는 대칭키 버전별 유효기간(result_cd 가 0000, 0007일 경우에 나감)")
    private SymkeyStatInfo symkeyStatInfo; 



    /* ===========================================================================
    * 아이핀CI API 요청 시 필요한 값
    =========================================================================== */
    @Setter
    private String siteCode;  //[필수]사이트코드
    @Setter
    private String requestNo; //[필수]요청고유번호(이용기관에서 임의 생성한 값)
    @Setter
    private String key;       //[필수]회원사에서 사용할 암호화 KEY
    @Setter
    private String iv;        //[필수]Initial Vector값
    @Setter
    private String hmacKey;   //[필수]회원사에서 사용한 HMAC KEY

}
