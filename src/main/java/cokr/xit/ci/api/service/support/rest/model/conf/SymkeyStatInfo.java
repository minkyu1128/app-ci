package cokr.xit.ci.api.service.support.rest.model.conf;


import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SymkeyStatInfo",  description = "대칭키 요청 API 응답DTO의 JSON 데이터")
public class SymkeyStatInfo {

    @JsonAlias({"cur_symkey_version"})
    @Schema(required = false, title = "현재 대칭키 버전", example = " ", description = "현재 등록요청한 대칭키 버전")
    private String curSymkeyVersion;

    @JsonAlias({"cur_valid_dtim"})
    @Schema(required = false, title = "현재 대칭키 만료일시", example = " ", description = "현재 등록된 대칭키 만료일시(YYYYMMDDHH24MISS)")
    private String curValidDtim;

    @JsonAlias({"bef_symkey_version"})
    @Schema(required = false, title = "이전 대칭키 버전", example = " ", description = "이전 등록된 대칭키 버전")
    private String befSymkeyVersion;

    @JsonAlias({"bef_valid_dtim"})
    @Schema(required = false, title = "이전 대칭키 만료일시", example = " ", description = "이전 등록된 대칭키 만료일시(YYYYMMDDHH24MISS)")
    private String befValidDtim;


}
