package com.leader.api.data.token;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface TokenRecordRepository extends MongoRepository<TokenRecord, UUID> {

    TokenRecord findFirstById(UUID id);

    boolean existsById(UUID id);
}
