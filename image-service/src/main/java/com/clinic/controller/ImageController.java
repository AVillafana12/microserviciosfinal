package com.clinic.controller;

import com.clinic.image_service.entity.UserImage;
import com.clinic.service.ImagesService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImagesService imagesService;

    public ImageController(ImagesService imagesService) {
        this.imagesService = imagesService;
    }

    private Integer getUserIdFromAuth(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            Object userIdClaim = jwt.getClaim("user_id");
            if (userIdClaim != null) {
                return Integer.valueOf(userIdClaim.toString());
            }
        }
        throw new RuntimeException("User ID not found in token");
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file, 
                                        Authentication authentication) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }

            Integer userId = getUserIdFromAuth(authentication);
            UserImage savedImage = imagesService.uploadImage(file, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("id", savedImage.getId());
            response.put("name", savedImage.getFilename());
            response.put("size", savedImage.getSize());
            response.put("contentType", savedImage.getContentType());
            response.put("uploadedAt", savedImage.getUploadedAt());
            response.put("url", "/api/images/" + savedImage.getId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload file: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id, Authentication authentication) {
        try {
            Integer userId = getUserIdFromAuth(authentication);
            Optional<UserImage> imageOpt = imagesService.getImageById(id);

            if (imageOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            UserImage image = imageOpt.get();
            
            // Verificar que la imagen pertenece al usuario
            if (!image.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                image.getContentType() != null ? image.getContentType() : "application/octet-stream"
            ));
            headers.setContentDispositionFormData("inline", image.getFilename());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(image.getImage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<?> listImages(Authentication authentication) {
        try {
            Integer userId = getUserIdFromAuth(authentication);
            List<UserImage> images = imagesService.getUserImages(userId);

            List<Map<String, Object>> imageList = images.stream()
                    .map(img -> {
                        Map<String, Object> imageInfo = new HashMap<>();
                        imageInfo.put("id", img.getId());
                        imageInfo.put("name", img.getFilename());
                        imageInfo.put("size", img.getSize());
                        imageInfo.put("contentType", img.getContentType());
                        imageInfo.put("uploadedAt", img.getUploadedAt());
                        imageInfo.put("url", "/api/images/" + img.getId());
                        return imageInfo;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(imageList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to list images: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteImage(@PathVariable Long id, Authentication authentication) {
        try {
            Integer userId = getUserIdFromAuth(authentication);
            boolean deleted = imagesService.deleteImage(id, userId);
            
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Image deleted successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Image not found or unauthorized"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete image: " + e.getMessage()));
        }
    }
}
