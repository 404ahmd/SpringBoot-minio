package com.ahmadibrahim.Cloud_Storage_SpringBoot.Dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FileStatistics {
    private Long totalFiles;
    private Long totalSize;
    private Long imageCount;
    private Long documentCount;
    private Long videoCount;
}
