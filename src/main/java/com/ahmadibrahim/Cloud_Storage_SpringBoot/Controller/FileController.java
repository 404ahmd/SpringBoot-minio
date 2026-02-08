package com.ahmadibrahim.Cloud_Storage_SpringBoot.Controller;

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
import com.ahmadibrahim.Cloud_Storage_SpringBoot.Dto.UploadResponse;
import com.ahmadibrahim.Cloud_Storage_SpringBoot.Model.FileMetadata;
import com.ahmadibrahim.Cloud_Storage_SpringBoot.Service.FileStorageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) throws Exception{
        if (file.isEmpty()){
            return ResponseEntity.badRequest().build();
        }

        String username = authentication.getName();
        System.out.println("User " + username + " is uploading file with");

        FileMetadata metadata = fileStorageService.saveFile(file, username);
        return ResponseEntity.ok(UploadResponse.builder()
                .fileId(metadata.getId().toString())
                .fileName(metadata.getName())
                .message("Upload berhasil")
                .size(metadata.getSize()).build());
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> donwload(@PathVariable Long fileId, @RequestParam(required = false) String token, Authentication authentication) throws Exception{
        String username = authentication != null ? authentication.getName() : "anonymous";
        System.out.println("User " + username + " is downloading file with ID: " + fileId);

        FileMetadata metadata = fileStorageService.getMetadata(fileId);
        if (metadata == null){
            return ResponseEntity.notFound().build();
        }

        Resource resource = fileStorageService.download(metadata.getPath());
        if (resource == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + metadata.getName() + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(metadata.getSize()))
                .body(resource);
    }

    @DeleteMapping("/delete/{fileId}")
    public ResponseEntity<String> deleteFile(@PathVariable Long fileId)throws Exception{
        fileStorageService.delete(fileId);
        return ResponseEntity.ok("File berhasil dihapus");
    }

    @GetMapping("/get/all")
    public ResponseEntity<List<FileResponse>> listAllFiles(Authentication authentication) {
        try {
            String username = authentication.getName();
            System.out.println("User " + username + " is fetching all files");
            
            List<FileResponse> files = fileStorageService.getAllFiles();
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}


