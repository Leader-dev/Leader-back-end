package com.leader.api.data.org.type;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrganizationTypeRepository extends MongoRepository<OrganizationType, String> {

    OrganizationType findByAlias(String alias);
}
