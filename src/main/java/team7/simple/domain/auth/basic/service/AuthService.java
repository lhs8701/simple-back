package team7.simple.domain.auth.basic.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team7.simple.domain.auth.basic.dto.LoginRequestDto;
import team7.simple.domain.auth.basic.dto.RemoveConflictRequestDto;
import team7.simple.domain.auth.basic.dto.SignupRequestDto;
import team7.simple.domain.auth.jwt.dto.TokenRequestDto;
import team7.simple.domain.auth.jwt.dto.TokenResponseDto;
import team7.simple.domain.auth.jwt.entity.ActiveAccessToken;
import team7.simple.domain.auth.jwt.entity.JwtExpiration;
import team7.simple.domain.auth.jwt.entity.LogoutAccessToken;
import team7.simple.domain.auth.jwt.entity.RefreshToken;
import team7.simple.domain.auth.jwt.repository.ActiveAccessTokenRedisRepository;
import team7.simple.domain.auth.jwt.repository.LogoutAccessTokenRedisRepository;
import team7.simple.domain.auth.jwt.repository.RefreshTokenRedisRepository;
import team7.simple.domain.player.service.PlayerService;
import team7.simple.domain.user.entity.User;
import team7.simple.domain.user.repository.UserJpaRepository;
import team7.simple.global.common.constant.ActiveStatus;
import team7.simple.global.error.advice.exception.*;
import team7.simple.global.security.JwtProvider;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserJpaRepository userJpaRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    private final LogoutAccessTokenRedisRepository logoutAccessTokenRedisRepository;
    private final ActiveAccessTokenRedisRepository activeAccessTokenRedisRepository;

    private final PlayerService playerService;

    public String signup(SignupRequestDto signupRequestDto) {
        if (userJpaRepository.findByAccount(signupRequestDto.getAccount()).isPresent())
            throw new CUserExistException();
        return userJpaRepository.save(signupRequestDto.toEntity(passwordEncoder)).getId();
    }

    public TokenResponseDto login(LoginRequestDto loginRequestDto) {
        User user = userJpaRepository.findByAccount(loginRequestDto.getAccount()).orElseThrow(CUserNotFoundException::new);

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword()))
            throw new CWrongPasswordException();

        String accessToken = jwtProvider.generateAccessToken(user.getAccount(), user.getRoles());
        String refreshToken = jwtProvider.generateRefreshToken(user.getAccount(), user.getRoles());

        refreshTokenRedisRepository.save(new RefreshToken(user.getId(), refreshToken));

        return TokenResponseDto.builder()
                .grantType("bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void logout(String accessToken, User user) {
        long remainMilliSeconds = jwtProvider.getExpiration(accessToken);

        refreshTokenRedisRepository.deleteById(user.getId());
        logoutAccessTokenRedisRepository.save(new LogoutAccessToken(accessToken, remainMilliSeconds));
        activeAccessTokenRedisRepository.deleteById(accessToken);
    }

    public void withdrawal(String accessToken, User user) {
        logout(accessToken, user);
        userJpaRepository.deleteById(user.getId());
    }

    public TokenResponseDto reissue(TokenRequestDto tokenRequestDto) {

        String existAccessToken = tokenRequestDto.getAccessToken();
        String existRefreshToken = tokenRequestDto.getRefreshToken();

        jwtProvider.validateTokenForReissue(existAccessToken);

        Authentication authentication = jwtProvider.getAuthentication(existAccessToken);
        User user = (User) authentication.getPrincipal();

        RefreshToken existRedisRefreshToken = refreshTokenRedisRepository.findById(user.getId()).orElseThrow(CRefreshTokenExpiredException::new);

        if (existRefreshToken.equals(existRedisRefreshToken.getRefreshToken())) {
            String newAccessToken = jwtProvider.generateAccessToken(user.getAccount(), user.getRoles());
            String newRefreshToken = jwtProvider.generateRefreshToken(user.getAccount(), user.getRoles());

            ActiveAccessToken finded = activeAccessTokenRedisRepository.findById(existAccessToken).orElse(null);
            if (finded != null) {
                activeAccessTokenRedisRepository.save(ActiveAccessToken.builder()
                        .accessToken(newAccessToken)
                        .userId(user.getId())
                        .conflict(finded.getConflict())
                        .expiration(JwtExpiration.ACCESS_TOKEN_EXPIRATION_TIME.getValue())
                        .build());
                activeAccessTokenRedisRepository.delete(finded);
            }
            refreshTokenRedisRepository.save(new RefreshToken(user.getId(), newRefreshToken));
            return TokenResponseDto.builder()
                    .grantType("bearer")
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build();
        } else {
            throw new CWrongRefreshTokenException();
        }
    }

    public void removeConflict(RemoveConflictRequestDto removeConflictRequestDto) {
        Authentication authentication = jwtProvider.getAuthentication(removeConflictRequestDto.getAccessToken());
        User user = (User) authentication.getPrincipal();
        ActiveAccessToken currentAccessToken = activeAccessTokenRedisRepository
                .findById(removeConflictRequestDto.getAccessToken())
                .orElseThrow(CUserNotFoundException::new);
        ActiveAccessToken otherAccessToken = activeAccessTokenRedisRepository
                .findByUserIdAndConflict(user.getId(), 2)
                .orElseThrow(CUserNotFoundException::new);
        if (currentAccessToken.getConflict() != ActiveStatus.PRE_CONFLICTED.ordinal()) {
            throw new CWrongTypeTokenException();
        }

        if (removeConflictRequestDto.isKeepGoing()) {
            playerService.updateConflictStatus(currentAccessToken, ActiveStatus.POST_CONFLICTED.ordinal());
            playerService.updateConflictStatus(otherAccessToken, ActiveStatus.PRE_CONFLICTED.ordinal());
        } else {
            activeAccessTokenRedisRepository.delete(currentAccessToken);
            playerService.updateConflictStatus(otherAccessToken, ActiveStatus.NO_CONFLICT.ordinal());
        }
    }
}
