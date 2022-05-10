package cokr.xit.ci.api.service.support.rest.model.conf;


import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "EncData",  description = "아이핀 CI 요청 응답DTO의 JSON 데이터")
public class EncData {

    @JsonAlias({"ci1"})
    @Schema(required = false, title = "연계정보1", example = " ", description = "Connection Info로 다른 웹사이트간 고객확인용으로 사용")
    private String ci1;

    @JsonAlias({"ci2"})
    @Schema(required = false, title = "연계정보2", example = " ", description = "연계정보1의 Key 유출에 대비한 예비값")
    private String ci2;

    @JsonAlias({"updt_cnt"})
    @Schema(required = false, title = "갱신횟수", example = " ", description = "연계정보 Key 유출될 경우 갱신 횟수(초기값 1세팅)")
    private String updtCnt;

    @JsonAlias({"tx_unique_no"})
    @Schema(required = false, title = "거래고유번호", example = " ", description = "result_cd가 0000일 경우 NICE에서 제공하는 거래일련번호")
    private String txUniqueNo;

}
