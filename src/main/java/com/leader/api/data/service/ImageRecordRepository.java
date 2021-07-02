package com.leader.api.data.service;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ImageRecordRepository extends MongoRepository<ImageRecord, ObjectId> {

    List<ImageRecord> findByUploadUserIdAndStatus(ObjectId uploadUserId, String status);

    ImageRecord findByUploadUserIdAndImageUrlAndStatus(ObjectId uploadUserId, String imageName, String status);

    long countByUploadUserIdAndStatus(ObjectId uploadUserId, String status);

    boolean existsByImageUrl(String imageUrl);

    void deleteByImageUrl(String imageName);
}
