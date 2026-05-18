package com.pickleball.infrastructure.external;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.pickleball.domain.services.MediaUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryUploadAdapter implements MediaUploadService {

    private final Cloudinary cloudinary;

    public CloudinaryUploadAdapter(
            @Value("${cloudinary.cloud_name:dummy_cloud}") String cloudName,
            @Value("${cloudinary.api_key:dummy_key}") String apiKey,
            @Value("${cloudinary.api_secret:dummy_secret}") String apiSecret) {

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        this.cloudinary = new Cloudinary(config);
    }

    @Override
    public String uploadFile(String fileName, InputStream inputStream, String contentType, boolean isVideo) {
        try {
            // Determine resource type based on flag or content type
            String resourceType = isVideo ? "video" : "image";

            // Upload parameters
            Map<String, Object> params = ObjectUtils.asMap(
                    "resource_type", resourceType
                   // Could add specific folders or transformation here if needed
            );

            // Read all bytes, cloudinary upload supports byte array (reading whole stream)
            byte[] bytes = inputStream.readAllBytes();

            Map<String, Object> uploadResult = cloudinary.uploader().upload(bytes, params);
            return uploadResult.get("secure_url").toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload media to Cloudinary", e);
        }
    }
}

