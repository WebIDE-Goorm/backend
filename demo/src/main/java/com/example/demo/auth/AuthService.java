package com.example.demo.auth;

import com.example.demo.common.ErrorMessage;
import com.example.demo.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                request.getUsername(), request.getPassword()
        );

        Authentication auth = authenticationManager.authenticate(authenticationToken);

        return LoginResponse.builder()
                .accessToken(jwtTokenProvider.createToken(auth))
                .build();
    }

    @Transactional
    public void signUp(SignUpRequest request) {
        userRepository.findByUsername(request.getUsername())
                .ifPresent(user -> {
                            throw new IllegalArgumentException(ErrorMessage.DUPLICATE_USER.getMessage());
                        }
                );

        User user = User.builder()
                .password(passwordEncoder.encode(request.getPassword()))
                .username(request.getUsername())
                .build();

        userRepository.save(user);
    }
}
