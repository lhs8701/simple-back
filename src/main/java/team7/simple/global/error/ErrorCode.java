package team7.simple.global.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND_EXCEPTION(-1, "해당 회원을 조회할 수 없습니다."),
    WRONG_PASSWORD_EXCEPTION(-2, "잘못된 비밀번호 입니다."),

    REFRESH_TOKEN_INVALID_EXCEPTION(-1007, "리프레쉬 토큰이 잘못되었습니다."),
    REFRESH_TOKEN_EXPIRED_EXCPEPTION(-1014, "리프레쉬 토큰이 만료되었습니다. 로그인을 다시 해주세요."),
    AUTHENTICATION_ENTRY_POINT_EXCEPTION(-1002, "해당 리소스에 접근하기 위한 권한이 없습니다."), //정상적으로 Jwt이 제대로 오지 않은 경우
    ACCESS_DENIED(-1003, "해당 리소스에 접근할 수 없는 권한입니다."),

    LOGIN_CONFLICT_EXCEPTION(-1004, "로그인 충돌입니다."),

    USER_EXIST_EXCEPTION(-1006, "이미 가입된 계정입니다. 로그인을 해주세요"),
    COURSE_NOT_FOUND_EXCEPTION(-1009,"해당 강좌를 찾을 수 없습니다."),
    UNIT_NOT_FOUND_EXCEPTION(-1009,"해당 강의를 찾을 수 없습니다."),

    FILE_NOT_FOUND_EXCEPTION(-1010, "해당 파일을 찾을 수 없습니다."),

    STUDY_NOT_FOUND_EXCEPTION(-1011, "수강 중인 강의가 아닙니다."),
    RATING_NOT_FOUND_EXCEPTION(-1012, "등록된 평점이 없습니다."),
    RATING_DUPLICATED_EXCEPTION(-1013, "이미 평점을 등록했습니다."),


    QUESTION_NOT_FOUND_EXCEPTION(-1011, "해당 질문을 찾을 수 없습니다."),
    ANSWER_NOT_FOUND_EXCEPTION(-1012, "해당 답변을 찾을 수 없습니다."),

    WRONG_REFRESH_TOKEN_EXCEPTION(-5000, "refresh 토큰이 잘못되었습니다"),
    WRONG_TYPE_TOKEN_EXCEPTION(-5001, "잘못된 Jwt 서명입니다."),
    EXPIRED_TOKEN_EXCEPTION(-5002, "만료된 토큰입니다."),
    UNSUPPORTED_TOKEN_EXCEPTION(-5003, "지원하지 않는 토큰입니다."),
    ILLEGAL_ARGUMENT_EXCEPTION(-5004, "잘못된 형식입니다."),
    USER_NOT_ACTIVE_EXCEPTION(-5005, "현재 시청중인 계정이 아닙니다."),
    NO_RECORD_EXCEPTION(-5006, "강의를 시청한 기록이 없습니다."),

    WRONG_APPROACH(-9998, "잘못된 접근입니다."),
    INTERNAL_SERVER_ERROR(-9999, "서버 에러입니다.");


    private int code;
    private String message;
}
