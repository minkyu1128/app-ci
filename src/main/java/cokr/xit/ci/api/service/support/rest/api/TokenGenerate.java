package cokr.xit.ci.api.service.support.rest.api;

import cokr.xit.ci.api.service.support.rest.code.NiceCiApiCd;
import cokr.xit.ci.api.service.support.rest.model.NiceCiRespDTO;
import cokr.xit.ci.api.service.support.rest.model.NiceCiRespVO;
import cokr.xit.ci.api.service.support.rest.model.conf.DataBodyGenerateTokenResp;
import cokr.xit.ci.api.service.support.rest.model.conf.DataBodyRevokeTokenResp;
import cokr.xit.ci.core.utils.DateUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Base64Util;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Slf4j
@SuperBuilder
public class TokenGenerate extends NiceCiApiAbstract {

    protected final String HOST;
    protected final String API_URL_GENERATE_TOKEN;
    protected final String API_URL_REVOKE_TOKEN;


    public NiceCiRespVO<DataBodyGenerateTokenResp> execute(String clientId, String clientSecret) throws Exception {
        /* ==============================================================================
        * 유효성 확인
        ============================================================================== */
        if (StringUtils.isEmpty(clientId))
            return NiceCiRespVO.<DataBodyGenerateTokenResp>errBuilder().errCode(NiceCiApiCd.FAIL).errMsg("클라이언트ID는 필수 입력값 입니다.").build();
        if (StringUtils.isEmpty(clientSecret))
            return NiceCiRespVO.<DataBodyGenerateTokenResp>errBuilder().errCode(NiceCiApiCd.FAIL).errMsg("클라이언트비밀번호는 필수 입력값 입니다.").build();



        /* ==============================================================================
        * API 호출
        ============================================================================== */
        NiceCiRespDTO<DataBodyGenerateTokenResp> generateResponseDTO = this.generateToken(clientId, clientSecret);


        /* ==============================================================================
        * 결과 반환
        * [토큰 재발급]
        *   -.토큰만료or폐기 상태에서 API 요청하면 GW_RSLT_CD가 1800 이 리턴되며 이때는 "token 요청"
        *   -.만료 30일 이전이면 토큰 재발급(폐기&요청)
        ============================================================================== */
        if ("1800".equals(generateResponseDTO.getDataHeader().getGW_RSLT_CD())) //결과코드가 "1800" 이면...
            generateResponseDTO = this.generateToken(clientId, clientSecret);
        if (!"1200".equals(generateResponseDTO.getDataHeader().getGW_RSLT_CD()))
            return NiceCiRespVO.<DataBodyGenerateTokenResp>errBuilder()
                    .errCode(NiceCiApiCd.valueOfEnum("HEAD_" + generateResponseDTO.getDataHeader().getGW_RSLT_CD()))
                    .errMsg(String.format("[%s]: %s", generateResponseDTO.getDataHeader().getGW_RSLT_CD(), generateResponseDTO.getDataHeader().getGW_RSLT_MSG()))
                    .build();
        if (DateUtil.secToDays(generateResponseDTO.getDataBody().getExpiresIn()) < 30) {  //토큰만료일이 30일 미만이면..
            //토큰폐기
            NiceCiRespDTO<DataBodyRevokeTokenResp> revokeResponseDTO = this.revokeToken(generateResponseDTO.getDataBody().getAccessToken(), clientId);
            if (!"1200".equals(revokeResponseDTO.getDataHeader().getGW_RSLT_CD()))
                return NiceCiRespVO.<DataBodyGenerateTokenResp>errBuilder()
                        .errCode(NiceCiApiCd.valueOfEnum("HEAD_" + generateResponseDTO.getDataHeader().getGW_RSLT_CD()))
                        .errMsg(String.format("[%s]: %s", generateResponseDTO.getDataHeader().getGW_RSLT_CD(), generateResponseDTO.getDataHeader().getGW_RSLT_MSG()))
                        .build();
            if(!revokeResponseDTO.getDataBody().getResult())
                return NiceCiRespVO.<DataBodyGenerateTokenResp>errBuilder().errCode(NiceCiApiCd.FAIL).errMsg(String.format("토큰(%s) 폐기에 실패 했습니다.", generateResponseDTO.getDataBody().getAccessToken())).build();
            //토큰요청
            generateResponseDTO = this.generateToken(clientId, clientSecret);
            if (!"1200".equals(generateResponseDTO.getDataHeader().getGW_RSLT_CD()))
                return NiceCiRespVO.<DataBodyGenerateTokenResp>errBuilder()
                        .errCode(NiceCiApiCd.valueOfEnum("HEAD_" + generateResponseDTO.getDataHeader().getGW_RSLT_CD()))
                        .errMsg(String.format("[%s]: %s", generateResponseDTO.getDataHeader().getGW_RSLT_CD(), generateResponseDTO.getDataHeader().getGW_RSLT_MSG()))
                        .build();
        }



        return NiceCiRespVO.<DataBodyGenerateTokenResp>okBuilder().resultInfo(generateResponseDTO.getDataBody()).build();
    }


