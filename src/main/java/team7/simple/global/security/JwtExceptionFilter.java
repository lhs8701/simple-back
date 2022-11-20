package team7.simple.global.security;

import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import team7.simple.global.error.ErrorCode;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class JwtExceptionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        try {
            chain.doFilter(request, response);
        } catch (JwtException e) {
            setErrorResponse(response, e);
        }
    }

    public void setErrorResponse(HttpServletResponse response, Throwable e) {
        if (e.getMessage().equals(String.valueOf(ErrorCode.LOGIN_CONFLICT_EXCEPTION.getCode()))) {
            log.error(ErrorCode.LOGIN_CONFLICT_EXCEPTION.getMessage());
            response.setStatus(HttpStatus.CONFLICT.value());
        }
        if (e.getMessage().equals(String.valueOf(ErrorCode.EXPIRED_TOKEN_EXCEPTION.getCode()))) {
            log.error(ErrorCode.EXPIRED_TOKEN_EXCEPTION.getMessage());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
        if (e.getMessage().equals(String.valueOf(ErrorCode.WRONG_TYPE_TOKEN_EXCEPTION.getCode()))) {
            log.error(ErrorCode.WRONG_TYPE_TOKEN_EXCEPTION.getMessage());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
        if (e.getMessage().equals(String.valueOf(ErrorCode.UNSUPPORTED_TOKEN_EXCEPTION.getCode()))) {
            log.error(ErrorCode.UNSUPPORTED_TOKEN_EXCEPTION.getMessage());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
        if (e.getMessage().equals(String.valueOf(ErrorCode.ILLEGAL_ARGUMENT_EXCEPTION.getCode()))) {
            log.error(ErrorCode.ILLEGAL_ARGUMENT_EXCEPTION.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
        }
    }
}
