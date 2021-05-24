package com.leader.api.data.user;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, ObjectId> {

    User findByPhone(String phone);

    boolean existsByPhone(String phone);
}
