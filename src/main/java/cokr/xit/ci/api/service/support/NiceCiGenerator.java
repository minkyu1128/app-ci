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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public ResponseVO initialKey(){

        NiceCiApiExecutor executor = buildExecutor();
        try {

            /* ==========================================================================
            * 1. ?????? ??????
            ========================================================================== */
            NiceCiRespVO<DataBodyGenerateTokenResp> tokenResponseVO = executor.token();
            if (!NiceCiApiCd.OK.equals(tokenResponseVO.getErrCode()))
                return ResponseVO.<String>builder().errCode(ErrCd.ERR600).errMsg(tokenResponseVO.getErrCode().getCode() + " " + tokenResponseVO.getErrMsg()).build();

            /* ==========================================================================
            * 2. ????????? ??????
            ========================================================================== */
            NiceCiRespVO<DataBodyPubkeyResp> pubkeyResponseVO = executor.pubkey();
            if (!NiceCiApiCd.OK.equals(pubkeyResponseVO.getErrCode()))
                return ResponseVO.<String>builder().errCode(ErrCd.ERR600).errMsg(pubkeyResponseVO.getErrCode().getCode() + " " + pubkeyResponseVO.getErrMsg()).build();

            /* ==========================================================================
            * 3. ????????? ?????? ??????
            *   -. ???????????? 1??? 1?????? ????????? ????????????, 1??? 2??? ?????? ???????????? ??? "0099 ????????????" ??? ?????? ?????????
            *   -. ??????????????? ????????? ???????????? DB??? ????????????, ?????? ????????? ????????? ???????????? ????????? ??????.
            ========================================================================== */
//            NiceCiRespVO<DataBodySymkeyResp> symkeyResponseVO = executor.symkey();
//            if (!NiceCiApiCd.OK.equals(symkeyResponseVO.getErrCode()))
//                return ResponseVO.<String>errRsltBuilder().errCode(ErrCd.ERR600).errMsg(symkeyResponseVO.getErrCode().getCode() + " " + symkeyResponseVO.getErrMsg()).build();
            ObjectMapper mapper = new ObjectMapper();
            DataBodySymkeyResp dataBodySymkeyResp = null;
            if (SymmetricKey.isValidStat()) {
                dataBodySymkeyResp = SymmetricKey.getInstance().getData();
            } else {    //????????? ????????? ???????????? ?????????...
                //?????? ????????? ??????(by ?????????)
                Optional<NiceCiSymkeyMng> niceCiSymkeyMng = niceCiSymkeyMngRepository.findByPubkey(pubkeyResponseVO.getResultInfo().getPublicKey());

                //????????? ???????????????????????? 1??? ?????? ????????????
                if (niceCiSymkeyMng.isPresent()
                        &&(Long.parseLong(niceCiSymkeyMng.get().getExpireDt()) > Long.parseLong(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))) ) {
                    dataBodySymkeyResp = mapper.readValue(niceCiSymkeyMng.get().getRespJsonData(), DataBodySymkeyResp.class);

                    //????????? ????????? ?????? ?????????
                    SymmetricKey.getInstance(dataBodySymkeyResp);


                } else {
                    //3. ????????? ?????? ??????
                    NiceCiRespVO<DataBodySymkeyResp> symkeyResponseVO = executor.symkey();
                    if (!NiceCiApiCd.OK.equals(symkeyResponseVO.getErrCode()))
                        return ResponseVO.<String>builder().errCode(ErrCd.ERR600).errMsg(symkeyResponseVO.getErrCode().getCode() + " " + symkeyResponseVO.getErrMsg()).build();
                    dataBodySymkeyResp = symkeyResponseVO.getResultInfo();

                    //????????? ?????? DB ??????
                    niceCiSymkeyMngRepository.save(NiceCiSymkeyMng.builder()
                            .pubkey(pubkeyResponseVO.getResultInfo().getPublicKey())
                            .symkey(dataBodySymkeyResp.getKey())
                            .expireDt(dataBodySymkeyResp.getSymkeyStatInfo().getCurValidDtim())
                            .version(dataBodySymkeyResp.getSymkeyStatInfo().getCurSymkeyVersion())
                            .iv(dataBodySymkeyResp.getIv())
                            .hmacKey(dataBodySymkeyResp.getHmacKey())
                            .respJsonData(mapper.writeValueAsString(dataBodySymkeyResp))
                            .build());
                }
            }


            return ResponseVO.<String>builder().errCode(ErrCd.OK).errMsg(ErrCd.OK.getCodeNm()).build();
        } catch (Exception e) {
            return ResponseVO.<String>builder().errCode(ErrCd.ERR699).errMsg(e.getMessage()).build();
        }
    }

    public ResponseVO<String> getCI(String jid, String clientIp) {

        NiceCiApiExecutor executor = this.buildExecutor();
        try {

            /* ==========================================================================
            * 4. ????????? CI ??????
            ========================================================================== */
            NiceCiRespVO<DataBodyCiResp> ciResponseVO = executor.ci(jid, clientIp);
            if (!NiceCiApiCd.OK.equals(ciResponseVO.getErrCode()))
                return ResponseVO.<String>builder().errCode(ErrCd.ERR600).errMsg(ciResponseVO.getErrCode().getCode() + " " + ciResponseVO.getErrMsg()).build();


            return ResponseVO.<String>builder().errCode(ErrCd.OK).errMsg(ErrCd.OK.getCodeNm()).resultInfo(ciResponseVO.getResultInfo().getDecEncData().getCi1()).build();
        } catch (Exception e) {
            return ResponseVO.<String>builder().errCode(ErrCd.ERR699).errMsg(e.getMessage()).build();
        }
    }

    private NiceCiApiExecutor buildExecutor(){
        return NiceCiApiExecutor.builder()
                .HOST(this.HOST)
                .CLIENT_ID(this.CLIENT_ID)
                .CLIENT_SECRET(this.CLIENT_SECRET)
                .API_GENERATE_TOKEN(this.API_GENERATE_TOKEN)
                .API_REVOKE_TOKEN(this.API_REVOKE_TOKEN)
                .API_PUBLICKEY(this.API_PUBLICKEY)
                .API_SYMMETRICKEY(this.API_SYMMETRICKEY)
                .API_CI(this.API_CI)
                .build();
    }
}
