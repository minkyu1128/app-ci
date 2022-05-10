package cokr.xit.ci.api.service.support.rest.model.conf;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "DataBodyPubkeyResp",  description = "공개키 요청 API 응답DataBody")
public class DataBodyPubkeyResp {

    @JsonAlias({"rsp_cd"})
    @Schema(required = true, title = "dataBody 응답코드", example = " ", description = "dataBody 정상처리여부 (P000 성공, 이외 모두 오류)")
    private String rspCd;

    @JsonAlias({"res_msg"})
    @Schema(required = false, title = "dataBody 응답메시지", example = " ", description = "rsp_cd가 \"EAPI\"로 시작될 경우 오류 메시지 세팅")
    private String resMsg;

    @JsonAlias({"result_cd"})
    @Schema(required = false, title = "상세결과코드", example = " ", description = "rsp_cd가 P000일 때 상세결과코드(0000:공개키발급, 0001:필수입력값 오류, 0003:공개키 발급 대상 회원사 아님, 0099: 기타오류)")
    private String resultCd;

    @JsonAlias({"site_code"})
    @Schema(required = false, title = "사이트코드", example = " ", description = "사이트코드")
    private String siteCode;

    @JsonAlias({"key_version"})
    @Schema(required = false, title = "공개키 버전", example = " ", description = "공개키 버전")
    private String keyVersion;

    @JsonAlias({"public_key"})
    @Schema(required = false, title = "공개키", example = " ", description = "공개키")
    private String publicKey;

    @JsonAlias({"valid_dtim"})
    @Schema(required = false, title = "공개키 만료일시", example = " ", description = "공개키 만료일시(YYYYMMDDHH24MISS)")
    private String validDtim;




}
