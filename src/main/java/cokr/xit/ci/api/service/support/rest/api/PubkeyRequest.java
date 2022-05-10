package cokr.xit.ci.api.service.support.rest.api;

import cokr.xit.ci.api.service.support.rest.code.NiceCiApiCd;
import cokr.xit.ci.api.service.support.rest.model.NiceCiRespDTO;
import cokr.xit.ci.api.service.support.rest.model.NiceCiRespVO;
import cokr.xit.ci.api.service.support.rest.model.conf.DataBodyPubkeyResp;
import cokr.xit.ci.api.service.support.rest.utils.Token;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Base64Util;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@SuperBuilder
public class PubkeyRequest extends NiceCiApiAbstract {

    protected final String HOST;
    protected final String API_URL;


    public NiceCiRespVO<DataBodyPubkeyResp> execute(String clientId) throws Exception {
        /* ==============================================================================
        * 유효성 확인
        ============================================================================== */
        if (StringUtils.isEmpty(Token.getInstance().getData().getAccessToken()))
            return NiceCiRespVO.<DataBodyPubkeyResp>errBuilder().errCode(NiceCiApiCd.FAIL).errMsg("엑세스토큰은 필수 입력값 입니다.").build();
        if (StringUtils.isEmpty(clientId))
            return NiceCiRespVO.<DataBodyPubkeyResp>errBuilder().errCode(NiceCiApiCd.FAIL).errMsg("클라이언트ID는 필수 입력값 입니다.").build();


        /* ==============================================================================
        * API 호출
        ============================================================================== */
        NiceCiRespDTO<DataBodyPubkeyResp> responseDTO = this.generatePublickey(Token.getInstance().getData().getAccessToken(), clientId);


        /* ==============================================================================
        * 결과 반환
        ============================================================================== */
        if (!"1200".equals(responseDTO.getDataHeader().getGW_RSLT_CD()))
            return NiceCiRespVO.<DataBodyPubkeyResp>errBuilder()
                    .errCode(NiceCiApiCd.valueOfEnum("HEAD_" + responseDTO.getDataHeader().getGW_RSLT_CD()))
                    .errMsg(String.format("[%s]: %s", responseDTO.getDataHeader().getGW_RSLT_CD(), responseDTO.getDataHeader().getGW_RSLT_MSG()))
                    .build();
        if (!"P000".equals(responseDTO.getDataBody().getRspCd()))
            return NiceCiRespVO.<DataBodyPubkeyResp>errBuilder()
                    .errCode(NiceCiApiCd.valueOfEnum(responseDTO.getDataBody().getRspCd()))
                    .errMsg(String.format("[%s]: %s. %s", responseDTO.getDataBody().getRspCd(), NiceCiApiCd.valueOfEnum(responseDTO.getDataBody().getRspCd()).getCodeNm(), responseDTO.getDataBody().getResMsg()))
                    .build();
        if (!"0000".equals(responseDTO.getDataBody().getResultCd()))
            return NiceCiRespVO.<DataBodyPubkeyResp>errBuilder()
                    .errCode(NiceCiApiCd.valueOfEnum("PUBKEY_" + responseDTO.getDataBody().getResultCd()))
                    .errMsg(String.format("[%s]: %s", responseDTO.getDataBody().getResultCd(), NiceCiApiCd.valueOfEnum("PUBKEY_" + responseDTO.getDataBody().getResultCd()).getCodeNm()))
                    .build();


        return NiceCiRespVO.<DataBodyPubkeyResp>okBuilder().resultInfo(responseDTO.getDataBody()).build();
    }


    protected NiceCiRespDTO<DataBodyPubkeyResp> generatePublickey(String accessToken, String clientId) throws Exception {
        try {
            /* ==============================================================================
            * HEADER 설정
            ============================================================================== */
            String bearerToken = Base64Util.encode(String.format("%s:%s:%s", accessToken, (new Date().getTime() / 1000), clientId));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType(MediaType.APPLICATION_JSON, Charset.forName("utf-8")));
            headers.set("Authorization", String.format("bearer %s", bearerToken));
            headers.set("client_id", clientId);
            headers.set("productID", PRODUCT_ID);


            /* ==============================================================================
            * URL 설정
            ============================================================================== */
            StringBuilder url = new StringBuilder();
            url.append(this.HOST)
                    .append(this.API_URL);
            String jsonStr = this.createMessage(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));


            /* ==============================================================================
            * API 호출
            ============================================================================== */
            ResponseEntity<String> resp = this.callApi(HttpMethod.POST, url.toString(), jsonStr, headers);
            log.info("==================================================================================");
            log.info("==== 공개키 요청 Result Info... ====");
            log.info("[Headers]: " + resp.getHeaders().toString());
            log.info("[Body]: " + resp.getBody());
            log.info("==================================================================================");



            /* ==============================================================================
            * 결과 확인
            ============================================================================== */
//            NiceCiRespDTO<DataBodyPubkeyResp> respDTO = mapper.readValue(resp.getBody(), NiceCiRespDTO.class);
            String strRespBody = resp.getBody().replace("\"\"", "null");
            NiceCiRespDTO<DataBodyPubkeyResp> respDTO = mapper.readValue(strRespBody, new TypeReference<NiceCiRespDTO<DataBodyPubkeyResp>>(){});


            return respDTO;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("공개키 요청 실패." + e.getMessage());
        }
    }


    /**
     * JSON 포맷의 요청메시지 생성
     *
     * @param reqDtim
     * @return
     * @throws JsonProcessingException
     */
    protected String createMessage(String reqDtim) throws JsonProcessingException {
        Map<String, String> dataHeader = new HashMap<>();
        dataHeader.put("CNTY_CD", "ko");  //[필수]이용언어: ko, en, cn ...
        dataHeader.put("TRAN_ID", null);  //[선택]API 통신구간에서 요청에 대한 응답을 확인하기 위한 고유번호

        Map<String, String> dataBody = new HashMap<>();
        dataBody.put("req_dtim", reqDtim);  //[필수]공개키 요청일시(YYYYMMDDHH24MISS)

        Map<String, Object> m = new HashMap<>();
        m.put("dataHeader", dataHeader);
        m.put("dataBody", dataBody);

        return mapper.writeValueAsString(m);
    }
}
