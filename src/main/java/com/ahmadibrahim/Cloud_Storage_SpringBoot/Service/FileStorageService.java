package com.ahmadibrahim.Cloud_Storage_SpringBoot.Service;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import com.ahmadibrahim.Cloud_Storage_SpringBoot.Dto.FileStatistics;
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

    public List<FileResponse> getAllFilesByUsername(String username) {
        return fileMetadataRepository.findByUsersUsername(username).stream()
                .map(file -> new FileResponse(
                        file.getId(),
                        file.getName(),
                        file.getMimeType(),
                        file.getSize()
                )).collect(Collectors.toList());
    }

    public FileResponse getFileById(Long id, String username) {
        return fileMetadataRepository.findByIdAndUsersUsername(id, username)
                .map(file -> new FileResponse(
                        file.getId(),
                        file.getName(),
                        file.getMimeType(),
                        file.getSize()
                )).orElse(null);
    }

    public FileMetadata getFileMetadataById(Long id, String username) {
        return fileMetadataRepository.findByIdAndUsersUsername(id, username)
                .orElseThrow(() -> new RuntimeException("FIle not found or access denied"));
    }

    @Transactional
    public FileResponse saveFile(MultipartFile file, String username) throws Exception {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("File is empty");
            }

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Username Not Found"));

            ensureBucketIsExists();
            String path = user.getId() + "/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setName(file.getOriginalFilename());
            fileMetadata.setPath(path);
            fileMetadata.setSize(file.getSize());
            fileMetadata.setMimeType(file.getContentType());
            fileMetadata.setUsers(user);
            FileMetadata savedMetadata = fileMetadataRepository.save(fileMetadata);

            System.out.println("file uploaded to minio by user " + username + ": " + " path");
            return new FileResponse(
                    fileMetadata.getId(),
                    fileMetadata.getName(),
                    fileMetadata.getMimeType(),
                    fileMetadata.getSize()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to minio " + e.getMessage(), e);
        }
    }

    public Resource download(Long fileId, String username) {
        try {
            FileMetadata fileMetadata = fileMetadataRepository.findByIdAndUsersUsername(fileId, username)
                    .orElseThrow(() -> new RuntimeException("File Not Found Or Access Denied"));


            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileMetadata.getPath())
                            .build());

            return new InputStreamResource(stream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to download " + e.getMessage(), e);
        }
    }

    @Transactional
    public boolean deleteFile(Long fileId, String username) {
        try {
            FileMetadata fileMetadata = fileMetadataRepository.findByIdAndUsersUsername(fileId, username)
                    .orElseThrow(() -> new RuntimeException("File Not Found Or Access Denied"));

            minioClient.removeObject(
                    RemoveObjectArgs
                            .builder()
                            .bucket(bucketName)
                            .object(fileMetadata.getPath())
                            .build()
            );

            fileMetadataRepository.deleteById(fileId);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file " + e.getMessage(), e);
        }

    }

    public FileStatistics getFileStatisticsByUsername(String username) {
        List<FileMetadata> fileMetadata = fileMetadataRepository.findByUsersUsername(username);
        long totalFiles = fileMetadata.size();
        long totalSize = fileMetadata.stream().mapToLong(FileMetadata::getSize).sum();
        long imageCount = fileMetadata.stream()
                .filter(f -> f.getMimeType() != null && f.getMimeType().startsWith("image/"))
                .count();
        long videoCount = fileMetadata.stream()
                .filter(f -> f.getMimeType() != null && f.getMimeType().startsWith("video/"))
                .count();
        long documentCount = fileMetadata.stream()
                .filter(f -> f.getMimeType() != null &&
                        (f.getMimeType().contains("document") ||
                                f.getMimeType().contains("pdf") ||
                                f.getMimeType().contains("word") ||
                                f.getMimeType().contains("excel")))
                .count();

        return new FileStatistics(
                totalFiles,
                totalSize,
                imageCount,
                videoCount,
                documentCount
        );
    }

    /*
     * HELPER FUNCTION
     */
    public void ensureBucketIsExists() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    public FileMetadata getMetadata(Long fileId) {
        return fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File dengan ID " + fileId + "tidak ditemukan"));
    }
}
