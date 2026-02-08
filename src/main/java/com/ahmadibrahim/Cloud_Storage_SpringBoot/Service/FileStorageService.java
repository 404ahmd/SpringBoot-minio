package com.ahmadibrahim.Cloud_Storage_SpringBoot.Service;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ahmadibrahim.Cloud_Storage_SpringBoot.Dto.FileResponse;
import com.ahmadibrahim.Cloud_Storage_SpringBoot.Model.FileMetadata;
import com.ahmadibrahim.Cloud_Storage_SpringBoot.Model.User;
import com.ahmadibrahim.Cloud_Storage_SpringBoot.Repository.FileMetadataRepository;
import com.ahmadibrahim.Cloud_Storage_SpringBoot.Repository.UserRepository;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final MinioClient minioClient;
    private final FileMetadataRepository fileMetadataRepository;
    private final UserRepository userRepository;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Transactional
    public FileMetadata saveFile(MultipartFile file, String username) throws Exception {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Username Not Found"));

        ensureBucketIsExists();
        String path = user.getId() + "/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();

        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setName(file.getOriginalFilename());
        fileMetadata.setPath(path);
        fileMetadata.setSize(file.getSize());
        fileMetadata.setMimeType(file.getContentType());
        fileMetadata.setUsers(user);
        FileMetadata savedMetadata = fileMetadataRepository.save(fileMetadata);

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());

        return savedMetadata;
    }

    public Resource download(String path) {
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build());

            return new InputStreamResource(stream);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to download " + e.getMessage());
        }
    }

    public void ensureBucketIsExists() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    public User ensureUserIsExists(Long userId) throws Exception {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User with id " + userId + " not found"));

    }

    public List<FileResponse> getAllFiles() {
        return fileMetadataRepository.findAll().stream()
                .map(file -> new FileResponse(
                        file.getId(),
                        file.getName(),
                        file.getMimeType(),
                        file.getSize()))
                .collect(Collectors.toList());
    }

    public void delete(Long fileId) throws Exception {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File tidak ditemukan"));

        minioClient.removeObject(
                RemoveObjectArgs
                        .builder()
                        .bucket(bucketName)
                        .object(metadata.getPath())
                        .build());

        fileMetadataRepository.delete(metadata);
    }

    public FileMetadata getMetadata(Long fileId) {
        return fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File dengan ID " + fileId + "tidak ditemukan"));
    }
}
