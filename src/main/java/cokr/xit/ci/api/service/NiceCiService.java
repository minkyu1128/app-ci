package cokr.xit.ci.api.service;

import cokr.xit.ci.api.code.ErrCd;
import cokr.xit.ci.api.model.ResponseVO;
import cokr.xit.ci.api.service.support.NiceCiGenerator;
import cokr.xit.ci.api.service.support.socket.Interop;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class NiceCiService {

    private final NiceCiGenerator niceCiGenerator;

	@Value("${contract.nice.ci.type}")
	private String type;

	/**
	 * 주민번호로 CI를 취득 한다.
	 * 	-.CI: 연계정보(Connecting Information)
	 * 	-.국가표준 규격에 따라 사용자의 주민번호를 암호화한 개인 식별값.
	 * 	-.서비스에 상관없이 값이 일정 함.
	 * 	-.주민번호 -> 해쉬 -> CI 값 (88 byte)
	 * @param siteCode
	 * @param sitePw
	 * @param jids
	 * @return
	 */
	public ResponseVO findAllBySiteCodeAndSitePwAndJidIn(String siteCode, String sitePw, List<String> jids) {
		return ResponseVO.builder()
				.errCode(ErrCd.OK)
				.errMsg(ErrCd.OK.getCodeNm())
				.resultInfo(
//						jids.stream()
						jids.parallelStream()
						.map(jid -> {
							ResponseVO responseVO = null;
							try {
								/* ========================
								 * validate
								 ======================== */
								if(!Optional.ofNullable(siteCode).isPresent()){
									responseVO = ResponseVO.builder().errCode(ErrCd.ERR401).errMsg("사이트코드(은)는 필수조건 입니다.").build();
									throw new RuntimeException(responseVO.getErrMsg());
								}
								if(!Optional.ofNullable(sitePw).isPresent()){
									responseVO = ResponseVO.builder().errCode(ErrCd.ERR401).errMsg("사이트 패스워드(은)는 필수조건 입니다.").build();
									throw new RuntimeException(responseVO.getErrMsg());
								}
								if(!Optional.ofNullable(jid).isPresent()){
									responseVO = ResponseVO.builder().errCode(ErrCd.ERR401).errMsg("서비스 구분값(주민번호:JID)(은)는 필수조건 입니다.").build();
									throw new RuntimeException(responseVO.getErrMsg());
								}

								/* ========================
								 * api call
								 ======================== */
								if("socket".equals(type)) {
									responseVO = Interop.getCI(siteCode, sitePw, jid);
								}else{
                                    responseVO = niceCiGenerator.getCI(jid, null);
								}

							} catch (Exception e){
								log.error(e.getMessage());
							} finally {
								/* ========================
								 * result set
								 ======================== */
								Map<String, Object> m = new HashMap<>();
								m.put("key", jid);
								m.put("value", responseVO);
								return m;
							}

						})
						.collect(Collectors.toMap(m -> String.valueOf(m.get("key")), m -> m.get("value"), (k1, k2)->k1)))
				.build();
	}


}
