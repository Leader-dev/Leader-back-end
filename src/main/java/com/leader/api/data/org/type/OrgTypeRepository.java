package com.leader.api.data.org.type;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrgTypeRepository extends MongoRepository<OrgType, ObjectId> {

    OrgType findByAlias(String alias);

    <T> List<T> findAllByAliasNotNull(Class<T> type);
}
