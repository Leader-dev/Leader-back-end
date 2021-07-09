package com.leader.api.data.org;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface OrganizationRepository extends MongoRepository<Organization, ObjectId> {

    Organization findByNumberId(String numberId);

    boolean existsByNumberId(String numberId);

    Organization findByIdAndStatus(ObjectId id, String status);

    <T> T findFirstById(ObjectId id, Class<T> type);

    @Query("?0")
    <T> Page<T> findByQuery(
            Document query,
            Pageable pageable,
            Class<T> type
    );
}
