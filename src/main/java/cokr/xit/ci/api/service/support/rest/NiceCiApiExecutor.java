package cokr.xit.ci.api.service.support.rest;

import cokr.xit.ci.api.service.support.rest.api.CiRequest;
import cokr.xit.ci.api.service.support.rest.api.PubkeyRequest;
import cokr.xit.ci.api.service.support.rest.api.SymkeyRegist;
import cokr.xit.ci.api.service.support.rest.api.TokenGenerate;
import cokr.xit.ci.api.service.support.rest.code.NiceCiApiCd;
import cokr.xit.ci.api.service.support.rest.model.NiceCiRespVO;
import cokr.xit.ci.api.service.support.rest.model.conf.DataBodyCiResp;
import cokr.xit.ci.api.service.support.rest.model.conf.DataBodyGenerateTokenResp;
import cokr.xit.ci.api.service.support.rest.model.conf.DataBodyPubkeyResp;
import cokr.xit.ci.api.service.support.rest.model.conf.DataBodySymkeyResp;
import cokr.xit.ci.api.service.support.rest.utils.PublicKey;
import cokr.xit.ci.api.service.support.rest.utils.SymmetricKey;
import cokr.xit.ci.api.service.support.rest.utils.Token;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Builder
public class NiceCiApiExecutor {
    private final String HOST;
    private final String CLIENT_ID;
    private final String CLIENT_SECRET;
    private final String API_GENERATE_TOKEN;
    private final String API_REVOKE_TOKEN;
    private final String API_PUBLICKEY;
    private final String API_SYMMETRICKEY;
    private final String API_CI;



    public NiceCiRespVO<DataBodyGenerateTokenResp> token() throws Exception {
        NiceCiRespVO<DataBodyGenerateTokenResp> result = null;
        if (Token.isValidStat()) {
            result = NiceCiRespVO.<DataBodyGenerateTokenResp>okBuilder()
                    .resultInfo(Token.getInstance().getData())
                    .build();
        } else {
            result = TokenGenerate.builder()
                    .HOST(this.HOST)
                    .API_URL_GENERATE_TOKEN(this.API_GENERATE_TOKEN)
                    .API_URL_REVOKE_TOKEN(this.API_REVOKE_TOKEN)
                    .build()
                    .execute(CLIENT_ID, CLIENT_SECRET);
            // 토큰정보 싱글톤 초기화
            if (NiceCiApiCd.OK.equals(result.getErrCode()))
                Token.getInstance(result.getResultInfo());
        }


        return result;
    }



    public NiceCiRespVO<DataBodyPubkeyResp> pubkey() throws Exception {
        NiceCiRespVO<DataBodyPubkeyResp> result = null;
        if (PublicKey.isValidStat()) {
            result = NiceCiRespVO.<DataBodyPubkeyResp>okBuilder()
                    .resultInfo(PublicKey.getInstance().getData())
                    .build();
        } else {
            result = PubkeyRequest.builder()
                    .HOST(this.HOST)
                    .API_URL(this.API_PUBLICKEY)
                    .build()
                    .execute(this.CLIENT_ID);
            // 공개키 싱글톤 초기화
            if (NiceCiApiCd.OK.equals(result.getErrCode()))
                PublicKey.getInstance(result.getResultInfo());
        }

        return result;
    }



    public NiceCiRespVO<DataBodySymkeyResp> symkey() throws Exception {
        NiceCiRespVO<DataBodySymkeyResp> result = null;
        if(SymmetricKey.isValidStat()){
            result = NiceCiRespVO.<DataBodySymkeyResp>okBuilder()
                    .resultInfo(SymmetricKey.getInstance().getData())
                    .build();
        }else{
            result = SymkeyRegist.builder()
                    .HOST(this.HOST)
                    .API_URL(this.API_SYMMETRICKEY)
                    .build()
                    .execute(this.CLIENT_ID);
            // 대칭키 싱글톤 초기화
            if(NiceCiApiCd.OK.equals(result.getErrCode()))
                SymmetricKey.getInstance(result.getResultInfo());
        }


        return result;
    }


    public NiceCiRespVO<DataBodyCiResp> ci(String jid, String clientIp) throws Exception {

        NiceCiRespVO<DataBodyCiResp> result = CiRequest.builder()
                .HOST(this.HOST)
                .API_URL(this.API_CI)
                .build()
                .execute(this.CLIENT_ID, this.CLIENT_SECRET, jid, clientIp);

        return result;
    }


}
