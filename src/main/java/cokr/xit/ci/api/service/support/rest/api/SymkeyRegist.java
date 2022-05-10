package cokr.xit.ci.api.service.support.rest.api;

import cokr.xit.ci.api.service.support.rest.code.NiceCiApiCd;
import cokr.xit.ci.api.service.support.rest.model.NiceCiRespDTO;
import cokr.xit.ci.api.service.support.rest.model.NiceCiRespVO;
import cokr.xit.ci.api.service.support.rest.model.conf.DataBodySymkeyResp;
import cokr.xit.ci.api.service.support.rest.utils.PublicKey;
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
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@SuperBuilder
public class SymkeyRegist extends NiceCiApiAbstract {

    protected final String HOST;
    protected final String API_URL;


    public NiceCiRespVO<DataBodySymkeyResp> execute(String clientId) throws Exception {
        /* ==============================================================================
        * 유효성 확인
        ============================================================================== */
        if (StringUtils.isEmpty(Token.getInstance().getData().getAccessToken()))
            return NiceCiRespVO.<DataBodySymkeyResp>errBuilder().errCode(NiceCiApiCd.FAIL).errMsg("엑세스토큰은 필수 입력값 입니다.").build();
        if (StringUtils.isEmpty(clientId))
            return NiceCiRespVO.<DataBodySymkeyResp>errBuilder().errCode(NiceCiApiCd.FAIL).errMsg("클라이언트ID는 필수 입력값 입니다.").build();
        if (StringUtils.isEmpty(PublicKey.getInstance().getData().getSiteCode()))
            return NiceCiRespVO.<DataBodySymkeyResp>errBuilder().errCode(NiceCiApiCd.FAIL).errMsg("사이트코드는 필수 입력값 입니다.").build();
        if (StringUtils.isEmpty(PublicKey.getInstance().getData().getPublicKey()))
            return NiceCiRespVO.<DataBodySymkeyResp>errBuilder().errCode(NiceCiApiCd.FAIL).errMsg("공개키는 필수 입력값 입니다.").build();
        if (StringUtils.isEmpty(PublicKey.getInstance().getData().getKeyVersion()))
            return NiceCiRespVO.<DataBodySymkeyResp>errBuilder().errCode(NiceCiApiCd.FAIL).errMsg("공개키버전은 필수 입력값 입니다.").build();


        /* ==============================================================================
        * API 호출
        ============================================================================== */
        NiceCiRespDTO<DataBodySymkeyResp> responseDTO = this.generateSymmetrickey(Token.getInstance().getData().getAccessToken(), clientId, PublicKey.getInstance().getData().getSiteCode(), PublicKey.getInstance().getData().getPublicKey(), PublicKey.getInstance().getData().getKeyVersion());


        /* ==============================================================================
        * 결과 반환
        ============================================================================== */
        if (!"1200".equals(responseDTO.getDataHeader().getGW_RSLT_CD()))
            return NiceCiRespVO.<DataBodySymkeyResp>errBuilder()
                    .errCode(NiceCiApiCd.valueOfEnum("HEAD_" + responseDTO.getDataHeader().getGW_RSLT_CD()))
                    .errMsg(String.format("[%s]: %s", responseDTO.getDataHeader().getGW_RSLT_CD(), responseDTO.getDataHeader().getGW_RSLT_MSG()))
                    .build();
        if (!"P000".equals(responseDTO.getDataBody().getRspCd()))
            return NiceCiRespVO.<DataBodySymkeyResp>errBuilder()
                    .errCode(NiceCiApiCd.valueOfEnum(responseDTO.getDataBody().getRspCd()))
                    .errMsg(String.format("[%s]: %s. %s", responseDTO.getDataBody().getRspCd(), NiceCiApiCd.valueOfEnum(responseDTO.getDataBody().getRspCd()).getCodeNm(), responseDTO.getDataBody().getResMsg()))
                    .build();
        if (!"0000".equals(responseDTO.getDataBody().getResultCd()))
            return NiceCiRespVO.<DataBodySymkeyResp>errBuilder()
                    .errCode(NiceCiApiCd.valueOfEnum("SYMKEY_" + responseDTO.getDataBody().getResultCd()))
                    .errMsg(String.format("[%s]: %s", responseDTO.getDataBody().getResultCd(), NiceCiApiCd.valueOfEnum("SYMKEY_" + responseDTO.getDataBody().getResultCd()).getCodeNm()))
                    .build();


        return NiceCiRespVO.<DataBodySymkeyResp>okBuilder().resultInfo(responseDTO.getDataBody()).build();

    }


