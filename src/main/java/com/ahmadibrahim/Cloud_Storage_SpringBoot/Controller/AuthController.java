package com.ahmadibrahim.Cloud_Storage_SpringBoot.Controller;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ahmadibrahim.Cloud_Storage_SpringBoot.Dto.AuthRequest;
import com.ahmadibrahim.Cloud_Storage_SpringBoot.Dto.AuthResponse;
import com.ahmadibrahim.Cloud_Storage_SpringBoot.Service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?>login(@RequestBody AuthRequest authRequest){
        try{
            AuthResponse authResponse = authService.login(authRequest);
            return ResponseEntity.ok(authResponse);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("Error", "Login Gagal" + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request){
        return ResponseEntity.ok(Map.of("message", "Logout berhasil"));
    }

    @GetMapping("/sayHello")
    public String sayHello(){
        return "Hello World";
    }
}
