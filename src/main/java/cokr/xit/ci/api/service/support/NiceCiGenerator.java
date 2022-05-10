package cokr.xit.ci.api.service.support;

import cokr.xit.ci.api.code.ErrCd;
import cokr.xit.ci.api.domain.NiceCiSymkeyMng;
import cokr.xit.ci.api.domain.repository.NiceCiSymkeyMngRepository;
import cokr.xit.ci.api.model.ResponseVO;
import cokr.xit.ci.api.service.support.rest.NiceCiApiExecutor;
import cokr.xit.ci.api.service.support.rest.code.NiceCiApiCd;
import cokr.xit.ci.api.service.support.rest.model.NiceCiRespVO;
import cokr.xit.ci.api.service.support.rest.model.conf.DataBodyCiResp;
import cokr.xit.ci.api.service.support.rest.model.conf.DataBodyGenerateTokenResp;
import cokr.xit.ci.api.service.support.rest.model.conf.DataBodyPubkeyResp;
import cokr.xit.ci.api.service.support.rest.model.conf.DataBodySymkeyResp;
import cokr.xit.ci.api.service.support.rest.utils.SymmetricKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Log4j2
@Component
@RequiredArgsConstructor
public class NiceCiGenerator {

    private final NiceCiSymkeyMngRepository niceCiSymkeyMngRepository;


    @Value("${contract.nice.ci.rest.host}")
    private String HOST;
    @Value("${contract.nice.ci.rest.client-id}")
    private String CLIENT_ID;
    @Value("${contract.nice.ci.rest.client-secret}")
    private String CLIENT_SECRET;
    @Value("${contract.nice.ci.rest.api.generate-token}")
    private String API_GENERATE_TOKEN;
    @Value("${contract.nice.ci.rest.api.revoke-token}")
    private String API_REVOKE_TOKEN;
    @Value("${contract.nice.ci.rest.api.publickey}")
    private String API_PUBLICKEY;
    @Value("${contract.nice.ci.rest.api.symmetrickey}")
    private String API_SYMMETRICKEY;
    @Value("${contract.nice.ci.rest.api.ci}")
    private String API_CI;

    public ResponseVO<String> getCI(String jid, String clientIp) {

        NiceCiApiExecutor executor = NiceCiApiExecutor.builder()
                .HOST(this.HOST)
                .CLIENT_ID(this.CLIENT_ID)
                .CLIENT_SECRET(this.CLIENT_SECRET)
                .API_GENERATE_TOKEN(this.API_GENERATE_TOKEN)
                .API_REVOKE_TOKEN(this.API_REVOKE_TOKEN)
                .API_PUBLICKEY(this.API_PUBLICKEY)
                .API_SYMMETRICKEY(this.API_SYMMETRICKEY)
                .API_CI(this.API_CI)
                .build();
        try {

            /* ==========================================================================
            * 1. 토큰 요청
            ========================================================================== */
            NiceCiRespVO<DataBodyGenerateTokenResp> tokenResponseVO = executor.token();
            if (!NiceCiApiCd.OK.equals(tokenResponseVO.getErrCode()))
                return ResponseVO.<String>builder().errCode(ErrCd.ERR600).errMsg(tokenResponseVO.getErrCode().getCode() + " " + tokenResponseVO.getErrMsg()).build();

            /* ==========================================================================
            * 2. 공개키 요청
            ========================================================================== */
            NiceCiRespVO<DataBodyPubkeyResp> pubkeyResponseVO = executor.pubkey();
            if (!NiceCiApiCd.OK.equals(pubkeyResponseVO.getErrCode()))
                return ResponseVO.<String>builder().errCode(ErrCd.ERR600).errMsg(pubkeyResponseVO.getErrCode().getCode() + " " + pubkeyResponseVO.getErrMsg()).build();

            /* ==========================================================================
            * 3. 대칭키 등록 요청  
            *   -. 대칭키는 1일 1회만 등록이 가능하며, 1일 2회 이상 등록요청 시 "0099 기타오류" 가 발생 하므로
            *   -. 등록요청에 성공한 대칭키는 DB에 저장하여, 서버 재기동 시에도 휘발되지 않도록 한다.
            ========================================================================== */
//            NiceCiRespVO<DataBodySymkeyResp> symkeyResponseVO = executor.symkey();
//            if (!NiceCiApiCd.OK.equals(symkeyResponseVO.getErrCode()))
//                return ResponseVO.<String>builder().errCode(ErrCd.ERR600).errMsg(symkeyResponseVO.getErrCode().getCode() + " " + symkeyResponseVO.getErrMsg()).build();
            ObjectMapper mapper = new ObjectMapper();
            DataBodySymkeyResp dataBodySymkeyResp = null;
            if (SymmetricKey.isValidStat()) {
                dataBodySymkeyResp = SymmetricKey.getInstance().getData();
            } else {    //대칭키 상태가 유효하지 않으면...
                //현재 대칭키 조회(by 공개키)
                Optional<NiceCiSymkeyMng> niceCiSymkeyMng = niceCiSymkeyMngRepository.findByPubkey(pubkeyResponseVO.getResultInfo().getPublicKey());

                if (niceCiSymkeyMng.isPresent()) {
                    dataBodySymkeyResp = mapper.readValue(niceCiSymkeyMng.get().getRespJsonData(), DataBodySymkeyResp.class);

                    //대칭키 싱글톤 객체 초기화
                    SymmetricKey.getInstance(dataBodySymkeyResp);

                } else {
                    //3. 대칭키 등록 요청
                    NiceCiRespVO<DataBodySymkeyResp> symkeyResponseVO = executor.symkey();
                    if (!NiceCiApiCd.OK.equals(symkeyResponseVO.getErrCode()))
                        return ResponseVO.<String>builder().errCode(ErrCd.ERR600).errMsg(symkeyResponseVO.getErrCode().getCode() + " " + symkeyResponseVO.getErrMsg()).build();
                    dataBodySymkeyResp = symkeyResponseVO.getResultInfo();

                    //대칭키 정보 DB 등록
                    niceCiSymkeyMngRepository.save(NiceCiSymkeyMng.builder()
                            .pubkey(pubkeyResponseVO.getResultInfo().getPublicKey())
                            .symkey(dataBodySymkeyResp.getKey())
                            .version(dataBodySymkeyResp.getSymkeyStatInfo().getCurSymkeyVersion())
                            .iv(dataBodySymkeyResp.getIv())
                            .hmacKey(dataBodySymkeyResp.getHmacKey())
                            .respJsonData(mapper.writeValueAsString(dataBodySymkeyResp))
                            .build());
                }
            }

            /* ==========================================================================
            * 4. 아이핀 CI 요청
            ========================================================================== */
            NiceCiRespVO<DataBodyCiResp> ciResponseVO = executor.ci(jid, clientIp);
            if (!NiceCiApiCd.OK.equals(pubkeyResponseVO.getErrCode()))
                return ResponseVO.<String>builder().errCode(ErrCd.ERR600).errMsg(ciResponseVO.getErrCode().getCode() + " " + ciResponseVO.getErrMsg()).build();


            return ResponseVO.<String>builder().errCode(ErrCd.OK).errMsg(ErrCd.OK.getCodeNm()).resultInfo(ciResponseVO.getResultInfo().getDecEncData().getCi1()).build();
        } catch (Exception e) {
            return ResponseVO.<String>builder().errCode(ErrCd.ERR699).errMsg(e.getMessage()).build();
        }
    }
}
