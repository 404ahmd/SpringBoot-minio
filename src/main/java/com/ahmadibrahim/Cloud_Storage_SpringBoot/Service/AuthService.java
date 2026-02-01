package com.ahmadibrahim.Cloud_Storage_SpringBoot.Service;

import com.ahmadibrahim.Cloud_Storage_SpringBoot.Dto.AuthRequest;
import com.ahmadibrahim.Cloud_Storage_SpringBoot.Dto.AuthResponse;
import com.ahmadibrahim.Cloud_Storage_SpringBoot.Model.User;
import com.ahmadibrahim.Cloud_Storage_SpringBoot.Repository.UserRepository;
import com.ahmadibrahim.Cloud_Storage_SpringBoot.Util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    public String register(AuthRequest authRequest){
        if (userRepository.findByUsername(authRequest.getUsername()).isPresent()){
            throw new RuntimeException("Username sudah digunakan");
        }

        User user = User.builder()
                .username(authRequest.getUsername())
                .password(passwordEncoder.encode(authRequest.getPassword())).build();
        userRepository.save(user);
        return "User berhasil didaftarkan";
    }

    public AuthResponse login(AuthRequest authRequest){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getUsername(),
                        authRequest.getPassword()
                )
        );

        if (authentication.isAuthenticated()){
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(authRequest.getUsername());
            String token = jwtUtil.generateToken(userDetails);
            AuthResponse authResponse = new AuthResponse();
            authResponse.setToken(token);
            authResponse.setType("Bearer");
            authResponse.setUsername(authRequest.getUsername());
            return authResponse;
        }else {
            throw new RuntimeException("Authentication Failed");
        }
    }
}
