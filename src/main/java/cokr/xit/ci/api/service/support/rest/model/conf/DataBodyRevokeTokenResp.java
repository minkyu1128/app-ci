package cokr.xit.ci.api.service.support.rest.model.conf;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "DataBodyRevokeTokenResp",  description = "토큰폐기 API 응답DataBody")
public class DataBodyRevokeTokenResp {

    @JsonAlias({"result"})
    @Schema(required = true, title = "토큰폐기 여부", example = " ", description = "폐기여부(true: 폐기 성공, false: 폐기 실패)")
    private Boolean result; 

}
