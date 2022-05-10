package cokr.xit.ci.api.service.support.rest.api;

import cokr.xit.ci.api.service.support.rest.code.NiceCiApiCd;
import cokr.xit.ci.api.service.support.rest.model.NiceCiRespDTO;
import cokr.xit.ci.api.service.support.rest.model.NiceCiRespVO;
import cokr.xit.ci.api.service.support.rest.model.conf.DataBodyCiResp;
import cokr.xit.ci.api.service.support.rest.model.conf.DataBodySymkeyResp;
import cokr.xit.ci.api.service.support.rest.model.conf.EncData;
import cokr.xit.ci.api.service.support.rest.utils.SymmetricKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Base64Util;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@SuperBuilder
public class CiRequest extends NiceCiApiAbstract {

    protected final String HOST;
    protected final String API_URL;


    public NiceCiRespVO<DataBodyCiResp> execute(String clientId, String clientSecret, String jumin, String clientIp) throws Exception {
        /* ==============================================================================
        * 유효성 확인
        ============================================================================== */
        if (StringUtils.isEmpty(clientId))
            return NiceCiRespVO.<DataBodyCiResp>errBuilder().errCode(NiceCiApiCd.FAIL).errMsg("클라이언트ID는 필수 입력값 입니다.").build();
        if (StringUtils.isEmpty(clientSecret))
            return NiceCiRespVO.<DataBodyCiResp>errBuilder().errCode(NiceCiApiCd.FAIL).errMsg("클라이언트비밀번호는 필수 입력값 입니다.").build();
        if (StringUtils.isEmpty(SymmetricKey.getInstance().getData().getKey()))
            return NiceCiRespVO.<DataBodyCiResp>errBuilder().errCode(NiceCiApiCd.FAIL).errMsg("대칭키는 필수 입력값 입니다.").build();
        if (StringUtils.isEmpty(SymmetricKey.getInstance().getData().getSymkeyStatInfo().getCurSymkeyVersion()))
            return NiceCiRespVO.<DataBodyCiResp>errBuilder().errCode(NiceCiApiCd.FAIL).errMsg("대칭키 버전은 필수 입력값 입니다.").build();
        if (StringUtils.isEmpty(SymmetricKey.getInstance().getData().getSiteCode()))
            return NiceCiRespVO.<DataBodyCiResp>errBuilder().errCode(NiceCiApiCd.FAIL).errMsg("사이트코드는 필수 입력값 입니다.").build();
        if (StringUtils.isEmpty(SymmetricKey.getInstance().getData().getRequestNo()))
            return NiceCiRespVO.<DataBodyCiResp>errBuilder().errCode(NiceCiApiCd.FAIL).errMsg("신청번호는 필수 입력값 입니다.").build();
        if (StringUtils.isEmpty(SymmetricKey.getInstance().getData().getIv()))
            return NiceCiRespVO.<DataBodyCiResp>errBuilder().errCode(NiceCiApiCd.FAIL).errMsg("IV는 필수 입력값 입니다.").build();
        if (StringUtils.isEmpty(SymmetricKey.getInstance().getData().getHmacKey()))
            return NiceCiRespVO.<DataBodyCiResp>errBuilder().errCode(NiceCiApiCd.FAIL).errMsg("HmacKey는 필수 입력값 입니다.").build();
        if (StringUtils.isEmpty(jumin))
            return NiceCiRespVO.<DataBodyCiResp>errBuilder().errCode(NiceCiApiCd.FAIL).errMsg("주민번호는 필수 입력값 입니다.").build();


        /* ==============================================================================
        * API 호출
        ============================================================================== */
        NiceCiRespDTO<DataBodyCiResp> responseDTO = this.getCi(clientId, clientSecret, SymmetricKey.getInstance().getData(), jumin, clientIp);


        /* ==============================================================================
        * 결과 반환
        ============================================================================== */
        if (!"1200".equals(responseDTO.getDataHeader().getGW_RSLT_CD()))
            return NiceCiRespVO.<DataBodyCiResp>errBuilder()
                    .errCode(NiceCiApiCd.valueOfEnum("HEAD_" + responseDTO.getDataHeader().getGW_RSLT_CD()))
                    .errMsg(String.format("[%s]: %s", responseDTO.getDataHeader().getGW_RSLT_CD(), responseDTO.getDataHeader().getGW_RSLT_MSG()))
                    .build();
        if (!"P000".equals(responseDTO.getDataBody().getRspCd()))
            return NiceCiRespVO.<DataBodyCiResp>errBuilder()
                    .errCode(NiceCiApiCd.valueOfEnum(responseDTO.getDataBody().getRspCd()))
                    .errMsg(String.format("[%s]: %s. %s", responseDTO.getDataBody().getRspCd(), NiceCiApiCd.valueOfEnum(responseDTO.getDataBody().getRspCd()).getCodeNm(), responseDTO.getDataBody().getResMsg()))
                    .build();
        if (!"0000".equals(responseDTO.getDataBody().getResultCd()))
            return NiceCiRespVO.<DataBodyCiResp>errBuilder()
                    .errCode(NiceCiApiCd.valueOfEnum("CI_" + responseDTO.getDataBody().getResultCd()))
                    .errMsg(String.format("[%s]: %s", responseDTO.getDataBody().getResultCd(), NiceCiApiCd.valueOfEnum("CI_" + responseDTO.getDataBody().getResultCd()).getCodeNm()))
                    .build();


        /* ==============================================================================
        * 무결성 확인
        ============================================================================== */
        //enc_data와 intergrity_value 비교
        if (!responseDTO.getDataBody().getIntegrityValue().equals(createIntegrityValue(responseDTO.getDataBody().getEncData(), SymmetricKey.getInstance().getData().getHmacKey())))
            return NiceCiRespVO.<DataBodyCiResp>errBuilder()
                    .errCode(NiceCiApiCd.FAIL)
                    .errMsg("응답데이터 무결성 체크 오류")
                    .build();
        /* ==============================================================================
        * 데이터 복호화
        ============================================================================== */
        String decEncDataStr = this.decEncData(responseDTO.getDataBody().getEncData(), SymmetricKey.getInstance().getData().getKey(), SymmetricKey.getInstance().getData().getIv());
        responseDTO.getDataBody().setDecEncData(mapper.readValue(decEncDataStr, EncData.class));


        return NiceCiRespVO.<DataBodyCiResp>okBuilder().resultInfo(responseDTO.getDataBody()).build();

    }


