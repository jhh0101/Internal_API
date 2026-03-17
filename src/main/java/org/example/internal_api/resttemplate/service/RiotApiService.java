package org.example.internal_api.resttemplate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.internal_api.global.error.CustomException;
import org.example.internal_api.resttemplate.dto.RiotAccountResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiotApiService {

    // 변수명을 빈 이름과 맞춰주면 스프링이 알아서 매칭해줌
    private final RestTemplate riotRestTemplate;

    @Retryable(                                 // 자동 실행
            retryFor = {CustomException.class}, // CustomException 에러가 터지면
            maxAttempts = 3,                    // 최대 3번(3이 기본 값)
            backoff = @Backoff(delay = 1000)    // 다시 요청 후 딜레이 1초
    )
    public RiotAccountResponse getAccountInfo(String gameName, String tagLine) {
            // Bean에 rootUri가 등록되어 있지 않거나, 전체 주소(https://...)를 다 써야 할 때
            // 혹은 쿼리 파라미터(?count=20&start=0)가 복잡하게 붙을 때 사용!
//        URI uriEx = UriComponentsBuilder
//                .fromUriString("/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}")
//                .queryParam("start", 0)   // ?start=0
//                .queryParam("count", 20)  // &count=20
//                .buildAndExpand(gameName, tagLine)
//                .encode()
//                .toUri(); // 반드시 .toUri()로 변환해서 RestTemplate에 넘겨야 이중 인코딩이 안 일어남

        // String response = riotRestTemplate.getForObject(uri, String[].class); // .buildAndExpand(gameName, tagLine)으로 변수를 삽입했으므로 파라미터에 넣지 않음

        String uri = "/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}";

        log.info("[Riot API 호출] 닉네임 : {}, 태그 : {}, 엔드 포인트 : {}", gameName, tagLine, uri);

        RiotAccountResponse response = riotRestTemplate.getForObject(uri, RiotAccountResponse.class, gameName, tagLine);    // 변수는 주소(uri)에 들어간 변수(/{gameName}/{tagLine}) 순서로 전달

        log.info("[Riot API 응답 성공] 추출한 PUUID : {}", response.puuid());
        return response;
    }

    public List<String> getMatchIds(String puuid) {
        String uri = "/lol/match/v5/matches/by-puuid/{puuid}/ids?start={start}&count={count}";

        log.info("[매치 리스트 조회] PUUID : {}", puuid);

//        String[] response = riotRestTemplate.getForObject(uri, String[].class, puuid, 0, 20);

//        return response != null ? Arrays.asList(response) : Collections.emptyList();

        // List<String>으로 변환 해서 리턴하고 싶을 때 ParameterizedTypeReference 사용(익명 클래스 방식이므로 {}를 끝에 사용)
        ParameterizedTypeReference<List<String>> typeRef = new ParameterizedTypeReference<List<String>>() {};

        ResponseEntity<List<String>> response = riotRestTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,    // 요청 바디나 추가 헤더가 없으면 null
                typeRef,            // ParameterizedTypeReference로 만든 타입 전달
                puuid
        );

        return response.getBody(); // 결과 꺼내기
    }

    public Object getMatchDetail(String matchId) {
        String uri = "/lol/match/v5/matches/{matchId}";

        log.info("[매치 상세 조회] Match ID : {}", matchId);

        return riotRestTemplate.getForObject(uri, Object.class, matchId);

    }

    @Recover // @Retryable를 사용하는 메서드를 실패 했을 때(파라미터명 매치 시켜줘야 인식함[String gameName, String tagLine])
    public String recover(CustomException e, String gameName, String tagLine) {
        log.error("[Riot API 3회 호출 호출 실패] 에러 : {}", e.getErrorCode().getMessage());

        throw e;
    }
}
