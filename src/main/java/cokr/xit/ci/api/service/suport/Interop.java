package cokr.xit.ci.api.service.suport;


import KISINFO.VNO.VNOInterop;
import cokr.xit.ci.api.code.ErrCd;
import cokr.xit.ci.api.model.ResponseVO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Interop
{	
    public Interop()
	{
	    
	}
    
	public static ResponseVO getCI(String siteCode, String sitePw, String jumin) {
		
		final String sSiteCode	= siteCode;	// NICE평가정보에서 발급한 서비스 사이트코드
		final String sSitePw    = sitePw;   // NICE평가정보에서 발급한 서비스 사이트패스워드
		final String sJumin		= jumin.replaceAll("[^0-9]", "");    // 주민등록번호 13자리
		final String sFlag		= "JID";    // 서비스 구분값 (JID:주민번호 이용)



		int iRtnCI = -1;
		ErrCd errCode = null;
		String errMsg = null;
		String ci = null;
		try {
			// 모듈 객체 생성
			VNOInterop vnoInterop = new VNOInterop();

	    	/* ──── CI 값을 추출하기 위한 부분 Start */
	    	// 인증요청처리
	    	iRtnCI = vnoInterop.fnRequestConnInfo(sSiteCode, sSitePw, sJumin, sFlag);
	    	log.info("=======================================================================");
	    	log.info("JID=" + sJumin);
	    	log.info("iRtnCI=" + iRtnCI);

	    	// 인증결과코드에 따른 처리
	    	if (iRtnCI == 1) {	    	
	    		// CI 값 추출 (연계정보 확인값, 88Byte)
	    		String sConnInfo = vnoInterop.getConnInfo();
	    		log.info("CONNINFO=[" + sConnInfo + "]");

				// 결과설정
				errCode = ErrCd.OK;
				errMsg = String.format("[%s] (응답코드 %s)", ErrCd.OK.getCodeNm(), iRtnCI);
				ci = sConnInfo;
	    	} else if (iRtnCI == 3) {
	    		log.info("[사용자 정보와 서비스 구분값 매핑 오류]");
	    		log.info("사용자 정보와 서비스 구분값이 서로 일치하도록 매핑하여 주시기 바랍니다.");

				// 결과설정
				errCode = ErrCd.ERR405;
				errMsg = String.format("[사용자 정보와 서비스 구분값 매핑 오류] 사용자 정보와 서비스 구분값이 서로 일치하도록 매핑하여 주시기 바랍니다. (응답코드 %s)", iRtnCI);
	    	} else if (iRtnCI == -9) {
	    		log.info("[입력값 오류]");
	    		log.info("fnRequestConnInfo 함수 처리시, 필요한 4개의 파라미터값의 정보를 정확하게 입력해 주시기 바랍니다.");

				// 결과설정
				errCode = ErrCd.ERR403;
				errMsg = String.format("[입력값 오류] fnRequestConnInfo 함수 처리시, 필요한 4개의 파라미터값의 정보를 정확하게 입력해 주시기 바랍니다.(응답코드 %s)", iRtnCI);
	    	} else if (iRtnCI == -21 || iRtnCI == -31 || iRtnCI == -34) {
	    		log.info("[통신오류]");
	    		log.info("방화벽 이용 시 아래 IP와 Port(총 5개)를 등록해주셔야 합니다.");
	    		log.info("IP : 203.234.219.72 / Port : 81, 82, 83, 84, 85");

				// 결과설정
				errCode = ErrCd.ERR521;
				errMsg = String.format("[통신오류] 방화벽 이용 시 아래 IP와 Port(총 5개)를 등록해주셔야 합니다. IP : 203.234.219.72 / Port : 81, 82, 83, 84, 85.(응답코드 %s)", iRtnCI);
	    	} else {
	    		log.info("[기타오류]");
	    		log.info("iRtnCI 값 확인 후 NICE평가정보 전산 담당자에게 문의");

				// 결과설정
				errCode = ErrCd.ERR999;
				errMsg = String.format("[기타오류] iRtnCI 값 확인 후 NICE평가정보 전산 담당자에게 문의. (응답코드 %s)", iRtnCI);
	    	}
	    	/* ──── CI 값을 추출하기 위한 부분 End */

		} catch (Exception e) {
			// 결과설정
			errCode = ErrCd.ERR602;
			errMsg = String.format("[나이스API 오류] %s (응답코드 %s)", e.getMessage(), iRtnCI);
		} finally {
			return ResponseVO.builder()
					.errCode(errCode)
					.errMsg(errMsg)
					.resultInfo(ci)
					.build();
		}
	}
	
	


}