    protected NiceCiRespDTO<DataBodyCiResp> getCi(String clientId, String clientSecret, DataBodySymkeyResp symkeyResp, String jumin, String clientIp) throws Exception {
        try {


            /* ==============================================================================
            * HEADER 설정
            ============================================================================== */
//            String authorizationToken = Base64.getEncoder().encodeToString(String.format("%s:%s", clientId, clientSecret).getBytes(StandardCharsets.UTF_8));
            String authorizationToken = Base64Util.encode(String.format("%s:%s", clientId, clientSecret));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType(MediaType.APPLICATION_JSON, Charset.forName("utf-8")));
            headers.set("Authorization", String.format("Basic %s", authorizationToken));
            headers.set("productID", PRODUCT_ID);


            /* ==============================================================================
            * URL 설정
            ============================================================================== */
            StringBuilder url = new StringBuilder();
            url.append(this.HOST)
                    .append(this.API_URL);
            final String strEncData = this.createEncData(symkeyResp.getSiteCode(), jumin, symkeyResp.getRequestNo(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")), clientIp);
            final String encData = this.encEncData(strEncData, symkeyResp.getKey(), symkeyResp.getIv());
            final String integrityValue = this.createIntegrityValue(encData, symkeyResp.getHmacKey());
            final String jsonStr = this.createMessage(symkeyResp.getSymkeyStatInfo().getCurSymkeyVersion(), encData, integrityValue);


            /* ==============================================================================
            * API 호출
            ============================================================================== */
            ResponseEntity<String> resp = this.callApi(HttpMethod.POST, url.toString(), jsonStr, headers);
            log.info("==================================================================================");
            log.info("==== 아이핀 CI 요청 Info... ====");
            log.info("[Body]: " + jsonStr);
            log.info("[encData]: " + strEncData);
            log.info("==== 아이핀 CI 요청 Result Info... ====");
            log.info("[Headers]: " + resp.getHeaders().toString());
            log.info("[Body]: " + resp.getBody());
            log.info("==================================================================================");



            /* ==============================================================================
            * 결과 확인
            ============================================================================== */
            String strRespBody = resp.getBody().replace("\"\"", "null");
            NiceCiRespDTO<DataBodyCiResp> respDTO = mapper.readValue(strRespBody, new TypeReference<NiceCiRespDTO<DataBodyCiResp>>(){});


            return respDTO;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("아이핀 CI 요청 실패." + e.getMessage());
        }


    }


    /**
     * JSON 포맷의 요청메시지 생성
     *
     * @param symkeyVersion
     * @param encData
     * @param integrityValue
     * @return
     * @throws JsonProcessingException
     */
    protected String createMessage(String symkeyVersion, String encData, String integrityValue) throws JsonProcessingException {
        Map<String, String> dataHeader = new HashMap<>();
        dataHeader.put("CNTY_CD", "ko");  //[필수]이용언어: ko, en, cn ...
        dataHeader.put("TRAN_ID", null);  //[선택]API 통신구간에서 요청에 대한 응답을 확인하기 위한 고유번호

        Map<String, String> dataBody = new HashMap<>();
        dataBody.put("symkey_version", symkeyVersion);   //[선택]대칭키 버전
        dataBody.put("enc_data", encData);               //[필수]JSON암호화 값
        dataBody.put("integrity_value", integrityValue); //[선택]무결성체크를 위해 enc_data를 HMAC처리 후, Base64 인코딩한 값

        Map<String, Object> m = new HashMap<>();
        m.put("dataHeader", dataHeader);
        m.put("dataBody", dataBody);

        return mapper.writeValueAsString(m);
    }

    private String createEncData(String siteCode, String jid, String reqNo, String reqDtim, String clientIp) throws JsonProcessingException {
        Map<String, String> m = new HashMap<>();
        m.put("site_code", siteCode);                                    //[필수]사이트코드(공개키요청시 수신한 사이트코드)
        m.put("info_req_type", "1");                                     //[필수]정보요청유형(1: CI제공)
        m.put("jumin_id", jid.replaceAll("[^0-9]", "")); //[필수]주민등록번호(13자리)
        m.put("req_no", reqNo);                                          //[필수]이용기관에서 서비스에 대한 요청거래를 확인하기 위한 고유값
        m.put("req_dtim", reqDtim);                                      //[필수]거래요청시간(YYYYYMMDDHH24MISS)
        m.put("client_ip", clientIp);                                    //[선택]서비스 이용 사용자 IP

        return mapper.writeValueAsString(m);


    }

    private String encEncData(String encData, String symkey, String iv) throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {

        // 암호화
        SecretKey secureKey = new SecretKeySpec(symkey.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secureKey, new IvParameterSpec(iv.getBytes()));
        byte[] encrypted = cipher.doFinal(encData.trim().getBytes());

        // Base64 인코딩
        String reqDataEnc = Base64Utils.encodeToString(encrypted);

        return reqDataEnc;
    }


    private String decEncData(String encData, String symkey, String iv) throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {

        // Base64 디코딩
        byte[] respDataEnc = Base64Utils.decode(encData.getBytes());

        // 복호화
        SecretKey secureKey = new SecretKeySpec(symkey.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secureKey, new IvParameterSpec(iv.getBytes()));
        byte[] decrypted = cipher.doFinal(respDataEnc);
        String strDecrypted = new String(decrypted);

        return strDecrypted;
    }


    /**
     * Hmac 무결성체크값(integrity_value) 생성
     *
     * @param encData
     * @param hmacKey
     * @return
     */
    private String createIntegrityValue(String encData, String hmacKey) {
        byte[] hmacSha256 = hmac256(hmacKey.getBytes(), encData.getBytes());
        String integrityValue = Base64.getEncoder().encodeToString(hmacSha256);

        return integrityValue;
    }

    private byte[] hmac256(byte[] secretKey, byte[] message) {
        byte[] hmac256 = null;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec sks = new SecretKeySpec(secretKey, "HmacSHA256");
            mac.init(sks);
            hmac256 = mac.doFinal(message);
            return hmac256;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMACSHA256 encrypt");
        }
    }


}

