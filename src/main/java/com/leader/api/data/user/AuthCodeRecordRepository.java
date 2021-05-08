package com.leader.api.data.user;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;

public interface AuthCodeRecordRepository extends MongoRepository<AuthCodeRecord, ObjectId> {

    AuthCodeRecord findByPhone(String phone);

    void deleteByPhone(String phone);

    default String generateAuthCode(String phone) {
        final String authcode = String.valueOf(Math.random()).substring(2, 8);
        AuthCodeRecord authCodeRecord = new AuthCodeRecord();
        authCodeRecord.phone = phone;
        authCodeRecord.authcode = authcode;
        authCodeRecord.timestamp = new Date();
        deleteByPhone(phone);
        insert(authCodeRecord);
        return authcode;
    }

    default long timePassedSinceLastAuthCode(String phone) {
        AuthCodeRecord authCodeRecord = findByPhone(phone);
        if (authCodeRecord == null) {
            return -1;
        }
        return new Date().getTime() - authCodeRecord.timestamp.getTime();
    }

    default boolean isAuthCodeValid(String phone, String authcode, long expire) {
        AuthCodeRecord authCodeRecord = findByPhone(phone);
        if (authCodeRecord == null) {
            return false;
        }
        long timePassed = new Date().getTime() - authCodeRecord.timestamp.getTime();
        return timePassed <= expire && authCodeRecord.authcode.equals(authcode);
    }
}
