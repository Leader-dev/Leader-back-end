package com.leader.api.data.org.manage.department;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrganizationDepartmentRepository extends MongoRepository<OrganizationDepartment, ObjectId> {
}
