package com.leader.api.data.user;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, ObjectId> {

    <T> T findById(ObjectId id, Class<T> type);

    <T> Page<T> findAllBy(Pageable pageable, Class<T> type);

    <T> T findByUid(String uid, Class<T> type);

    User findByPhone(String phone);

    boolean existsByUid(String uid);

    boolean existsByPhone(String phone);
}
