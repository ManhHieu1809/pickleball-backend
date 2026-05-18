package com.pickleball.domain.services;

import java.io.InputStream;

public interface MediaUploadService {

    String uploadFile(String fileName, InputStream inputStream, String contentType, boolean isVideo);
}

