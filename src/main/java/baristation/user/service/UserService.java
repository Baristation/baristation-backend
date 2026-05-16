package baristation.user.service;

import baristation.common.exception.CustomException;
import baristation.common.exception.ErrorCode;
import baristation.common.logging.TraceIdUtil;
import baristation.common.r2.R2ImageService;
import baristation.common.redis.RedisService;
import baristation.security.jwt.JwtTokenProvider;
import baristation.security.payload.dto.TokenPair;
import jakarta.servlet.http.HttpServletRequest;
import baristation.user.domain.User;
import baristation.user.payload.dto.UserUpdateRequest;
import baristation.user.repository.UserRepository;
import baristation.user.validator.NicknameValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final UserRepository userRepository; // repo
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;
    private final R2ImageService r2ImageService;
    private final NicknameValidator nicknameValidator;

    public void logout(HttpServletRequest request) {
        String accessToken = resolveToken(request);
        Long userId = extractUserId(accessToken);
        String userIdText = String.valueOf(userId);

        redisService.deleteRefreshToken(userIdText);
        long expiration = jwtTokenProvider.getExpirationToken(accessToken);
        redisService.setBlackList(accessToken, "logout", Duration.ofMillis(expiration));

    }

    public TokenPair refresh(String refreshToken) {
        // null/blank 체크
        if (!StringUtils.hasText(refreshToken)) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_REQUIRED);
        }

        // validateRefreshToken은 CustomException을 던지므로 catch 불필요
        jwtTokenProvider.validateRefreshToken(refreshToken);

        Long userId = extractUserId(refreshToken);
        String userIdText = String.valueOf(userId);


        String savedUserRefreshToken = redisService.getRefreshToken(userIdText);
        if (savedUserRefreshToken == null || !savedUserRefreshToken.equals(refreshToken)) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }
        User user = userRepository.getUserByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        TokenPair newTokenPair = jwtTokenProvider.createTokenSet(user);
        redisService.setRefreshToken(userIdText, newTokenPair.refreshToken());
        return newTokenPair;
    }

    public void deleteUser(HttpServletRequest request) {
        String accessToken = resolveToken(request);
        Long userId = extractUserId(accessToken);
        String userIdText = String.valueOf(userId);

        User user = userRepository.getUserByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
        redisService.deleteRefreshToken(userIdText);

        long expiration = jwtTokenProvider.getExpirationToken(accessToken);
        redisService.setBlackList(accessToken, "delete", Duration.ofMillis(expiration));

    }

    /**
     * 회원 정보 수정 (닉네임, 이미지)
     * - 닉네임 검증 (형식, 금지어, 특수문자)
     * - 중복 체크 (대소문자 무시)
     * - 프로필 이미지 처리
     */
    public void updateUser(Long userId, UserUpdateRequest updateRequest, MultipartFile profileImage) {
        User user = userRepository.getUserByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (updateRequest == null || !StringUtils.hasText(updateRequest.nickname())) {
            throw new CustomException(ErrorCode.USER_NICKNAME_REQUIRED);
        }

        String newNickname = updateRequest.nickname();

        // 1. 닉네임 형식 검증 (길이, 허용 문자, 금지어, 특수문자)
        nicknameValidator.validate(newNickname);

        // 2. 중복 체크 (대소문자 무시, 자신의 현재 닉네임 제외)
        if (!user.getNickname().equalsIgnoreCase(newNickname)
                && userRepository.existsByNicknameIgnoreCase(newNickname)) {
            throw new CustomException(ErrorCode.USER_NICKNAME_DUPLICATE);
        }

        // 3. 검증 완료된 닉네임 업데이트
        user.updateNickname(newNickname);

        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                String oldImageUrl = user.getProfileImageUrl();
                if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                    r2ImageService.deleteByUrl(oldImageUrl);
                }

                String newImageUrl = r2ImageService.uploadProfileImage(profileImage, userId);
                user.updateProfileImageUrl(newImageUrl);
            } catch (IOException e) {
                throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
            }
        }


    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new CustomException(ErrorCode.TOKEN_INVALID);
    }

    private Long extractUserId(String token) {
        String subject = jwtTokenProvider.getSubject(token);

        if (!StringUtils.hasText(subject)) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }

        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }
    }
}
