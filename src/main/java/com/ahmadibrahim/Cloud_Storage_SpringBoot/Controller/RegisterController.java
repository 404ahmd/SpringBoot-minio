package com.ahmadibrahim.Cloud_Storage_SpringBoot.Controller;

import com.ahmadibrahim.Cloud_Storage_SpringBoot.Dto.AuthRequest;
import com.ahmadibrahim.Cloud_Storage_SpringBoot.Service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class RegisterController {

    private final AuthService authService;
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest authRequest){
        String result = authService.register(authRequest);
        return ResponseEntity.ok(Map.of("Message", result));
    }
}
