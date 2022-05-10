package cokr.xit.ci.api.code;

public enum ErrCd {

	OK("정상")

	/* =======================================================================
	* 공통(4xx: Client, 5xx: Server, 6xx: LinkService, 9xx: Etc..)
	======================================================================= */
	//클라이언트 요청 오류
	, ERR401("필수 파라미터가 없습니다.")
	, ERR402("파라미터 유효성 검증 오류")
	, ERR403("잘못된 파라미터 입니다.")
	, ERR404("일치하는 자료가 없습니다.")
	, ERR405("잘못된 요청 값.")
	, ERR411("잘못된 JSON 포맷 문자열")
	//서버 오류
	, ERR501("HttpServer 오류")
	, ERR502("HttpClient 오류")
	, ERR503("RestClient 오류")
	, ERR504("요청 데이터 Json 파싱 오류")
	, ERR505("응답 데이터 Json 파싱 오류")
	, ERR506("Hash 생성 오류")
	, ERR511("JSON 형식으로 변환 실패")
	, ERR520("통신오류")
	, ERR521("방화벽 설정 오류")
	//외부서비스 오류
	, ERR600("API서버 요청 오류")
	, ERR601("API서버 응답 오류")
	, ERR602("API서버 내부 오류")
	, ERR603("유효하지 않은 토큰(OTT) 값")
	, ERR610("응답 데이터에 필수값이 없음")
	, ERR620("API Response Error")
	, ERR699("API 기타 오류")
	//기타오류
	, ERR999("기타 오류")
	, ERR901("권한 없음")
	, ERR902("유효하지 않은 데이터")
	, ERR903("처리 완료된 데이터")
	;




	private final String code;     //코드
	private final String codeNm;   //코드명
	ErrCd(String codeNm) {
		this.code = this.name();
		this.codeNm = codeNm;
	}

	public String getCode() {
		return this.code;
	}

	public String getCodeNm() {
		return this.codeNm;
	}
}
