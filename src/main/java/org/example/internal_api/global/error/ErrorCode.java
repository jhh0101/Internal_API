package org.example.internal_api.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력 값입니다."),
    RIOT_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "해당 라이엇 계정을 찾을 수 없습니다."),
    RIOT_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "R002","라이엇 API 호출 횟수를 초과했습니다. 잠시 후 다시 시도해주세요."),
    RIOT_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "R003","라이엇 서버 통신 중 알 수 없는 에러가 발생했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S999", "서버 내부 오류가 발생했습니다.");

    /*
        400 (Bad Request): 클라이언트 탓. (입력값 오류, 이메일 형식 틀림, 비밀번호 짧음)
        401 (Unauthorized): 인증 안 됨. (로그인 안 함, 토큰 만료됨, 토큰 이상함)
        403 (Forbidden): 권한 없음. (로그인은 했는데, 일반 유저가 관리자 페이지 접근 시도)
        404 (Not Found): 데이터 없음. (없는 회원 조회, 이미 삭제된 경매 상품 조회)
        409 (Conflict): 데이터 충돌. (이미 가입된 이메일로 또 가입 시도, 중복 닉네임)
        500 (Internal Server Error): 서버(내) 탓. (DB 연결 끊김, NullPointerException 등 내가 코드 잘못 짠 경우)
    */

    private final HttpStatus status;
    private final String code;
    private final String message;
}
