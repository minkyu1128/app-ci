package cokr.xit.ci.api.service.support.rest.code;

public enum NiceCiApiCd {

	UNKNOWN("알수없음")
	/* =======================================================================
	* HTTP 응답코드
	*  -. HTTP의 코드값을 열거형 상수로 선언하기 위해 prefix "HTTP_"를 추가 함
	======================================================================= */
	, HTTP_200("No Error")
	, HTTP_400("Bad Request or Inavalid Token")
	, HTTP_401("Authorized required")
	, HTTP_402("unAuthorized")
	, HTTP_403("service Disabled")
	, HTTP_404("Service Not Found")
	, HTTP_500("Internal Server Error")
	, HTTP_501("Access Denied by Protected Service")
	, HTTP_502("Bad Response from Protected Service")
	, HTTP_503("Service Temporarily Unavailable")

	/* =======================================================================
	* APIM 결과코드 (dataHeader)
	*  -. Data Header의 코드값을 열거형 상수로 선언하기 위해 prefix "HEAD_"를 추가 함
	======================================================================= */
	, HEAD_1200("오류 없음 (정상)")
	, HEAD_1300("request body가 비었습니다.")
	, HEAD_1400("잘못된 요청")
	, HEAD_1401("인증 필요")
	, HEAD_1402("권한없음")
	, HEAD_1403("서비스 사용 중지됨")
	, HEAD_1404("서비스를 찾을 수 없음")
	, HEAD_1500("서버 내부 오류")
	, HEAD_1501("보호된 서비스에서 엑세스가 거부되었습니다.")
	, HEAD_1502("보호된 서비스에서 응답이 잘못되었습니다.")
	, HEAD_1503("일시적으로 사용할 수 없는 서비스")
	, HEAD_1700("엑세스가 허용되지 않습니다. - Client ID")
	, HEAD_1701("엑세스가 허용되지 않습니다. - Service URI")
	, HEAD_1702("엑세스가 허용되지 않습니다. - Client ID + Client IP")
	, HEAD_1703("엑세스가 허용되지 않습니다. - Client ID + Service URI")
	, HEAD_1705("엑세스가 허용되지 않습니다. - Client ID + Black List Client IP")
	, HEAD_1706("액세스가 허용되지 않습니다 - Client ID + Product Code")
	, HEAD_1711("거래제한된 요일입니다.")
	, HEAD_1712("거래제한된 시간입니다.")
	, HEAD_1713("거래제한된 요일/시간입니다.")
	, HEAD_1714("거래제한된 일자입니다.")
	, HEAD_1715("거래제한된 일자/시간입니다.")
	, HEAD_1716("공휴일 거래가 제한된 서비스입니다.")
	, HEAD_1717("SQL인젝션, XSS방어")
	, HEAD_1800("잘못된 토큰")
	, HEAD_1801("잘못된 클라이언트 정보")
	, HEAD_1900("초과된 연결 횟수")
	, HEAD_1901("초과 된 토큰 조회 실패")
	, HEAD_1902("초과된 토큰 체크 실패")
	, HEAD_1903("초과된 접속자 수 ")
	, HEAD_1904("전송 크기 초과")
	, HEAD_1905("접속량이 너무 많음")
	, HEAD_1906("상품이용 한도 초과")
	, HEAD_1907("API 이용 주기 초과")
	, HEAD_1908("상품 이용 주기 초과")
	
	
	/* =======================================================================
	* 응답코드(rsp_cd)
	======================================================================= */
	, P000("정상응답")
	, S603("내부 DB 오류")
	, P013("이용기관 개시상태 아님")
	, E998("서비스 권한 오류")
	, E999("내부시스템 오류")
	, Exxx("기타시스템 오류")


	/* =======================================================================
	* APIM 결과코드 (dataBody)
	======================================================================= */
	, EAPI2500("맵핑정보 없음 - {0}")
	, EAPI2510("요청맵핑 데이터가 없습니다.")
	, EAPI2530("응답전문 맵핑 오류")
	, EAPI2540("대응답 정보 없음")
	, EAPI2550("숫자타입 입력 오류")
	, EAPI2560("실수타입 입력 오류")
	, EAPI2561("실수형 타입 길이정보 문법 에러 ( 형식 : \"전체길이,실수부길이\")")
	, EAPI2562("실수형 타입 논리 에러 ( 전체 길이는 소수부 길이보다 커야합니다. )")
	, EAPI2563("실수형 타입 파싱 에러( 입력값을 실수값으로 변환할 수 없습니다.  )")
	, EAPI2564("실수형 타입 정수부 길이 에러")
	, EAPI2565("실수형 타입 소수부 길이 에러")
	, EAPI2600("내부 시스템 오류")
	, EAPI2700("외부 시스템 연동 오류")
	, EAPI2701("타임아웃이 발생하였습니다.")
	, EAPI2702("DISCONNECTION_OK")
	, EAPI2703("DISCONNECTION_FAIL")
	, EAPI2704("RESULT_OK")
	, EAPI2705("RESULT_FAIL")
	, EAPI2892("반복부 카운터 에러(지정된 건수보다 크거나 작습니다)")
	, EAPI5001("schema 검증 정보가 없습니다.")
	, EAPI5002("schema 검증 실패")


	/* =======================================================================
	* 상세 결과코드
	*  -. 상세결과 코드값을 열거형 상수로 선언하기 위해 prefix "DRSLT_"를 추가 함
	======================================================================= */
	, DRSLT_0000("공개키 발급")
	, DRSLT_0001("필수입력값 오류")
	, DRSLT_0003("공개키 발급 대상 회원사 아님")
	, DRSLT_0099("기타오류")
	;




	private final String code;     //코드
	private final String codeNm;   //코드명
	NiceCiApiCd(String codeNm) {
		this.code = this.name();
		this.codeNm = codeNm;
	}

	public String getCode() {
		return this.code;
	}

	public String getCodeNm() {
		return this.codeNm;
	}


	public static NiceCiApiCd valueOfEnum(String code){
		if(code == null)
			return NiceCiApiCd.UNKNOWN;

		NiceCiApiCd ensErrCd = null;
		try {
			ensErrCd = NiceCiApiCd.valueOf(code);
		} catch (IllegalArgumentException e){
			ensErrCd = NiceCiApiCd.UNKNOWN;
		}
		return ensErrCd;
	}
}
