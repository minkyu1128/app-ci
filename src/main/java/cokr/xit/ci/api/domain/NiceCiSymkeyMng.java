package cokr.xit.ci.api.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @ToString
@Builder
//@DynamicUpdate //변경된 필드에 대해서만 update SQL문 생성
/*
 * @NoArgsConstructor, @AllArgsConstructor 추가
 * 	-.@Builder만 사용할 경우 queryDsl Select 시 기본생성자 오류("No default constructor for entity") 발생
 */
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "nice_ci_symkey_mng", schema = "", catalog = "")
@Schema(name = "NiceCiSymkeyMng",  description = "나이스 CI 대칭키 관리")
public class NiceCiSymkeyMng {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long niceCiSymkeyMngId;          //ID(PK)

	@Schema(required = true, title = "공개키(PK)", example = " ", description = "")
	@Column(nullable = false, length = 1000)
	private String pubkey;

	@Schema(required = true, title = "대칭키", example = " ", description = "대칭키등록은 1일 1회만 가능하며, 6개월 내 갱신(재등록) 필요")
	@Column(nullable = false, length = 32)
	private String symkey;

	@Schema(required = false, title = "만료일시", example = " ", description = "yyyyMMddHHmmss 포맷")
	@Column(nullable = true, length = 14)
	private String expireDt;

	@Schema(required = false, title = "버전", example = " ", description = "대칭키 현재 버전")
	@Column(nullable = true, length = 50)
	private String version;

	@Schema(required = true, title = "Initial Vector 값", example = " ", description = "CI 송수신 시 사용할 암복호화 key 값")
	@Column(nullable = false, length = 16)
	private String iv;

	@Schema(required = true, title = "hmac key", example = " ", description = "무결성체크 시 사용할 HMAC KEY")
	@Column(nullable = false, length = 32)
	private String hmacKey;

	@Schema(required = true, title = "대체키등록API 응답 Json 데이터", example = " ", description = "DataBodySymkeyResp 객체")
	@Column(nullable = false, length = 1000)
	private String respJsonData;


	@Schema(required = false, title = "등록일시", example = " ", description = "")
	@Column(name = "regist_dt", nullable = true)
	@CreationTimestamp
	private LocalDateTime registDt;

	@Schema(required = false, title = "최종 수정일시", example = " ", description = "")
	@Column(name = "last_updt_dt", nullable = true)
	@UpdateTimestamp
	private LocalDateTime lastUpdtDt;



}
