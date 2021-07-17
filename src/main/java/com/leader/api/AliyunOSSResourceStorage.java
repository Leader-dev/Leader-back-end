package com.leader.api;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.leader.api.resource.storage.StaticResourceStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class AliyunOSSResourceStorage implements StaticResourceStorage {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${aliyun.oss.access-start-url}")
    private String accessStartUrl;

    private OSS getOSSClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    private void operateOSSClient(Consumer<OSS> consumer) {
        OSS client = getOSSClient();
        consumer.accept(client);
        client.shutdown();
    }

    private <T> T operateOSSClientWithReturn(Function<OSS, T> function) {
        OSS client = getOSSClient();
        T result = function.apply(client);
        client.shutdown();
        return result;
    }

    public void storeFile(String url, InputStream inputStream) {
        operateOSSClient(oss -> oss.putObject(bucketName, url, inputStream));
    }

    public void copyFile(String sourceUrl, String targetUrl) {
        operateOSSClient(oss -> oss.copyObject(bucketName, sourceUrl, bucketName, targetUrl));
    }

    public URL generatePresignedUploadUrl(String url, Date expiration) {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, url, HttpMethod.PUT);
        request.setExpiration(expiration);
        return operateOSSClientWithReturn(oss -> oss.generatePresignedUrl(request));
    }

    public boolean fileExists(String url) {
        return operateOSSClientWithReturn(oss -> oss.doesObjectExist(bucketName, url));
    }

    public void deleteFile(String url) {
        operateOSSClient(oss -> oss.deleteObject(bucketName, url));
    }

    public String getAccessStartUrl() {
        return accessStartUrl;
    }
}
