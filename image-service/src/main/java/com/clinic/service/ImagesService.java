package com.clinic.service;

import com.clinic.image_service.entity.UserImage;
import com.clinic.repository.ImagesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ImagesService {

    private final ImagesRepository imagesRepository;

    public ImagesService(ImagesRepository imagesRepository) {
        this.imagesRepository = imagesRepository;
    }

    @Transactional
    public UserImage uploadImage(MultipartFile file, Integer userId) throws IOException {
        UserImage userImage = new UserImage();
        userImage.setUserId(userId);
        userImage.setImage(file.getBytes());
        userImage.setFilename(file.getOriginalFilename());
        userImage.setContentType(file.getContentType());
        userImage.setSize(file.getSize());
        
        return imagesRepository.save(userImage);
    }

    public List<UserImage> getUserImages(Integer userId) {
        return imagesRepository.findByUserId(userId);
    }

    public Optional<UserImage> getImageById(Long id) {
        return imagesRepository.findById(id);
    }

    @Transactional
    public boolean deleteImage(Long id, Integer userId) {
        Optional<UserImage> image = imagesRepository.findById(id);
        if (image.isPresent() && image.get().getUserId().equals(userId)) {
            imagesRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
