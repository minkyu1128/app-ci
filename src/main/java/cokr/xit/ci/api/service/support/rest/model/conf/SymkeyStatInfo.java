package cokr.xit.ci.api.service.support.rest.model.conf;

import cokr.xit.ci.api.service.support.rest.model.TransDTO;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class SymkeyStatInfo implements TransDTO {

    @JsonAlias({"cur_symkey_version"})
    private String curSymkeyVersion;      //현재 등록요청한 대칭키 버전

    @JsonAlias({"cur_valid_dtim"})
    private String curValidDtim;          //현재 등록된 대칭키 만료일시(YYYYMMDDHH24MISS)

    @JsonAlias({"bef_symkey_version"})
    private String befSymkeyVersion;      //이전 등록된 대칭키 버전

    @JsonAlias({"bef_valid_dtim"})
    private String befValidDtim;          //이전 등록된 대칭키 만료일시(YYYYMMDDHH24MISS)


}
