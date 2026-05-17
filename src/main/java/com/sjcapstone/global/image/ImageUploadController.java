package com.sjcapstone.global.image;

import com.sjcapstone.global.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/images")
@RequiredArgsConstructor
public class ImageUploadController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${app.base-url}")
    private String baseUrl;

    @PostMapping
    public ResponseEntity<CommonResponse<ImageUploadResponse>> upload(
            @RequestParam("file") MultipartFile file) throws IOException {

        Path dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        String ext = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + ext;
        Files.copy(file.getInputStream(), dir.resolve(filename));

        String imageUrl = baseUrl + "/images/" + filename;
        return ResponseEntity.ok(CommonResponse.ok("이미지 업로드 성공", new ImageUploadResponse(imageUrl)));
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}