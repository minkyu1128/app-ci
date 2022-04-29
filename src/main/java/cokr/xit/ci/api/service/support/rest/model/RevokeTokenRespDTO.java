package cokr.xit.ci.api.service.support.rest.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class RevokeTokenRespDTO implements TransDTO {

    @JsonAlias({"result"})
    private Boolean result; //폐기여부(true: 폐기 성공, false: 폐기 실패)

}
