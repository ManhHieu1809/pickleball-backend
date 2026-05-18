package com.pickleball.presentation.controllers;

import com.pickleball.domain.services.MediaUploadService;
import com.pickleball.presentation.helpers.ResponseHelper;
import com.pickleball.presentation.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaUploadService mediaUploadService;

    public MediaController(MediaUploadService mediaUploadService) {
        this.mediaUploadService = mediaUploadService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadMedia(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseHelper.badRequest("File is empty");
        }

        try {
            boolean isVideo = file.getContentType() != null && file.getContentType().startsWith("video/");
            String url = mediaUploadService.uploadFile(
                    file.getOriginalFilename(),
                    file.getInputStream(),
                    file.getContentType(),
                    isVideo
            );

            return ResponseHelper.ok(url, "Media uploaded successfully");
        } catch (IOException e) {
            return ResponseHelper.internalError("Could not process file: " + e.getMessage());
        }
    }
}
