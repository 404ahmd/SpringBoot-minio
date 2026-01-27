package com.ahmadibrahim.Cloud_Storage_SpringBoot.Service;

import com.ahmadibrahim.Cloud_Storage_SpringBoot.Dto.FileResponse;
import com.ahmadibrahim.Cloud_Storage_SpringBoot.Model.FileMetadata;
import com.ahmadibrahim.Cloud_Storage_SpringBoot.Repository.FileMetadataRepository;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final MinioClient minioClient;
    private final FileMetadataRepository fileMetadataRepository;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Transactional
    public FileMetadata saveFile(MultipartFile file, Long ownerId) throws Exception{
        ensureBucketIsExists();

        String path = ownerId + "/" + file.getOriginalFilename();
        FileMetadata metadata = new FileMetadata();
        metadata.setName(file.getOriginalFilename());
        metadata.setPath(path);
        metadata.setSize(file.getSize());
        metadata.setMimeType(file.getContentType());
        metadata.setOwnerId(ownerId);
        FileMetadata savedMetadata = fileMetadataRepository.save(metadata);
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        log.info("File berhasil diupload");
        return savedMetadata;
    }

    public byte[] downloadFile(String path){
        try(InputStream stream = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucketName).object(path).build()
        )){
            return stream.readAllBytes();
        }catch (Exception e){
            log.error("gagal mengambil file dari storage", e.getMessage());
            throw new RuntimeException("Gagal megambil file dari storage");
        }
    }

    public void ensureBucketIsExists() throws Exception{
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists){
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    public List<FileResponse> getAllFiles(){
        return fileMetadataRepository.findAll().stream()
                .map(file -> new FileResponse(
                        file.getId(),
                        file.getName(),
                        file.getMimeType(),
                        file.getSize()
                )).collect(Collectors.toList());
    }

    public void delete(Long fileId) throws Exception{
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(()->new RuntimeException("File tidak ditemukan"));

        minioClient.removeObject(
                RemoveObjectArgs
                        .builder()
                        .bucket(bucketName)
                        .object(metadata.getPath())
                        .build()
        );

        fileMetadataRepository.delete(metadata);
    }
    public FileMetadata getMetadata(Long fileId){
        return fileMetadataRepository.findById(fileId).orElseThrow(()
                -> new RuntimeException("File dengan ID " + fileId + "tidak ditemukan"));
    }
}
