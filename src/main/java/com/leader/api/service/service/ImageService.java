package com.leader.api.service.service;

import com.leader.api.data.service.ImageRecord;
import com.leader.api.data.service.ImageRecordRepository;
import com.leader.api.resource.storage.StaticResourceStorage;
import com.leader.api.service.util.SecureService;
import com.leader.api.service.util.UserIdService;
import com.leader.api.util.InternalErrorException;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static com.leader.api.data.service.ImageRecord.TEMP;
import static com.leader.api.data.service.ImageRecord.USING;

@Service
public class ImageService {

    public static final long MAXIMUM_TEMP_UPLOAD_COUNT = 9;
    public static final String FILE_PREFIX = "v1_";
    public static final int RANDOM_SALT_LENGTH = 32;
    public static final List<String> IMAGE_EXTENSIONS = Arrays.asList(".jpg", ".png", ".tiff", ".heif");

    private final StaticResourceStorage resourceStorage;
    private final ImageRecordRepository imageRecordRepository;
    private final UserIdService userIdService;
    private final SecureService secureService;

    @Autowired
    public ImageService(StaticResourceStorage resourceStorage, ImageRecordRepository imageRecordRepository,
                        UserIdService userIdService, SecureService secureService) {
        this.resourceStorage = resourceStorage;
        this.imageRecordRepository = imageRecordRepository;
        this.userIdService = userIdService;
        this.secureService = secureService;
    }

    private static boolean extensionInvalid(String filename) {
        for (String extension: IMAGE_EXTENSIONS) {
            if (filename.endsWith(extension)) {
                return false;
            }
        }
        return true;
    }

    private void uploadFile(ObjectId userId, String imageUrl, InputStream inputStream) {
        resourceStorage.storeFile(imageUrl, inputStream);
        ImageRecord record = new ImageRecord();
        record.uploadUserId = userId;
        record.imageUrl = imageUrl;
        record.status = TEMP;
        imageRecordRepository.save(record);
    }

    private void deleteFile(String imageUrl) {
        resourceStorage.deleteFile(imageUrl);
        imageRecordRepository.deleteByImageUrl(imageUrl);
    }

    public String uploadTempImage(MultipartFile file) throws IOException {
        ObjectId userId = userIdService.getCurrentUserId();
        long count = imageRecordRepository.countByUploadUserIdAndStatus(userId, TEMP);
        if (count >= MAXIMUM_TEMP_UPLOAD_COUNT) {
            throw new InternalErrorException("Too many temp images.");
        }

        if (extensionInvalid(file.getOriginalFilename())) {
            throw new InternalErrorException("Invalid image.");
        }

        String imageUrl;
        do {
            imageUrl = FILE_PREFIX + secureService.generateRandomSalt(RANDOM_SALT_LENGTH);
        } while (resourceStorage.fileExists(imageUrl));
        uploadFile(userId, imageUrl, file.getInputStream());

        return imageUrl;
    }

    public List<String> uploadTempImages(List<MultipartFile> files) throws IOException {
        ObjectId userId = userIdService.getCurrentUserId();
        long count = imageRecordRepository.countByUploadUserIdAndStatus(userId, TEMP);
        int newFileCount = files.size();
        if (count + newFileCount > MAXIMUM_TEMP_UPLOAD_COUNT) {
            throw new InternalErrorException("Too many temp images.");
        }

        for (MultipartFile file : files) {
            if (extensionInvalid(file.getOriginalFilename())) {
                throw new InternalErrorException("Invalid file.");
            }
        }

        ArrayList<String> imageUrls = new ArrayList<>(newFileCount);
        ArrayList<InputStream> inputStreams = new ArrayList<>(newFileCount);
        for (MultipartFile file : files) {
            String imageUrl;
            do {
                imageUrl = FILE_PREFIX + secureService.generateRandomSalt(RANDOM_SALT_LENGTH);
            } while (imageUrls.contains(imageUrl) || resourceStorage.fileExists(imageUrl));
            imageUrls.add(imageUrl);
            inputStreams.add(file.getInputStream());
        }

        // upload file using multi-process
        CountDownLatch latch = new CountDownLatch(newFileCount);
        for (int i = 0; i < newFileCount; i++) {
            String imageUrl = imageUrls.get(i);
            InputStream inputStream = inputStreams.get(i);
            new Thread(() -> {
                uploadFile(userId, imageUrl, inputStream);
                latch.countDown();
            }).start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return imageUrls;
    }

    public void assertUploadedTempImage(String imageUrl) {
        if (imageUrl == null)
            return;

        ObjectId userId = userIdService.getCurrentUserId();
        if (!imageRecordRepository.existsByUploadUserIdAndImageUrlAndStatus(userId, imageUrl, TEMP)) {
            throw new InternalErrorException("Image not uploaded.");
        }
    }

    public void assertUploadedTempImages(List<String> imageUrls) {
        if (imageUrls == null)
            return;

        ObjectId userId = userIdService.getCurrentUserId();
        for (String imageUrl: imageUrls) {
            if (imageRecordRepository.existsByUploadUserIdAndImageUrlAndStatus(userId, imageUrl, TEMP)) {
                throw new InternalErrorException("Images not uploaded.");
            }
        }
    }

    public void confirmUploadImage(String imageUrl) {
        if (imageUrl == null)
            return;

        ObjectId userId = userIdService.getCurrentUserId();
        ImageRecord record = imageRecordRepository.findByUploadUserIdAndImageUrlAndStatus(userId, imageUrl, TEMP);
        if (record == null) {
            throw new InternalErrorException("Image not uploaded.");
        }
        record.status = USING;
        imageRecordRepository.save(record);
    }

    public void confirmUploadImages(List<String> imageUrls) {
        if (imageUrls == null)
            return;

        ObjectId userId = userIdService.getCurrentUserId();
        ArrayList<ImageRecord> records = new ArrayList<>();
        for (String imageUrl: imageUrls) {
            ImageRecord record = imageRecordRepository.findByUploadUserIdAndImageUrlAndStatus(userId, imageUrl, TEMP);
            if (record == null) {
                throw new InternalErrorException("Images not uploaded.");
            }
            records.add(record);
        }
        records.forEach(record -> record.status = USING);
        imageRecordRepository.saveAll(records);
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null)
            return;

        deleteFile(imageUrl);
    }

    public void deleteImages(List<String> imageUrls) {
        if (imageUrls == null)
            return;

        CountDownLatch latch = new CountDownLatch(imageUrls.size());
        for (String imageUrl: imageUrls) {
            new Thread(() -> {
                deleteFile(imageUrl);
                latch.countDown();
            }).start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void deleteTemp() {
        ObjectId userId = userIdService.getCurrentUserId();
        List<ImageRecord> records = imageRecordRepository.findByUploadUserIdAndStatus(userId, TEMP);
        deleteImages(records.stream().map(record -> record.imageUrl).collect(Collectors.toList()));
    }

    public String getAccessStartUrl() {
        return resourceStorage.getAccessStartUrl();
    }
}
