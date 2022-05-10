package cokr.xit.ci.api.service.support.rest.model;

import cokr.xit.ci.api.service.support.rest.code.NiceCiApiCd;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@ToString
public class NiceCiRespVO<T> {

    private NiceCiApiCd errCode;
    private String errMsg;


    private T resultInfo;


    @Builder(builderClassName = "okBuilder" ,builderMethodName = "okBuilder")
    NiceCiRespVO(T resultInfo) {
        this.errCode = NiceCiApiCd.OK;
        this.errMsg = NiceCiApiCd.OK.getCodeNm();
        this.resultInfo = resultInfo;
    }

    @Builder(builderClassName = "errBuilder" ,builderMethodName = "errBuilder")
    NiceCiRespVO(NiceCiApiCd errCode, String errMsg, T resultInfo) {
        this.errCode = errCode;
        this.errMsg = errMsg;
        this.resultInfo = resultInfo;
    }
}