    protected NiceCiRespDTO<DataBodyGenerateTokenResp> generateToken(String clientId, String clientSecret) throws Exception {
        try {


            /* ==============================================================================
            * HEADER 설정
            ============================================================================== */
            String authorizationToken = Base64Util.encode(String.format("%s:%s", clientId, clientSecret));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType(MediaType.APPLICATION_FORM_URLENCODED, Charset.forName("utf-8")));
            headers.set("Authorization", String.format("Basic %s", authorizationToken));


            /* ==============================================================================
            * URL 설정
            ============================================================================== */
            StringBuilder url = new StringBuilder();
            url.append(this.HOST)
                    .append(API_URL_GENERATE_TOKEN);
            MultiValueMap<String, String> body = this.createMessage();


            /* ==============================================================================
            * API 호출
            ============================================================================== */
            ResponseEntity<String> resp = this.callApi(HttpMethod.POST, url.toString(), body, headers);
            log.info("==================================================================================");
            log.info("==== 토큰발급 요청 Result Info... ====");
            log.info("[Headers]: " + resp.getHeaders().toString());
            log.info("[Body]: " + resp.getBody());
            log.info("==================================================================================");


            /* ==============================================================================
            * 결과 확인
            *  응답데이터 예시 => {"dataHeader":{"GW_RSLT_CD":"1200","GW_RSLT_MSG":"오류 없음"},"dataBody":{"access_token":"8c680964-eec7-485c-ad58-6534e90cc653","token_type":"bearer","expires_in":1576914752,"scope":"default"}}
            ============================================================================== */
//            NiceCiRespDTO<DataBodyGenerateTokenResp> respDTO = mapper.readValue(resp.getBody(), NiceCiRespDTO.class);
            String strRespBody = resp.getBody().replace("\"\"", "null");
            NiceCiRespDTO<DataBodyGenerateTokenResp> respDTO = mapper.readValue(strRespBody, new TypeReference<NiceCiRespDTO<DataBodyGenerateTokenResp>>(){});
            if(!(respDTO.getDataBody()==null||"".equals(respDTO.getDataBody())))
                respDTO.getDataBody().setExpiredDt(this.addSec(respDTO.getDataBody().getExpiresIn(), "yyyyMMddHHmmss"));


            return respDTO;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("토큰발급 요청 실패." + e.getMessage());
        }
    }


    protected NiceCiRespDTO<DataBodyRevokeTokenResp> revokeToken(String accessToken, String clientId) throws Exception {
        try {
            /* ==============================================================================
            * HEADER 설정
            ============================================================================== */
            String bearerToken = Base64Util.encode(String.format("%s:%s:%s", accessToken, (new Date().getTime() / 1000), clientId));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType(MediaType.APPLICATION_FORM_URLENCODED, Charset.forName("utf-8")));
            headers.set("Authorization", String.format("Basic %s", bearerToken));


            /* ==============================================================================
            * URL 설정
            ============================================================================== */
            StringBuilder url = new StringBuilder();
            url.append(this.HOST)
                    .append(API_URL_REVOKE_TOKEN);


            /* ==============================================================================
            * API 호출
            ============================================================================== */
            ResponseEntity<String> resp = this.callApi(HttpMethod.POST, url.toString(), null, headers);
            log.info("==================================================================================");
            log.info("==== 토큰폐기 요청 Result Info... ====");
            log.info("[Headers]: " + resp.getHeaders().toString());
            log.info("[Body]: " + resp.getBody());
            log.info("==================================================================================");


            /* ==============================================================================
            * 결과 확인
            ============================================================================== */
//            NiceCiRespDTO<DataBodyRevokeTokenResp> respDTO = mapper.readValue(resp.getBody(), NiceCiRespDTO.class);
            String strRespBody = resp.getBody().replace("\"\"", "null");
            NiceCiRespDTO<DataBodyRevokeTokenResp> respDTO = mapper.readValue(strRespBody, new TypeReference<NiceCiRespDTO<DataBodyRevokeTokenResp>>(){});


            return respDTO;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("토큰폐기 요청 실패." + e.getMessage());
        }
    }


    /**
     * JSON 포맷의 요청메시지 생성
     *
     * @return
     * @throws JsonProcessingException
     */
    protected MultiValueMap<String, String> createMessage() throws JsonProcessingException {
        MultiValueMap<String, String> m = new LinkedMultiValueMap<>();
        m.add("grant_type", "client_credentials");
        m.add("scope", "default");

        return m;
    }



    /**
     * 현재날짜에 초(sec)를 더한 날짜를 반환 한다.
     * @param sec
     * @param pattern
     * @return
     */
    private String addSec(Integer sec, String pattern){
        if(sec==null)
            return null;

        if("".equals(pattern)||pattern==null)
            pattern = "yyyyMMdd";

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis((sec*1000L) + (new Date().getTime()));
        Date date = calendar.getTime();

        return new SimpleDateFormat(pattern).format(date);
    }

}
