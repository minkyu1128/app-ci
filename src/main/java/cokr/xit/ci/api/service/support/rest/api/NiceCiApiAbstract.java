package cokr.xit.ci.api.service.support.rest.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@Slf4j
@SuperBuilder
public class NiceCiApiAbstract {

    protected final String PRODUCT_ID = "2101466024";

    protected final ObjectMapper mapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);





    /**
     * <pre>메소드 설명: API 호출
     * </pre>
     *
     * @param method
     * @param url
     * @param body
     * @param headers
     * @return ResponseEntity 요청처리 후 응답객체
     * @author: 박민규
     */
    protected final ResponseEntity<String> callApi(HttpMethod method, String url, Object body, HttpHeaders headers) {

        ResponseEntity<String> responseEntity = null;
        try {
            //uri 및 entity(param) 설정
            HttpEntity<?> entity = null;
            UriComponents uri = null;
            switch (method) {
                case GET:
                    entity = new HttpEntity<>(headers);
                    uri = UriComponentsBuilder
                            .fromHttpUrl(String.format("%s?%s", url, body == null ? "" : body))
//							.encode(StandardCharsets.UTF_8)	//"%"기호가 "%25"로 인코딩 발생하여 주석처리 함.
                            .build(false);
                    break;
                case POST:
                    entity = new HttpEntity<>(body, headers);
                    uri = UriComponentsBuilder
                            .fromHttpUrl(url)
                            .encode(StandardCharsets.UTF_8)
                            .build();
                    break;

                default:
                    break;
            }


            //api 호출
            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
            factory.setConnectTimeout(3000); //커넥션타임아웃 설정 3초
            factory.setReadTimeout(3000);//타임아웃 설정 3초
            RestTemplate restTemplate = new RestTemplate(factory);
            log.info("  url => " + uri.toString());
            log.info("  method => " + method);
            log.info("  headers => " + entity.getHeaders().toString());
            log.info("  body => " + entity.getBody());
            responseEntity = restTemplate.exchange(URI.create(uri.toString()), method, entity, String.class);  //이 한줄의 코드로 API를 호출해 String타입으로 전달 받는다.

            /*
             * HttpStatus 정보 확인 방법
             * 	-.코드: responseEntity.getStatusCodeValue()
             * 	-.메시지: responseEntity.getStatusCode()
             */

        } catch (HttpServerErrorException e) {
            responseEntity = new ResponseEntity<String>(e.getResponseBodyAsString(), e.getStatusCode());
            log.error(String.format("call API 서버오류[url =>%s param => %s error => %s]", url, body, e.getMessage()));
        } catch (HttpClientErrorException e) {
            responseEntity = new ResponseEntity<String>(e.getResponseBodyAsString(), e.getStatusCode());
            log.error(String.format("call API 클라이언트오류[url =>%s param => %s error => %s]", url, body, e.getMessage()));
        } catch (RestClientException e) {    //timeout 발생 또는 기타 오류...
            responseEntity = new ResponseEntity<String>(e.getMessage(), HttpStatus.REQUEST_TIMEOUT);
            log.error(String.format("RestAPI 호출 오류[url =>%s param => %s error => %s]", url, body, e.getMessage()));
        } catch (Exception e) {
            responseEntity = new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            log.error(String.format("call API 기타오류[url =>%s param => %s error => %s]", url, body, e.getMessage()));
        }

        //결과 응답
        return responseEntity;
    }


    protected final String randomAlphaWord(int wordLength) {
        Random r = new Random();

        StringBuilder sb = new StringBuilder(wordLength);
        for (int i = 0; i < wordLength; i++) {
            char tmp = (char) ('a' + r.nextInt('z' - 'a'));
            sb.append(tmp);

        }
        return sb.toString();

    }
}
