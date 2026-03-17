package org.example.internal_api.global.config;

import lombok.extern.slf4j.Slf4j;
import org.example.internal_api.global.error.CustomException;
import org.example.internal_api.global.error.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

@Slf4j
@Configuration
public class RiotRestTemplateConfig {
    @Value("${riot.api.key}")
    private String riotKey;

    @Bean
    public RestTemplate riotRestTemplate(RestTemplateBuilder builder) {
        return builder
                .rootUri("https://asia.api.riotgames.com")          // 베이스 url 고정
                .connectTimeout(Duration.ofSeconds(3))              // 3초간 연결 시도(연결 안 되면 포기)
                .readTimeout(Duration.ofSeconds(5))                 // 5초간 읽기 시도(응답 안 오면 포기)
                .additionalInterceptors(riotAuthInterceptor())      // 헤더 스텔스 주입
                .errorHandler(riotErrorHandler())                   // 에러 낚아채기
                .build();
    }

    // 인터셉터: API 요청할 때마다 중간에 가로채서 API 키를 쑤셔 넣습니다.
    private ClientHttpRequestInterceptor riotAuthInterceptor() {
        return (HttpRequest request, byte[] body, ClientHttpRequestExecution execution) -> {
            log.info("[Riot API 요청] URI : {}", request.getURI());
            log.info("[헤더 주입 확인] API Key 앞부분: {}",
                    riotKey.length() > 5 ? riotKey.substring(0, 5) : "키가 너무 짧거나 비어있음");

            // 헤더에 토큰 저장
            request.getHeaders().add("X-Riot-Token", riotKey.trim());

            return execution.execute(request, body);
        };
    }

    private ResponseErrorHandler riotErrorHandler() {
        return new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError();
            }

            @Override
            public void handleError(URI url, HttpMethod method,ClientHttpResponse response) throws IOException {
                log.error("[Riot API 에러] 요청 주소: {}, 메서드: {}, 상태코드 : ",url, method, response.getStatusCode());

                HttpStatus status = (HttpStatus) response.getStatusCode();

                if (status == HttpStatus.NOT_FOUND) {
                    throw new CustomException(ErrorCode.RIOT_USER_NOT_FOUND);
                } else if (status == HttpStatus.TOO_MANY_REQUESTS) {
                    throw new CustomException(ErrorCode.RIOT_RATE_LIMIT_EXCEEDED);
                } else {
                    throw new CustomException(ErrorCode.RIOT_SERVER_ERROR);
                }

            }
        };
    }
}
