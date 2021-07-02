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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private ImageRecord newRecord(ObjectId userId, String imageUrl) {
        ImageRecord record = new ImageRecord();
        record.uploadUserId = userId;
        record.imageUrl = imageUrl;
        record.status = TEMP;
        return record;
    }

    private void deleteFileAndEraseRecord(String imageUrl) {
        resourceStorage.deleteFile(imageUrl);
        imageRecordRepository.deleteByImageUrl(imageUrl);
    }

    // atomic operation, success or fail
    public void uploadTempImage(MultipartFile file) throws IOException {
        ObjectId userId = userIdService.getCurrentUserId();
        long count = imageRecordRepository.countByUploadUserIdAndStatus(userId, TEMP);

        // check if file count is in range
        if (count + 1 > MAXIMUM_TEMP_UPLOAD_COUNT) {
            throw new InternalErrorException("Too many temp images.");
        }

        // check if file is valid
        if (extensionInvalid(file.getOriginalFilename())) {
            throw new InternalErrorException("Invalid image.");
        }

        // generate new url
        String imageUrl;
        do {
            imageUrl = FILE_PREFIX + secureService.generateRandomSalt(RANDOM_SALT_LENGTH);
        } while (imageRecordRepository.existsByImageUrl(imageUrl));

        // upload
        resourceStorage.storeFile(imageUrl, file.getInputStream());

        // update database record
        try {
            imageRecordRepository.insert(newRecord(userId, imageUrl));
        } catch (Exception e) {  // rollback if exception occur
            e.printStackTrace();
            deleteImage(imageUrl);
            throw new InternalErrorException("Upload failed.", e);
        }
    }

    // atomic operation, all success or all fail
    public void uploadTempImages(List<MultipartFile> files) throws IOException {
        if (files.size() == 0) {
            return;
        }
        if (files.size() == 1) {
            uploadTempImage(files.get(0));
            return;
        }

        ObjectId userId = userIdService.getCurrentUserId();
        long count = imageRecordRepository.countByUploadUserIdAndStatus(userId, TEMP);
        int newFileCount = files.size();

        // check if file count is in range
        if (count + newFileCount > MAXIMUM_TEMP_UPLOAD_COUNT) {
            throw new InternalErrorException("Too many temp images.");
        }

        // check if all files are valid
        for (MultipartFile file : files) {
            if (extensionInvalid(file.getOriginalFilename())) {
                throw new InternalErrorException("Invalid file.");
            }
        }

        // prepare all urls and streams
        ArrayList<String> imageUrls = new ArrayList<>(newFileCount);
        ArrayList<InputStream> inputStreams = new ArrayList<>(newFileCount);
        for (MultipartFile file : files) {
            String imageUrl;
            do {
                imageUrl = FILE_PREFIX + secureService.generateRandomSalt(RANDOM_SALT_LENGTH);
            } while (imageUrls.contains(imageUrl) || imageRecordRepository.existsByImageUrl(imageUrl));
            imageUrls.add(imageUrl);
            inputStreams.add(file.getInputStream());
        }

        // upload files using multi-process
        AtomicReference<Exception> exceptionOccurred = new AtomicReference<>(null);  // signal if any exception occur
        CountDownLatch latch = new CountDownLatch(newFileCount);  // thread counter
        for (int i = 0; i < newFileCount; i++) {
            // copy to variable for lambda to catch
            String imageUrl = imageUrls.get(i);
            InputStream inputStream = inputStreams.get(i);
            // create new thread and start
            new Thread(() -> {
                try {
                    resourceStorage.storeFile(imageUrl, inputStream);
                } catch (Exception e) {
                    e.printStackTrace();
                    exceptionOccurred.set(e);
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        try {
            latch.await();  // wait for all thread to end
        } catch (InterruptedException e) {
            e.printStackTrace();
            exceptionOccurred.set(e);
        }
        if (exceptionOccurred.get() != null) {  // if any exception occur, rollback all changes
            deleteImages(imageUrls);
            throw new InternalErrorException("Upload failed.", exceptionOccurred.get());
        }

        // update database record
        try {
            ArrayList<ImageRecord> records = new ArrayList<>();
            for (String imageUrl : imageUrls) {
                records.add(newRecord(userId, imageUrl));
            }
            imageRecordRepository.insert(records);
        } catch (Exception e) {  // rollback if exception occur
            e.printStackTrace();
            deleteImages(imageUrls);
            throw new InternalErrorException("Upload failed.", e);
        }
    }

    public String getUploadedTempImage() {  // asserts at least one image uploaded
        ObjectId userId = userIdService.getCurrentUserId();
        List<ImageRecord> imageRecords = imageRecordRepository.findByUploadUserIdAndStatus(userId, TEMP);
        if (imageRecords.size() < 1) {
            throw new InternalErrorException("Image not uploaded.");
        }
        return imageRecords.get(0).imageUrl;
    }

    public ArrayList<String> getUploadedTempImages() {
        ObjectId userId = userIdService.getCurrentUserId();
        List<ImageRecord> imageRecords = imageRecordRepository.findByUploadUserIdAndStatus(userId, TEMP);
        Stream<String> imageUrlStream = imageRecords.stream().map(imageRecord -> imageRecord.imageUrl);
        return imageUrlStream.collect(Collectors.toCollection(ArrayList::new));
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

        deleteFileAndEraseRecord(imageUrl);
    }

    public void deleteImages(List<String> imageUrls) {
        if (imageUrls == null)
            return;

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
            throw new InternalErrorException("Delete failed.", e);
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