    protected NiceCiRespDTO<DataBodySymkeyResp> generateSymmetrickey(String accessToken, String clientId, String siteCode, String publicKey, String pubkeyVersion) throws Exception {
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
            final String requestNo = createRequestNo();
            final String key = this.createKey();
            final String iv = this.createIv();
            final String hmacKey = this.createHmacKey();
            final String strSymkeyRegInfo = this.createSymkeyRegInfo(siteCode, requestNo, key, iv, hmacKey);
            final String jsonStr = this.createMessage(pubkeyVersion, encSymkeyRegInfo(strSymkeyRegInfo, publicKey));



            /* ==============================================================================
            * API 호출
            ============================================================================== */
            ResponseEntity<String> resp = this.callApi(HttpMethod.POST, url.toString(), jsonStr, headers);
            log.info("==================================================================================");
            log.info("==== 대칭키 등록 요청 Info... ====");
            log.info("[Body]: " + jsonStr);
            log.info("[symkeyRegInfo]: " + strSymkeyRegInfo);
            log.info("==== 대칭키 등록 요청 Result Info... ====");
            log.info("[Headers]: " + resp.getHeaders().toString());
            log.info("[Body]: " + resp.getBody());
            log.info("==================================================================================");



            /* ==============================================================================
            * 결과 확인
            ============================================================================== */
//            NiceCiRespDTO<DataBodySymkeyResp> respDTO = mapper.readValue(resp.getBody(), NiceCiRespDTO.class);
            String strRespBody = resp.getBody().replace("\"\"", "null");
            strRespBody = strRespBody.replace("\\", "").replace("\"{", "{").replace("}\"", "}");  //symkey_stat_info 데이터의 쌍따옴표를 제거하여 Json데이터로 인식하도록 replace 적용
            NiceCiRespDTO<DataBodySymkeyResp> respDTO = mapper.readValue(strRespBody, new TypeReference<NiceCiRespDTO<DataBodySymkeyResp>>(){});
            if (!(respDTO.getDataBody() == null || "".equals(respDTO.getDataBody()))) {
                respDTO.getDataBody().setSiteCode(siteCode);
                respDTO.getDataBody().setRequestNo(requestNo);
                respDTO.getDataBody().setKey(key);
                respDTO.getDataBody().setIv(iv);
                respDTO.getDataBody().setHmacKey(hmacKey);
            }

            return respDTO;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("대칭키 등록 요청 실패." + e.getMessage());
        }
    }


    private String createSymkeyRegInfo(String siteCode, String requestNo, String key, String iv, String hmacKey) throws JsonProcessingException {
        Map<String, String> m = new HashMap<>();
        m.put("site_code", siteCode);   //[필수]사이트코드(공개키요청시 수신한 사이트코드)
        m.put("request_no", requestNo); //[필수]요청고유번호(이용기관에서 임의 생성한 값)
        m.put("key", key);              //[필수]회원사에서 사용할 암호화 KEY 세팅(32byte AES256 bits, 16byte AES128 bits). NICE에 Key 등록 후 최대 6개월 내 갱신 필요
        m.put("iv", iv);                //[필수]데이터를 암호화할 Initial Vector. 회원사에서 생성(16 byte 딱 맞게 생성)
        m.put("hmac_key", hmacKey);     //[필수]무결성체크값에 사용할 Hmac Key. 회원사에서 생성(32 byte 딱 맞게 생성)

        return mapper.writeValueAsString(m);
    }


    /**
     * 대칭키 생성
     * -. KEY를 NICE에 등록 후 6개월 내 갱신이 필요 함
     * -. 6개월 내 현재 등록키, 이전 등록키 사용 가능하므로 대칭키 등록 후 AP에서 암호화키 변경 적용 필요
     * -. 6개월 내 키등록이 없으면 암복호화 오류 발생
     * <p>
     * [예시]
     * 1) 이용기관에서 대칭키 드록 후, 암복호화를 진행
     * 2) 적절한 시점에 신규 대칭키를 등록
     * 3) API에서도 신규 대칭키로 암호화(enc_data) 키 변경 진행
     * => 직전 등록된 대칭키는 유효기간 내 사용가능하므로 대칭키 등록과 API의 암호화 키변경은 이용기관 일정에 따라 진행)
     *
     * @return
     */
    private String createKey() {
        return randomAlphaWord(32);
    }

    /**
     * Initail Vector 생성
     * -. 데이터를 암호화할 Initial Vector
     * -. 16 byte 딱 맞게 생성 해야 함
     *
     * @return
     */
    private String createIv() {
        return randomAlphaWord(16);
    }

    /**
     * 요청고유번호
     * -.이용기관에서 임의 생성한 값
     *
     * @return
     */
    private String createRequestNo() {
        return randomAlphaWord(30);
    }

    /**
     * hamc_key 생성
     * -. 무결성체크값에 사용할 Hmac Key
     * -. 32 byte 딱 맞게 생성 해야 함
     *
     * @return
     */
    private String createHmacKey() {
        return randomAlphaWord(32);
    }


    /**
     * 공개키로 암호화를 수행 후 반환 한다.
     *
     * @param symkeyRegInfo
     * @param sPublicKey
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    protected final String encSymkeyRegInfo(String symkeyRegInfo, String sPublicKey) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        //공개키 변환
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//        byte[] cipherEnc = Base64Utils.decode(sPublicKey.getBytes());
        byte[] cipherEnc = Base64.getDecoder().decode(sPublicKey);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(cipherEnc);
        java.security.PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

        //암호화
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] bytePlain = cipher.doFinal(symkeyRegInfo.getBytes());

        // Base64 인코딩
        String keyInfoEnc = Base64Utils.encodeToString(bytePlain);

        return keyInfoEnc;
    }


    /**
     * JSON 포맷의 요청메시지 생성
     *
     * @param pubkeyVersion
     * @param symkeyRegInfo
     * @return
     * @throws JsonProcessingException
     */
    protected String createMessage(String pubkeyVersion, String symkeyRegInfo) throws JsonProcessingException {
        Map<String, String> dataHeader = new HashMap<>();
        dataHeader.put("CNTY_CD", "ko");  //[필수]이용언어: ko, en, cn ...
        dataHeader.put("TRAN_ID", null);  //[선택]API 통신구간에서 요청에 대한 응답을 확인하기 위한 고유번호

        Map<String, String> dataBody = new HashMap<>();
        dataBody.put("pubkey_version", pubkeyVersion);  //[필수]공개키 버전
        dataBody.put("symkey_reg_info", symkeyRegInfo); //[필수]JSON암호화 값

        Map<String, Object> m = new HashMap<>();
        m.put("dataHeader", dataHeader);
        m.put("dataBody", dataBody);

        return mapper.writeValueAsString(m);
    }
}
