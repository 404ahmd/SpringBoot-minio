package com.ahmadibrahim.Cloud_Storage_SpringBoot.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadResponse {
    private String fileId;
    private String fileName;
    private String message;
    private Long size;
}
