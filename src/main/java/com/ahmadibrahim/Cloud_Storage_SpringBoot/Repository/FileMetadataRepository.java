package com.ahmadibrahim.Cloud_Storage_SpringBoot.Repository;

import com.ahmadibrahim.Cloud_Storage_SpringBoot.Model.FileMetadata;
import com.ahmadibrahim.Cloud_Storage_SpringBoot.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    List<FileMetadata> findByUsers(User user);
    List<FileMetadata> findByUsersUsername(String username);
    Optional<FileMetadata> findByIdAndUsers(Long id, User user);
    Optional<FileMetadata> findByIdAndUsersUsername(Long id, String username);
    List<FileMetadata> findByMimeTypeAndUsers(String mimeType, User user);
    List<FileMetadata> findByMimeTypeAndUsersUsername(String mimeType, String username);
}
