package cokr.xit.ci.api.service.support.rest.model;

import cokr.xit.ci.api.service.support.rest.model.conf.DataHeader;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class NiceCiRespDTO<T> {


    private DataHeader dataHeader;

    private T dataBody;

}
