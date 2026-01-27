package com.ahmadibrahim.Cloud_Storage_SpringBoot.Model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Data
@Table (name = "files")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadata {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String path;
    private Long size;
    private String mimeType;
    private Long ownerId;
}
