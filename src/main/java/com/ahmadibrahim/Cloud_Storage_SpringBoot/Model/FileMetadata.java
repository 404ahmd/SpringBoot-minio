package com.ahmadibrahim.Cloud_Storage_SpringBoot.Model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Data
@Table (name = "file_metadata")
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

    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "user_id"
    )
    private User users;
}
