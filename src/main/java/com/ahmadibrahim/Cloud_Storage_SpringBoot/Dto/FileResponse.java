package com.ahmadibrahim.Cloud_Storage_SpringBoot.Dto;

import lombok.*;

@Data
@AllArgsConstructor
public class FileResponse {
    private Long id;
    private String fileName;
    private String mimeType;
    private Long size;
}
