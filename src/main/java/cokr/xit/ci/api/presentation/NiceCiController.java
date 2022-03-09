package cokr.xit.ci.api.presentation;

import cokr.xit.ci.api.model.ResponseVO;
import cokr.xit.ci.api.service.NiceCiService;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class NiceCiController {

    private final NiceCiService diCiService;

    @Value("${nice.api.ci.site-code ?: }")
    private String SITE_CODE;
    @Value("${nice.api.ci.site-pw ?: }")
    private String SITE_PW;

    @SuppressWarnings("deprecation")
    @PostMapping("/nice/ci")
//    public ResponseEntity<ResponseVO> ci(@RequestBody Map<String, Object> mParam) {
    public ResponseEntity<ResponseVO> ci(@RequestBody String param) {
        Gson gson = new Gson();
        List<Map<String, Object>> params = gson.fromJson(param, ArrayList.class);
        List<String> jids = params.stream()
                .map(row -> {
                    if(row.get("jid") instanceof Double)
                        return String.valueOf(Double.valueOf((Double) row.get("jid")).longValue());
                    else
                        return String.valueOf(row.get("jid"));
                })
                .collect(Collectors.toList());

        ResponseVO respVO = diCiService.findAllByJid(SITE_CODE, SITE_PW, jids);

        return new ResponseEntity<ResponseVO>(respVO, HttpStatus.OK);
    }
}
