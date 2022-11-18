package team7.simple.global.security;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import team7.simple.domain.auth.jwt.entity.ActiveAccessToken;
import team7.simple.domain.auth.jwt.entity.JwtExpiration;
import team7.simple.domain.auth.jwt.repository.ActiveAccessTokenRedisRepository;
import team7.simple.domain.auth.jwt.repository.LogoutAccessTokenRedisRepository;
import team7.simple.global.common.ConstValue;
import team7.simple.global.error.ErrorCode;
import team7.simple.global.error.advice.exception.CAuthenticationEntryPointException;
import team7.simple.global.error.advice.exception.CExpiredTokenException;
import team7.simple.global.error.advice.exception.CUserNotActiveException;
import team7.simple.global.error.advice.exception.CWrongTypeTokenException;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtProvider {

    @Value("${spring.jwt.secret}") //암호키는 중요하므로 따로 빼서 관리
    private String secretKey;
    private final UserDetailsService userDetailsService;
    private final LogoutAccessTokenRedisRepository logoutAccessTokenRedisRepository;

    private final ActiveAccessTokenRedisRepository activeAccessTokenRedisRepository;

    private String ROLES = "roles";


    private Key getSigningKey(String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String userAccount, List<String> roles) {
        Claims claims = Jwts.claims().setSubject(userAccount);
        claims.put("roles", roles);
        Date now = new Date();

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + JwtExpiration.ACCESS_TOKEN_EXPIRATION_TIME.getValue()))
                .signWith(getSigningKey(secretKey), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String userAccount, List<String> roles) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(userAccount));
        claims.put("roles", roles);
        Date now = new Date();

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setExpiration(new Date(now.getTime() + JwtExpiration.REFRESH_TOKEN_EXPIRATION_TIME.getValue()))
                .signWith(getSigningKey(secretKey), SignatureAlgorithm.HS256)
                .compact();
    }


    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        if (claims.get(ROLES) == null) { //권한 있는지 확인
            throw new CAuthenticationEntryPointException();
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject()); //pk값을 가지고 loadUserByUsername()을 통해 유저 엔티티를 받는다.
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // 만료된 토큰이여도 refresh token을 검증 후 재발급할 수 있도록 claims 반환
    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(getSigningKey(secretKey)).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public long getExpiration(String token) {
        Date expiration = Jwts.parserBuilder().setSigningKey(getSigningKey(secretKey)).build().parseClaimsJws(token).getBody().getExpiration();
        Date now = new Date();
        return expiration.getTime() - now.getTime();
    }

    public String resolveToken(HttpServletRequest request) {
        return request.getHeader(ConstValue.JWT_HEADER);
    }

    public boolean validateToken(HttpServletRequest request, String jwt) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey(secretKey)).build().parseClaimsJws(jwt);
            if (logoutAccessTokenRedisRepository.existsById(jwt)) {
                request.setAttribute("exception", ErrorCode.EXPIRED_TOKEN_EXCEPTION.getCode());
                return false;
            }
            if (isConflicted(request, jwt)) {
                return false;
            }
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            request.setAttribute("exception", ErrorCode.WRONG_TYPE_TOKEN_EXCEPTION.getCode());
        } catch (ExpiredJwtException e) {
            request.setAttribute("exception", ErrorCode.EXPIRED_TOKEN_EXCEPTION.getCode());
        } catch (UnsupportedJwtException e) {
            request.setAttribute("exception", ErrorCode.UNSUPPORTED_TOKEN_EXCEPTION.getCode());
        } catch (IllegalArgumentException e) {
            request.setAttribute("exception", ErrorCode.ILLEGAL_ARGUMENT_EXCEPTION.getCode());
        } catch (Exception e) {
            log.error("================================================");
            log.error("JwtFilter - doFilterInternal() 오류발생");
            log.error("token : {}", jwt);
            log.error("Exception Message : {}", e.getMessage());
            log.error("Exception StackTrace : {");
            e.printStackTrace();
            log.error("}");
            log.error("================================================");
            request.setAttribute("exception", ErrorCode.INTERNAL_SERVER_ERROR.getCode());
        }
        return false;
    }

    private boolean isConflicted(HttpServletRequest request, String jwt) {
        ActiveAccessToken activeAccessToken = activeAccessTokenRedisRepository.findById(jwt).orElse(null);
        log.info("1");
        if (activeAccessToken == null){
            log.info("2");

            return false;
        }
        if (activeAccessToken.getConflict() == 1) {
            log.info("3");
            request.setAttribute("exception", ErrorCode.LOGIN_CONFLICT_EXCEPTION.getCode());
            return true;
        }
        if (activeAccessToken.getConflict() == 3) {
            request.setAttribute("exception", ErrorCode.ACCESS_DENIED.getCode());
            return true;
        }
        log.info("4");

        return false;
    }

    public void validateTokenForReissue(String jwt) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey(secretKey)).build().parseClaimsJws(jwt);
            if (logoutAccessTokenRedisRepository.existsById(jwt)) {
                throw new CExpiredTokenException();
            }
        } catch (SecurityException | MalformedJwtException e) {
            throw new CWrongTypeTokenException();
        } catch (ExpiredJwtException e) {
            throw new CExpiredTokenException();
        }
    }
}