package team7.simple.global.error.advice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import team7.simple.domain.record.error.exception.CRatingNotFoundException;
import team7.simple.domain.study.error.exception.CStudyNotFoundException;
import team7.simple.global.common.response.service.ResponseService;
import team7.simple.global.error.ErrorCode;
import team7.simple.global.error.advice.exception.*;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class ExceptionAdvice {
    private final ResponseService responseService;

    /**
     * 서버 내부에서 에러가 발생할 경우
     * @param e Exception
     * @return INTERNAL_SERVER_ERROR 500
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<?> defaultException(Exception e) {
        e.printStackTrace();
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 입력 형식이 잘못되었을 때 발생하는 예외
     * @param e IllegalArgumentException
     * @return BAD_REQUEST 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<?> handle(IllegalArgumentException e) {
        log.error(ErrorCode.ILLEGAL_ARGUMENT_EXCEPTION.getMessage());
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }


}
