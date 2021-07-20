package com.leader.api.data.admin;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AdminRepository extends MongoRepository<Admin, ObjectId> {

    <T> T findById(ObjectId id, Class<T> type);

    boolean existsByUsername(String username);

    Admin findByUsername(String username);
}
