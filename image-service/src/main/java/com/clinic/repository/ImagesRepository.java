package com.clinic.repository;

import com.clinic.image_service.entity.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImagesRepository extends JpaRepository<UserImage, Long> {
    List<UserImage> findByUserId(Integer userId);
    void deleteByIdAndUserId(Long id, Integer userId);
}
