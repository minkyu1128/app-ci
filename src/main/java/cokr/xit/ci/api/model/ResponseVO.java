package cokr.xit.ci.api.model;

import cokr.xit.ci.api.code.ErrCd;
import lombok.*;

@Builder
@Getter
@ToString
@EqualsAndHashCode
public class ResponseVO<T> {
    private ErrCd errCode;
    private String errMsg;
    private T resultInfo;

}
