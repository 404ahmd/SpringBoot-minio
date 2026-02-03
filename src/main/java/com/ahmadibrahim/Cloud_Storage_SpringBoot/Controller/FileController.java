package com.ahmadibrahim.Cloud_Storage_SpringBoot.Controller;

import com.ahmadibrahim.Cloud_Storage_SpringBoot.Dto.FileResponse;
import com.ahmadibrahim.Cloud_Storage_SpringBoot.Dto.UploadResponse;
import com.ahmadibrahim.Cloud_Storage_SpringBoot.Model.FileMetadata;
import com.ahmadibrahim.Cloud_Storage_SpringBoot.Repository.FileMetadataRepository;
import com.ahmadibrahim.Cloud_Storage_SpringBoot.Service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {
    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("user_id") Long userId
    ) throws Exception{
        if (file.isEmpty()){
            return ResponseEntity.badRequest().build();
        }

        FileMetadata metadata = fileStorageService.saveFile(file, userId);
        return ResponseEntity.ok(UploadResponse.builder()
                .fileId(metadata.getId().toString())
                .fileName(metadata.getName())
                .message("Upload berhasil")
                .size(metadata.getSize()).build());
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> donwload(@PathVariable Long fileId) throws Exception{
        FileMetadata metadata = fileStorageService.getMetadata(fileId);
        byte[] fileData = fileStorageService.downloadFile(metadata.getPath());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getName() + "\"")
                .body(fileData);
    }

    @DeleteMapping("/delete/{fileId}")
    public ResponseEntity<String> deleteFile(@PathVariable Long fileId)throws Exception{
        fileStorageService.delete(fileId);
        return ResponseEntity.ok("File berhasil dihapus");
    }

    @GetMapping("/get/all")
    public ResponseEntity<List<FileResponse>> listAllFiles() {
        return ResponseEntity.ok(fileStorageService.getAllFiles());
    }
}


