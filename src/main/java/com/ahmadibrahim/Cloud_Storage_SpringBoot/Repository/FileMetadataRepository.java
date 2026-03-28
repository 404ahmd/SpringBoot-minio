package com.ahmadibrahim.Cloud_Storage_SpringBoot.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ahmadibrahim.Cloud_Storage_SpringBoot.Model.FileMetadata;
import com.ahmadibrahim.Cloud_Storage_SpringBoot.Model.User;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    List<FileMetadata> findByUsers(User user);
    List<FileMetadata> findByUsersUsername(String username);
    Optional<FileMetadata> findByIdAndUsers(Long id, User user);
    Optional<FileMetadata> findByIdAndUsersUsername(Long id, String username);
    List<FileMetadata> findByMimeTypeAndUsers(String mimeType, User user);
    List<FileMetadata> findByMimeTypeAndUsersUsername(String mimeType, String username);

    @Query("SELECT f FROM FileMetadata f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND f.users.username = :username")
    List<FileMetadata>searchByNameAndUsername(@Param("username") String username, @Param("keyword") String keyword, Pageable pageable);

    // Gunakan And untuk menggabungkan dua parameter
    List<FileMetadata> findByUsersUsernameAndMimeTypeStartingWith(String username, String mimePrefix);
    List<FileMetadata> findByUsersUsername(String username, Sort sort);
}
