package com.leader.api.service.service;

import com.leader.api.data.service.ImageRecord;
import com.leader.api.data.service.ImageRecordRepository;
import com.leader.api.resource.storage.StaticResourceStorage;
import com.leader.api.service.util.SecureService;
import com.leader.api.service.util.UserIdService;
import com.leader.api.util.InternalErrorException;
import com.leader.api.util.MultitaskUtil;
import com.leader.api.util.component.DateUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.leader.api.data.service.ImageRecord.*;

@Service
public class ImageService {

    public static final long UPLOAD_LINK_EXPIRE_MILLISECONDS = 30000;
    public static final long MAXIMUM_TEMP_UPLOAD_COUNT = 9;
    public static final String FILE_PREFIX = "v1_";
    public static final int RANDOM_SALT_LENGTH = 32;

    private final StaticResourceStorage resourceStorage;
    private final ImageRecordRepository imageRecordRepository;
    private final UserIdService userIdService;
    private final SecureService secureService;
    private final DateUtil dateUtil;

    @Autowired
    public ImageService(StaticResourceStorage resourceStorage, ImageRecordRepository imageRecordRepository,
                        UserIdService userIdService, SecureService secureService, DateUtil dateUtil) {
        this.resourceStorage = resourceStorage;
        this.imageRecordRepository = imageRecordRepository;
        this.userIdService = userIdService;
        this.secureService = secureService;
        this.dateUtil = dateUtil;
    }

    private Date getExpirationSinceNow() {
        return new Date(dateUtil.getCurrentTime() + UPLOAD_LINK_EXPIRE_MILLISECONDS);
    }

    private String allocateNewImageUrl(ObjectId userId, Date expiration) {
        // insert the new record
        ImageRecord record = new ImageRecord();
        record.uploadUserId = userId;
        record.status = PENDING;
        record.uploadUrlExpire = expiration;

        String imageUrl;
        synchronized (imageRecordRepository) {
            // generate a new imageUrl
            do {
                imageUrl = FILE_PREFIX + secureService.generateRandomSalt(RANDOM_SALT_LENGTH);
            } while (imageRecordRepository.existsByImageUrl(imageUrl));
            record.imageUrl = imageUrl;
            imageRecordRepository.insert(record);
        }
        return imageUrl;
    }

    private void setRecordToInvalid(ImageRecord record) {
        record.status = INVALID;
        imageRecordRepository.save(record);
    }

    private void setRecordsToInvalid(List<ImageRecord> records) {
        records.forEach(record -> record.status = INVALID);
        imageRecordRepository.saveAll(records);
    }

    private void cleanUpInvalidImages() {  // only way to completely remove images
        ObjectId userId = userIdService.getCurrentUserId();

        // find invalid and expired records, extracting url part
        List<ImageRecord> invalidAndExpiredRecords = imageRecordRepository
                .findByUploadUserIdAndStatusAndUploadUrlExpireBefore(userId, INVALID, dateUtil.getCurrentDate());
        List<String> imageUrls = invalidAndExpiredRecords.stream()
                .map(record -> record.imageUrl).collect(Collectors.toList());

        // delete all images according to urls
        MultitaskUtil.forEach(imageUrls, imageUrl -> {
            resourceStorage.deleteFile(imageUrl);
            imageRecordRepository.deleteByImageUrl(imageUrl);
        });
    }

    public String generateNewUploadUrl() {
        ObjectId userId = userIdService.getCurrentUserId();
        Date expiration = getExpirationSinceNow();
        String imageUrl = allocateNewImageUrl(userId, expiration);
        return resourceStorage.generatePresignedUploadUrl(imageUrl, expiration).toString();
    }

