package com.leader.api.data.org.department;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrgDepartmentRepository extends MongoRepository<OrgDepartment, ObjectId> {

    List<OrgDepartment> findByOrgId(ObjectId organizationId);

    List<OrgDepartment> findByParentId(ObjectId parentId);

    List<OrgDepartment> findByOrgIdAndParentId(ObjectId organizationId, ObjectId parentId);

    boolean existsByOrgIdAndId(ObjectId organizationId, ObjectId id);
}
