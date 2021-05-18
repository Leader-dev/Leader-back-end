package com.leader.api.data.org.type;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrganizationTypeRepository extends MongoRepository<OrganizationType, String> {

    OrganizationType findByAlias(String alias);

    <T> List<T> findAllByAliasNotNull(Class<T> type);
}
