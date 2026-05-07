package com.optcg.deckbuilder.service;

import com.optcg.deckbuilder.model.dto.auth.AuthResponse;
import com.optcg.deckbuilder.model.dto.auth.LoginRequest;
import com.optcg.deckbuilder.model.dto.auth.RegisterRequest;
import com.optcg.deckbuilder.model.entity.User;
import com.optcg.deckbuilder.model.enums.Role;
import com.optcg.deckbuilder.exception.ConflictException;
import com.optcg.deckbuilder.repository.UserRepository;
import com.optcg.deckbuilder.security.JwtUtil;
import com.optcg.deckbuilder.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TotpService totpService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already in use");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(user);

        // Auto login after register
        return authenticateAndGenerateToken(request.getUsername(), request.getPassword());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (user.isTotpEnabled()) {
            if (request.getTotpCode() == null) {
                return AuthResponse.builder()
                        .requiresTotp(true)
                        .username(user.getUsername())
                        .build();
            }
            
            if (!totpService.validateCode(user.getTotpSecret(), request.getTotpCode())) {
                throw new BadCredentialsException("Invalid 2FA code");
            }
        }

        String jwt = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(jwt)
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .avatarUrl(userDetails.getAvatarUrl())
                .requiresTotp(false)
                .build();
    }

    private AuthResponse authenticateAndGenerateToken(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(jwt)
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .avatarUrl(userDetails.getAvatarUrl())
                .requiresTotp(false)
                .build();
    }
}