    public List<String> generateNewUploadUrls(int count) {
        if (count == 0) {
            return Collections.emptyList();
        }
        if (count == 1) {
            return Collections.singletonList(generateNewUploadUrl());
        }
        if (count > MAXIMUM_TEMP_UPLOAD_COUNT) {
            throw new InternalErrorException("Count too large.");
        }

        ObjectId userId = userIdService.getCurrentUserId();
        Date expiration = getExpirationSinceNow();
        String[] uploadUrls = new String[count];
        MultitaskUtil.forI(count, targetIndex -> {
            String imageUrl = allocateNewImageUrl(userId, expiration);
            String uploadUrl = resourceStorage.generatePresignedUploadUrl(imageUrl, expiration).toString();
            uploadUrls[targetIndex] = uploadUrl;
        });
        return Arrays.asList(uploadUrls);
    }

    public String duplicateImage(String imageUrl) {
        if (imageUrl == null) {
            return null;
        }

        ObjectId userId = userIdService.getCurrentUserId();
        String newUrl = allocateNewImageUrl(userId, dateUtil.getCurrentDate());
        resourceStorage.copyFile(imageUrl, newUrl);
        confirmUploadImage(newUrl);
        return newUrl;
    }

    public void assertUploadedTempImage(String imageUrl) {  // asserts at least one image uploaded
        if (imageUrl == null) {
            return;
        }

        ObjectId userId = userIdService.getCurrentUserId();
        boolean recordExists = imageRecordRepository.existsByUploadUserIdAndImageUrlAndStatus(userId, imageUrl, PENDING);
        if (!recordExists || !resourceStorage.fileExists(imageUrl)) {
            throw new InternalErrorException("Image not uploaded.");
        }
    }

    public void assertUploadedTempImages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.size() == 0) {
            return;
        }

        ObjectId userId = userIdService.getCurrentUserId();
        for (String imageUrl: imageUrls) {
            if (!imageRecordRepository.existsByUploadUserIdAndImageUrlAndStatus(userId, imageUrl, PENDING)) {
                throw new InternalErrorException("Images not uploaded.");
            }
        }
        if (!resourceStorage.allFilesExist(imageUrls)) {
            throw new InternalErrorException("Images not uploaded.");
        }
    }

    public void confirmUploadImage(String imageUrl) {
        if (imageUrl == null) {
            return;
        }

        ObjectId userId = userIdService.getCurrentUserId();
        ImageRecord record = imageRecordRepository.findByUploadUserIdAndImageUrlAndStatus(userId, imageUrl, PENDING);
        if (record == null || !resourceStorage.fileExists(imageUrl)) {
            throw new InternalErrorException("Image not uploaded.");
        }
        record.status = USING;
        imageRecordRepository.save(record);
    }

    public void confirmUploadImages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.size() == 0) {
            return;
        }

        ObjectId userId = userIdService.getCurrentUserId();
        ArrayList<ImageRecord> records = new ArrayList<>();
        for (String imageUrl: imageUrls) {
            ImageRecord record = imageRecordRepository.findByUploadUserIdAndImageUrlAndStatus(userId, imageUrl, PENDING);
            if (record == null) {
                throw new InternalErrorException("Images not uploaded.");
            }
            records.add(record);
        }
        if (!resourceStorage.allFilesExist(imageUrls)) {
            throw new InternalErrorException("Images not uploaded.");
        }
        records.forEach(record -> record.status = USING);
        imageRecordRepository.saveAll(records);
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null) {
            return;
        }

        setRecordToInvalid(imageRecordRepository.findByImageUrl(imageUrl));
        cleanUpInvalidImages();
    }

    public void deleteImages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.size() == 0) {
            return;
        }
        if (imageUrls.size() == 1) {
            deleteImage(imageUrls.get(0));
            return;
        }

        setRecordsToInvalid(imageRecordRepository.findByImageUrlIn(imageUrls));
        cleanUpInvalidImages();
    }

    public void cleanUp() {
        ObjectId userId = userIdService.getCurrentUserId();
        setRecordsToInvalid(imageRecordRepository.findByUploadUserIdAndStatus(userId, PENDING));
        cleanUpInvalidImages();
    }

    public String getAccessStartUrl() {
        return resourceStorage.getAccessStartUrl();
    }
}
