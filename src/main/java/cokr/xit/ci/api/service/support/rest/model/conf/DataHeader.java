package cokr.xit.ci.api.service.support.rest.model.conf;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DataHeader {
    @NotEmpty(message = "응답코드 값이 없습니다.")
    @Schema(required = true, title = "응답코드", example = "1200", description = "정상:1200 그외는 오류")
    @JsonAlias({"GW_RSLT_CD"})
    private String GW_RSLT_CD;

    @NotEmpty(message = "응답메시지 값이 없습니다.")
    @Schema(required = true, title = "응답메시지", example = " ", description = "한글 또는 영문")
    @JsonAlias({"GW_RSLT_MSG"})
    private String GW_RSLT_MSG;

    @Schema(required = false, title = "교환ID", example = " ", description = "API 호출 시 요청한 값 그대로 리턴(적용대상: 공개키/대칭키/아이핀CI 요청API)")
    @JsonAlias({"TRAN_ID"})
    private String TRAN_ID;
}
