package cokr.xit.ci.api.service.support.rest.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class GenerateTokenRespDTO implements TransDTO {

    @JsonAlias({"access_token"})
    private String accessToken; //사용자 엑세스 토큰 값(모든 API 요청시 헤더에 access_token을 포함하여 전송)

    @JsonAlias({"expires_in"})
    private Long expiresIn;     //access token 만료까지 남은시간(초)

    @JsonAlias({"token_type"})
    private String tokenType;   //bearer로 고정

    @JsonAlias({"scope"})
    private String scope;       //요청한 scope값(기본 default)

    @Setter
    private String expiredDt;   //access token 만료시간(yyyyMmddHHmmss)
}
