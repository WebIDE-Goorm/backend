package com.example.demo.common;

import com.example.demo.auth.User;
import com.example.demo.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * 보안 관련 유틸리티 클래스
 * JWT 토큰에서 사용자 정보를 추출하는 공통 메서드
 */
@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final UserRepository userRepository;
    
    public Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new CustomException(ErrorMessage.UNAUTHORIZED);
        }

        String username;
        if (authentication.getPrincipal() instanceof UserDetails) {
            username = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else {
            username = authentication.getName();
        }

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new CustomException(ErrorMessage.USER_NOT_FOUND));

        return user.getId();
    }
}
