package com.leader.api.data.org;

import org.bson.types.ObjectId;

public class OrganizationQueryObject {

    public ObjectId organizationId;
    public String queryName;
    public String typeAlias;
    public Long minMemberCount;
    public Long maxMemberCount;
    public int pageNumber;
    public int pageSize;
}
