package cokr.xit.ci.api.service.support.rest.model.conf;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "DataBodyGenerateTokenResp",  description = "토큰발급 API 응답DataBody")
public class DataBodyGenerateTokenResp {

    @JsonAlias({"access_token"})
    @Schema(required = true, title = "엑세스토큰", example = " ", description = "사용자 엑세스 토큰 값(모든 API 요청시 헤더에 access_token을 포함하여 전송)")
    private String accessToken;

    @JsonAlias({"expires_in"})
    @Schema(required = true, title = "엑세스토큰 만료 절대시간(sec)", example = " ", description = "access token 만료까지 남은시간(초)")
    private Integer expiresIn;

    @JsonAlias({"token_type"})
    @Schema(required = true, title = "토큰타입", example = " ", description = "bearer로 고정")
    private String tokenType;

    @JsonAlias({"scope"})
    @Schema(required = true, title = "요청한 scope값", example = " ", description = "요청한 scope값(기본 default)")
    private String scope;

    @Setter
    @Schema(required = true, title = "access token 만료시간(yyyyMmddHHmmss)", example = " ", description = "사용자 응답을 위해 절대시간(expires_in)을 yyyyMMddHHmmss 포맷으로 변경한 값")
    private String expiredDt;
}
