package cokr.xit.ci.api.service.support.rest;

import cokr.xit.ci.api.code.ErrCd;
import cokr.xit.ci.api.model.ResponseVO;
import cokr.xit.ci.api.service.support.rest.code.NiceCiApiCd;
import cokr.xit.ci.api.service.support.rest.model.*;
import cokr.xit.ci.core.utils.DateUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Base64Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
//public class NiceCiApi implements NiceCiApiStruct {
public class NiceCiApi {

    private final String PRODUCT_ID = "2101466024";
    @Value("${nice.ci.api.host}")
    private String HOST;
    @Value("${nice.ci.api.generate-token}")
    private String API_GENERATE_TOKEN;
    @Value("${nice.ci.api.revoke-token}")
    private String API_REVOKE_TOKEN;
    @Value("${nice.ci.api.publickey}")
    private String API_PUBLICKEY;
    @Value("${nice.ci.api.symmetrickey}")
    private String API_SYMMETRICKEY;
    @Value("${nice.ci.api.ci}")
    private String API_CI;


    private Gson gson = new GsonBuilder().registerTypeAdapter(Map.class, new MapDeserailizer()).disableHtmlEscaping().create();

    //@Override
    public ResponseVO<GenerateTokenRespDTO> generateToken(String clientId, String clientSecret) {
        ErrCd errCd = ErrCd.ERR999;
        String errMsg = ErrCd.ERR999.getCodeNm();
        try {
            /* ==============================================================================
            * 유효성 확인
            ============================================================================== */
            if (StringUtils.isEmpty(clientId)) {
                errCd = ErrCd.ERR401;
                errMsg = "클라이언트ID는 필수 입력값 입니다.";
                throw new RuntimeException("유효성 검증 실패.");
            }
            if (StringUtils.isEmpty(clientSecret)) {
                errCd = ErrCd.ERR401;
                errMsg = "클라이언트비밀번호는 필수 입력값 입니다.";
                throw new RuntimeException("유효성 검증 실패.");
            }


            /* ==============================================================================
            * HEADER 설정
            ============================================================================== */
            String authorizationToken = Base64Util.encode(String.format("%s:%s", clientId, clientSecret));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType(MediaType.APPLICATION_JSON, Charset.forName("utf-8")));
            headers.set("Authorization", String.format("Basic %s", authorizationToken));


            /* ==============================================================================
            * URL 설정
            ============================================================================== */
            StringBuilder url = new StringBuilder();
            url.append(this.HOST)
                    .append(API_GENERATE_TOKEN);
            Map<String, String> mParam = new HashMap<>();
            mParam.put("grant_type", "client_credentials");
            mParam.put("scope", "default");
            String jsonStr = gson.toJson(mParam);


            /* ==============================================================================
            * API 호출
            ============================================================================== */
            ResponseEntity<String> resp = this.callApi(HttpMethod.POST, url.toString(), jsonStr, headers);
            log.info("==================================================================================");
            log.info("==== 토큰 생성 ====");
            log.info("[Headers]: " + resp.getHeaders().toString());
            log.info("[Body]: " + resp.getBody());
            log.info("==================================================================================");



            /* ==============================================================================
            * 결과 확인
            ============================================================================== */
            if (!"1200".equals(resp.getHeaders().get("GW_RSLT_CD").get(0))) {
                errCd = ErrCd.ERR620;
                errMsg = String.format("[%s] %s.%s", resp.getHeaders().get("GW_RSLT_CD").get(0), resp.getHeaders().get("GW_RSLT_MSG").get(0), resp.getBody());
                throw new RuntimeException("응답 오류(GW_RSLT_CD) 입니다.");
            }
//            Map<String, Object> resultInfo = gson.fromJson(resp.getBody(), Map.class);
//            String accessToken = (String) resultInfo.get("access_token"); //사용자 엑세스 토큰 값(모든 API 요청시 헤더에 access_token을 포함하여 전송)
//            Long expiresIn = (Long) resultInfo.get("expires_in");         //access token 만료까지 남은시간(초)
//            String tokenType = (String) resultInfo.get("token_type");     //bearer로 고정
//            String scope = (String) resultInfo.get("scope");              //요청한 scope값(기본 default)
//            String expiredDt = DateUtil.absTimeSecToDate(expiresIn, "yyyyMMddHHmmss");
//            log.info("[엑세스토큰]: " + accessToken);
//            log.info("[엑세스토큰 만료시간]: " + expiredDt);
            GenerateTokenRespDTO resultInfo = gson.fromJson(resp.getBody(), GenerateTokenRespDTO.class);
            resultInfo.setExpiredDt(DateUtil.absTimeSecToDate(resultInfo.getExpiresIn(), "yyyyMMddHHmmss"));
            log.info("[응답데이터]: " + resultInfo.toString());


            return ResponseVO.<GenerateTokenRespDTO>builder().errCode(ErrCd.OK).errMsg(ErrCd.OK.getCodeNm()).resultInfo(resultInfo).build();
        } catch (Exception e) {
            return ResponseVO.<GenerateTokenRespDTO>builder().errCode(errCd).errMsg(errMsg + " " + errMsg).build();
        }
    }


    //@Override
    public ResponseVO<RevokeTokenRespDTO> revokeToken(String accessToken, String clientId) {
        ErrCd errCd = ErrCd.ERR999;
        String errMsg = ErrCd.ERR999.getCodeNm();
        try {
            /* ==============================================================================
            * 유효성 확인
            ============================================================================== */
            if (StringUtils.isEmpty(accessToken)) {
                errCd = ErrCd.ERR401;
                errMsg = "엑세스토큰은 필수 입력값 입니다.";
                throw new RuntimeException("유효성 검증 실패.");
            }
            if (StringUtils.isEmpty(clientId)) {
                errCd = ErrCd.ERR401;
                errMsg = "클라이언트ID는 필수 입력값 입니다.";
                throw new RuntimeException("유효성 검증 실패.");
            }


            /* ==============================================================================
            * HEADER 설정
            ============================================================================== */
            String bearerToken = Base64Util.encode(String.format("%s:%s:%s", accessToken, (new Date().getTime() / 1000), clientId));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType(MediaType.APPLICATION_JSON, Charset.forName("utf-8")));
            headers.set("Authorization", String.format("bearer %s", bearerToken));


            /* ==============================================================================
            * URL 설정
            ============================================================================== */
            StringBuilder url = new StringBuilder();
            url.append(this.HOST)
                    .append(API_REVOKE_TOKEN);


            /* ==============================================================================
            * API 호출
            ============================================================================== */
            ResponseEntity<String> resp = this.callApi(HttpMethod.POST, url.toString(), null, headers);
            log.info("==================================================================================");
            log.info("==== 토큰 폐기 ====");
            log.info("[Headers]: " + resp.getHeaders().toString());
            log.info("[Body]: " + resp.getBody());
            log.info("==================================================================================");



            /* ==============================================================================
            * 결과 확인
            ============================================================================== */
            if (!"1200".equals(resp.getHeaders().get("GW_RSLT_CD").get(0))) {
                errCd = ErrCd.ERR620;
                errMsg = String.format("[%s] %s.%s", resp.getHeaders().get("GW_RSLT_CD").get(0), resp.getHeaders().get("GW_RSLT_MSG").get(0), resp.getBody());
                throw new RuntimeException("응답 오류 입니다. ");
            }
            RevokeTokenRespDTO resultInfo = gson.fromJson(resp.getBody(), RevokeTokenRespDTO.class);
            log.info("[응답데이터]: " + resultInfo.toString());


            return ResponseVO.<RevokeTokenRespDTO>builder().errCode(ErrCd.OK).errMsg(ErrCd.OK.getCodeNm()).resultInfo(resultInfo).build();
        } catch (Exception e) {
            return ResponseVO.<RevokeTokenRespDTO>builder().errCode(errCd).errMsg(e.getMessage() + " " + errMsg).build();
        }
    }


    //@Override
    public ResponseVO<PublickeyRespDTO> generatePublickey(String accessToken, String clientId, String pubkeyVersion, String symkeyRegInfo) {
        ErrCd errCd = ErrCd.ERR999;
        String errMsg = ErrCd.ERR999.getCodeNm();
        try {
            /* ==============================================================================
            * 유효성 확인
            ============================================================================== */
            if (StringUtils.isEmpty(accessToken)) {
                errCd = ErrCd.ERR401;
                errMsg = "엑세스토큰은 필수 입력값 입니다.";
                throw new RuntimeException("유효성 검증 실패.");
            }
            if (StringUtils.isEmpty(clientId)) {
                errCd = ErrCd.ERR401;
                errMsg = "클라이언트ID는 필수 입력값 입니다.";
                throw new RuntimeException("유효성 검증 실패.");
            }
            if (StringUtils.isEmpty(pubkeyVersion)) {
                errCd = ErrCd.ERR401;
                errMsg = "공개키버전은 필수 입력값 입니다.";
                throw new RuntimeException("유효성 검증 실패.");
            }
            if (StringUtils.isEmpty(symkeyRegInfo)) {
                errCd = ErrCd.ERR401;
                errMsg = "공개키암호화 값(대칭키를 공개키로 암호화)은 필수 입력값 입니다.";
                throw new RuntimeException("유효성 검증 실패.");
            }


            /* ==============================================================================
            * HEADER 설정
            ============================================================================== */
            String bearerToken = Base64Util.encode(String.format("%s:%s:%s", accessToken, (new Date().getTime() / 1000), clientId));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType(MediaType.APPLICATION_JSON, Charset.forName("utf-8")));
            headers.set("Authorization", String.format("bearer %s", bearerToken));
            headers.set("client_id", clientId);
            headers.set("productID", PRODUCT_ID);
            headers.set("CNTY_CD", "ko");   //이용언어: ko, en, cn...
//            headers.set("TRAN_ID", );       //API통신구간에서 요청에 대한 응답을 확인하기 위한 고유번호


            /* ==============================================================================
            * URL 설정
            ============================================================================== */
            StringBuilder url = new StringBuilder();
            url.append(this.HOST)
                    .append(API_PUBLICKEY);
            Map<String, String> mParam = new HashMap<>();
            mParam.put("req_dtim", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            String jsonStr = gson.toJson(mParam);


            /* ==============================================================================
            * API 호출
            ============================================================================== */
            ResponseEntity<String> resp = this.callApi(HttpMethod.POST, url.toString(), jsonStr, headers);
            log.info("==================================================================================");
            log.info("==== 공개키 요청 ====");
            log.info("[Headers]: " + resp.getHeaders().toString());
            log.info("[Body]: " + resp.getBody());
            log.info("==================================================================================");



            /* ==============================================================================
            * 결과 확인
            ============================================================================== */
            if (!"1200".equals(resp.getHeaders().get("GW_RSLT_CD").get(0))) {
                errCd = ErrCd.ERR620;
                errMsg = String.format("[%s] %s.%s", resp.getHeaders().get("GW_RSLT_CD").get(0), resp.getHeaders().get("GW_RSLT_MSG").get(0), resp.getBody());
                throw new RuntimeException("응답 오류(GW_RSLT_CD) 입니다.");
            }
            PublickeyRespDTO resultInfo = gson.fromJson(resp.getBody(), PublickeyRespDTO.class);
            log.info("[응답데이터]: " + resultInfo.toString());
            if (!"P000".equals(resultInfo.getRspCd())) {
                errCd = ErrCd.ERR601;
                errMsg = String.format("[%s]%s", resultInfo.getRspCd(), NiceCiApiCd.valueOfEnum(resultInfo.getRspCd()).getCodeNm());
                throw new RuntimeException("응답코드(rsp_cd) 오류.");
            }
            if (!"0000".equals(resultInfo.getResultCd())) {
                errCd = ErrCd.ERR601;
                errMsg = String.format("[%s]%s", resultInfo.getResultCd(), NiceCiApiCd.valueOfEnum("DRSLT_" + resultInfo.getResultCd()).getCodeNm());
                throw new RuntimeException("상세결과코드(result_cd) 오류.");
            }


            return ResponseVO.<PublickeyRespDTO>builder().errCode(ErrCd.OK).errMsg(ErrCd.OK.getCodeNm()).resultInfo(resultInfo).build();
        } catch (Exception e) {
            return ResponseVO.<PublickeyRespDTO>builder().errCode(errCd).errMsg(e.getMessage() + " " + errMsg).build();
        }
    }


    //@Override
    public ResponseVO<SymmetrickeyRespDTO> generateSymmetrickey(String accessToken, String clientId, String pubkeyVersion, String symkeyRegInfo) {
        ErrCd errCd = ErrCd.ERR999;
        String errMsg = ErrCd.ERR999.getCodeNm();
        try {
            /* ==============================================================================
            * 유효성 확인
            ============================================================================== */
            if (StringUtils.isEmpty(accessToken)) {
                errCd = ErrCd.ERR401;
                errMsg = "엑세스토큰은 필수 입력값 입니다.";
                throw new RuntimeException("유효성 검증 실패.");
            }
            if (StringUtils.isEmpty(clientId)) {
                errCd = ErrCd.ERR401;
                errMsg = "클라이언트ID는 필수 입력값 입니다.";
                throw new RuntimeException("유효성 검증 실패.");
            }
            if (StringUtils.isEmpty(pubkeyVersion)) {
                errCd = ErrCd.ERR401;
                errMsg = "공개키버전은 필수 입력값 입니다.";
                throw new RuntimeException("유효성 검증 실패.");
            }
            if (StringUtils.isEmpty(symkeyRegInfo)) {
                errCd = ErrCd.ERR401;
                errMsg = "JSON암호화값은 필수 입력값 입니다.";
                throw new RuntimeException("유효성 검증 실패.");
            }


            /* ==============================================================================
            * HEADER 설정
            ============================================================================== */
            String bearerToken = Base64Util.encode(String.format("%s:%s:%s", accessToken, (new Date().getTime() / 1000), clientId));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType(MediaType.APPLICATION_JSON, Charset.forName("utf-8")));
            headers.set("Authorization", String.format("bearer %s", bearerToken));
            headers.set("client_id", clientId);
            headers.set("productID", PRODUCT_ID);
            headers.set("CNTY_CD", "ko");   //이용언어: ko, en, cn...
//            headers.set("TRAN_ID", );       //API통신구간에서 요청에 대한 응답을 확인하기 위한 고유번호


            /* ==============================================================================
            * URL 설정
            ============================================================================== */
            StringBuilder url = new StringBuilder();
            url.append(this.HOST)
                    .append(API_PUBLICKEY);
            Map<String, String> mParam = new HashMap<>();
            mParam.put("pubkey_version", pubkeyVersion);
            mParam.put("symkey_reg_info", symkeyRegInfo);
            String jsonStr = gson.toJson(mParam);


            /* ==============================================================================
            * API 호출
            ============================================================================== */
            ResponseEntity<String> resp = this.callApi(HttpMethod.POST, url.toString(), jsonStr, headers);
            log.info("==================================================================================");
            log.info("==== 대칭키 요청 ====");
            log.info("[Headers]: " + resp.getHeaders().toString());
            log.info("[Body]: " + resp.getBody());
            log.info("==================================================================================");



            /* ==============================================================================
            * 결과 확인
            ============================================================================== */
            if (!"1200".equals(resp.getHeaders().get("GW_RSLT_CD").get(0))) {
                errCd = ErrCd.ERR620;
                errMsg = String.format("[%s] %s.%s", resp.getHeaders().get("GW_RSLT_CD").get(0), resp.getHeaders().get("GW_RSLT_MSG").get(0), resp.getBody());
                throw new RuntimeException("API 응답 오류 입니다.");
            }
            SymmetrickeyRespDTO resultInfo = gson.fromJson(resp.getBody(), SymmetrickeyRespDTO.class);
            log.info("[응답데이터]: " + resultInfo.toString());
            if (!"P000".equals(resultInfo.getRspCd())) {
                errCd = ErrCd.ERR601;
                errMsg = String.format("[%s]%s", resultInfo.getRspCd(), NiceCiApiCd.valueOfEnum(resultInfo.getRspCd()).getCodeNm());
                throw new RuntimeException("응답코드(rsp_cd) 오류.");
            }
            if (!"0000".equals(resultInfo.getResultCd())) {
                errCd = ErrCd.ERR601;
                errMsg = String.format("[%s]%s", resultInfo.getResultCd(), NiceCiApiCd.valueOfEnum("DRSLT_" + resultInfo.getResultCd()).getCodeNm());
                throw new RuntimeException("상세결과코드(result_cd) 오류.");
            }


            return ResponseVO.<SymmetrickeyRespDTO>builder().errCode(ErrCd.OK).errMsg(ErrCd.OK.getCodeNm()).resultInfo(resultInfo).build();
        } catch (Exception e) {
            return ResponseVO.<SymmetrickeyRespDTO>builder().errCode(errCd).errMsg(e.getMessage() + " " + errMsg).build();
        }
    }


    //@Override
    public ResponseVO<CiRespDTO> getCi(String clientId, String clientSecret, String symkeyVersion, String encData, String integrityValue) {
        ErrCd errCd = ErrCd.ERR999;
        String errMsg = ErrCd.ERR999.getCodeNm();
        try {
            /* ==============================================================================
            * 유효성 확인
            ============================================================================== */
            if (StringUtils.isEmpty(clientId)) {
                errCd = ErrCd.ERR401;
                errMsg = "클라이언트ID는 필수 입력값 입니다.";
                throw new RuntimeException("유효성 검증 실패.");
            }
            if (StringUtils.isEmpty(clientSecret)) {
                errCd = ErrCd.ERR401;
                errMsg = "클라이언트비밀번호는 필수 입력값 입니다.";
                throw new RuntimeException("유효성 검증 실패.");
            }
            if (StringUtils.isEmpty(symkeyVersion)) {
                errCd = ErrCd.ERR401;
                errMsg = "대칭키 버전 필수 입력값 입니다.";
                throw new RuntimeException("유효성 검증 실패.");
            }
            if (StringUtils.isEmpty(encData)) {
                errCd = ErrCd.ERR401;
                errMsg = "JSON암호화 값은 필수 입력값 입니다.";
                throw new RuntimeException("유효성 검증 실패.");
            }
            if (StringUtils.isEmpty(integrityValue)) {
                errCd = ErrCd.ERR401;
                errMsg = "무결성 체크 값은 필수 입력값 입니다.";
                throw new RuntimeException("유효성 검증 실패.");
            }


            /* ==============================================================================
            * HEADER 설정
            ============================================================================== */
//            String authorizationToken = Base64.getEncoder().encodeToString(String.format("%s:%s", clientId, clientSecret).getBytes(StandardCharsets.UTF_8));
            String authorizationToken = Base64Util.encode(String.format("%s:%s", clientId, clientSecret));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType(MediaType.APPLICATION_JSON, Charset.forName("utf-8")));
            headers.set("Authorization", String.format("Basic %s", authorizationToken));
            headers.set("productID", PRODUCT_ID);
            headers.set("CNTY_CD", "ko");   //이용언어: ko, en, cn...
//            headers.set("TRAN_ID", );       //API통신구간에서 요청에 대한 응답을 확인하기 위한 고유번호


            /* ==============================================================================
            * URL 설정
            ============================================================================== */
            StringBuilder url = new StringBuilder();
            url.append(this.HOST)
                    .append(API_CI);
            Map<String, String> mParam = new HashMap<>();
            mParam.put("symkey_version", symkeyVersion);
            mParam.put("enc_data", encData);
            mParam.put("integrity_value", integrityValue);
            String jsonStr = gson.toJson(mParam);
            
            
            /* ==============================================================================
            * API 호출
            ============================================================================== */
            ResponseEntity<String> resp = this.callApi(HttpMethod.POST, url.toString(), jsonStr, headers);
            log.info("==================================================================================");
            log.info("==== CI조회 ====");
            log.info("[Headers]: " + resp.getHeaders().toString());
            log.info("[Body]: " + resp.getBody());
            log.info("==================================================================================");



            /* ==============================================================================
            * 결과 확인
            ============================================================================== */
            if (!"1200".equals(resp.getHeaders().get("GW_RSLT_CD").get(0))) {
                errCd = ErrCd.ERR620;
                errMsg = String.format("[%s] %s.%s", resp.getHeaders().get("GW_RSLT_CD").get(0), resp.getHeaders().get("GW_RSLT_MSG").get(0), resp.getBody());
                throw new RuntimeException("응답 오류(GW_RSLT_CD) 입니다.");
            }
            CiRespDTO resultInfo = gson.fromJson(resp.getBody(), CiRespDTO.class);
            log.info("[응답데이터]: " + resultInfo.toString());


            return ResponseVO.<CiRespDTO>builder().errCode(ErrCd.OK).errMsg(ErrCd.OK.getCodeNm()).resultInfo(resultInfo).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.<CiRespDTO>builder().errCode(errCd).errMsg(e.getMessage() + " " + errMsg).build();
        }


    }


    /**
     * <pre>메소드 설명: API 호출
     * </pre>
     *
     * @param method
     * @param url
     * @param body
     * @param headers
     * @return ResponseEntity 요청처리 후 응답객체
     * @author: 박민규
     */
    private ResponseEntity<String> callApi(HttpMethod method, String url, String body, HttpHeaders headers) {
        log.debug("param =======================");
        log.debug(body);


        ResponseEntity<String> responseEntity = null;
        try {
            //uri 및 entity(param) 설정
            HttpEntity<?> entity = null;
            UriComponents uri = null;
            switch (method) {
                case GET:
                    entity = new HttpEntity<>(headers);
                    uri = UriComponentsBuilder
                            .fromHttpUrl(String.format("%s?%s", url, body == null ? "" : body))
//							.encode(StandardCharsets.UTF_8)	//"%"기호가 "%25"로 인코딩 발생하여 주석처리 함.
                            .build(false);
                    break;
                case POST:
                    entity = new HttpEntity<>(body, headers);
                    uri = UriComponentsBuilder
                            .fromHttpUrl(url)
                            .encode(StandardCharsets.UTF_8)
                            .build();
                    break;

                default:
                    break;
            }


            //api 호출
            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
            factory.setConnectTimeout(3000); //커넥션타임아웃 설정 3초
            factory.setReadTimeout(3000);//타임아웃 설정 3초
            RestTemplate restTemplate = new RestTemplate(factory);
            System.out.println("  url => " + uri.toString());
            System.out.println("  method => " + method);
            System.out.println("  headers => " + entity.getHeaders().toString());
            System.out.println("  body => " + entity.getBody());
            responseEntity = restTemplate.exchange(URI.create(uri.toString()), method, entity, String.class);  //이 한줄의 코드로 API를 호출해 String타입으로 전달 받는다.

            /*
             * HttpStatus 정보 확인 방법
             * 	-.코드: responseEntity.getStatusCodeValue()
             * 	-.메시지: responseEntity.getStatusCode()
             */

        } catch (HttpServerErrorException e) {
            responseEntity = new ResponseEntity<String>(e.getResponseBodyAsString(), e.getStatusCode());
            log.error(String.format("call API 서버오류[url =>%s param => %s error => %s]", url, body, e.getMessage()));
        } catch (HttpClientErrorException e) {
            responseEntity = new ResponseEntity<String>(e.getResponseBodyAsString(), e.getStatusCode());
            log.error(String.format("call API 클라이언트오류[url =>%s param => %s error => %s]", url, body, e.getMessage()));
        } catch (RestClientException e) {    //timeout 발생 또는 기타 오류...
            responseEntity = new ResponseEntity<String>(e.getMessage(), HttpStatus.REQUEST_TIMEOUT);
            log.error(String.format("RestAPI 호출 오류[url =>%s param => %s error => %s]", url, body, e.getMessage()));
        } catch (Exception e) {
            responseEntity = new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            log.error(String.format("call API 기타오류[url =>%s param => %s error => %s]", url, body, e.getMessage()));
        }

        //결과 응답
        return responseEntity;
    }

}
