package com.ahmadibrahim.Cloud_Storage_SpringBoot.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ahmadibrahim.Cloud_Storage_SpringBoot.Dto.FileResponse;
import com.ahmadibrahim.Cloud_Storage_SpringBoot.Model.FileMetadata;
import com.ahmadibrahim.Cloud_Storage_SpringBoot.Service.FileStorageService;

import java.util.Map;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    private final FileStorageService fileStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @RequestParam("files") MultipartFile[] files,
            Authentication authentication) {
        try {
            // ⭐ Get username dari JWT
            String username = authentication.getName();
            List<Map<String, Object>> responses = new ArrayList<>();
            //System.out.println("User " + username + " is uploading file: " + file.getOriginalFilename());

            for (MultipartFile file : files){
                Map<String, Object>status = new HashMap<>();
                status.put("filename", file.getOriginalFilename());
                try {
                    FileResponse uploadedFile = fileStorageService.saveFile(file, username);
                    status.put("success", true);
                    status.put("message", "upload berhasil");
                } catch (Exception e) {
                    status.put("success", false);
                    status.put("message", "Gagal: " + e.getMessage());
                }
                responses.add(status);
            }
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload file to MinIO: " + e.getMessage()));
        }
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<?> downloadFile(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            System.out.println("User " + username + " is downloading file with ID: " + id);
            FileMetadata fileMetadata;
            try {
                fileMetadata = fileStorageService.getFileMetadataById(id, username);
            } catch (RuntimeException e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "access denied"));
            }

            Resource resource = fileStorageService.download(id, username);
            if (resource == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileMetadata.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileMetadata.getName() + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileMetadata.getSize()))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            System.out.println("User " + username + " is deleting file with ID: " + id);
            FileResponse fileInfo = fileStorageService.getFileById(id, username);
            if (fileInfo == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "access denied"));
            }

            boolean deleted = fileStorageService.deleteFile(id, username);
            if (!deleted) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "failed to delete file, INTERNAL SERVER ERROR"));
            }

            return ResponseEntity.ok(Map.of(
                    "message", "file delted success from minio",
                    "file_id", id
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "failed to delete file" + e.getMessage()));
        }
    }

    @GetMapping("/get/all")
    public ResponseEntity<List<FileResponse>> listAllFiles(Authentication authentication) {
        try {
            String username = authentication.getName();
            System.out.println("User " + username + " is fetching all files");

            List<FileResponse> files = fileStorageService.getAllFilesByUsername(username);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}


