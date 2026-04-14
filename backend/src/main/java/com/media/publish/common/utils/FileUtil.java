package com.media.publish.common.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileUtil {

    @Value("${app.upload.path}")
    private static String uploadPath;

    public static String getUploadPath() {
        return "/home/coze/publish/uploads";
    }

    public static String upload(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;
        Path uploadDir = Paths.get(getUploadPath());
        
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path filePath = uploadDir.resolve(newFilename);
        file.transferTo(filePath.toFile());

        return "/uploads/" + newFilename;
    }

    public static boolean delete(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        String fullPath = getUploadPath() + filePath.replace("/uploads", "");
        File file = new File(fullPath);
        return file.exists() && file.delete();
    }
}
