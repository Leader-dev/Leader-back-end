package com.leader.api.service.service;

import com.leader.api.data.service.ImageRecord;
import com.leader.api.data.service.ImageRecordRepository;
import com.leader.api.resource.storage.StaticResourceStorage;
import com.leader.api.service.util.SecureService;
import com.leader.api.service.util.UserIdService;
import com.leader.api.util.InternalErrorException;
import com.leader.api.util.component.DateUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
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

    private ImageRecord newRecord(ObjectId userId, String imageUrl, Date expiration) {
        ImageRecord record = new ImageRecord();
        record.uploadUserId = userId;
        record.imageUrl = imageUrl;
        record.status = PENDING;
        record.uploadUrlExpire = expiration;
        return record;
    }

    private void deleteFileAndEraseRecord(String imageUrl) {
        resourceStorage.deleteFile(imageUrl);
        imageRecordRepository.deleteByImageUrl(imageUrl);
    }

    public String generateNewUploadUrl() {
        ObjectId userId = userIdService.getCurrentUserId();

        // generate new url
        String imageUrl;
        do {
            imageUrl = FILE_PREFIX + secureService.generateRandomSalt(RANDOM_SALT_LENGTH);
        } while (imageRecordRepository.existsByImageUrl(imageUrl));

        Date expiration = getExpirationSinceNow();
        imageRecordRepository.insert(newRecord(userId, imageUrl, expiration));
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
        ArrayList<String> uploadUrls = new ArrayList<>(count);
        CountDownLatch latch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            String imageUrl;
            do {
                imageUrl = FILE_PREFIX + secureService.generateRandomSalt(RANDOM_SALT_LENGTH);
            } while (imageRecordRepository.existsByImageUrl(imageUrl));
            imageRecordRepository.insert(newRecord(userId, imageUrl, expiration));

            // copy variables for thread to catch
            final String finalImageUrl = imageUrl;
            final int finalI = i;
            new Thread(() -> {
                String uploadUrl = resourceStorage.generatePresignedUploadUrl(finalImageUrl, expiration).toString();
                uploadUrls.set(finalI, uploadUrl);
                latch.countDown();
            }).start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new InternalErrorException("Interrupted exception occur.", e);
        }
        return uploadUrls;
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

        deleteFileAndEraseRecord(imageUrl);
    }

    public void deleteImages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.size() == 0) {
            return;
        }

        CountDownLatch latch = new CountDownLatch(imageUrls.size());
        for (String imageUrl: imageUrls) {
            new Thread(() -> {
                deleteFileAndEraseRecord(imageUrl);
                latch.countDown();
            }).start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new InternalErrorException("InterruptedException occur.", e);
        }
    }

    public void cleanUp() {
        ObjectId userId = userIdService.getCurrentUserId();

        // set all pending record to invalid
        List<ImageRecord> pendingRecords = imageRecordRepository.findByUploadUserIdAndStatus(userId, PENDING);
        pendingRecords.forEach(imageRecord -> imageRecord.status = INVALID);
        imageRecordRepository.saveAll(pendingRecords);

        // remove all invalid and expired record
        List<ImageRecord> invalidAndExpiredRecords = imageRecordRepository
                .findByUploadUserIdAndStatusAndUploadUrlExpireBefore(userId, INVALID, dateUtil.getCurrentDate());
        deleteImages(invalidAndExpiredRecords.stream().map(record -> record.imageUrl).collect(Collectors.toList()));
    }

    public String getAccessStartUrl() {
        return resourceStorage.getAccessStartUrl();
    }
}
