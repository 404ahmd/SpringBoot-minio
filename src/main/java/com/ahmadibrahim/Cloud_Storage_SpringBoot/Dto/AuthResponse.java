package com.ahmadibrahim.Cloud_Storage_SpringBoot.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.awt.*;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private String username;
}
