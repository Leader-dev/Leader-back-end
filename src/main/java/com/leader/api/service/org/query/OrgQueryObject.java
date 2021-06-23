package com.leader.api.service.org.query;

import org.bson.types.ObjectId;

public class OrgQueryObject {

    public ObjectId orgId;
    public String queryName;
    public String typeAlias;
    public Long minMemberCount;
    public Long maxMemberCount;
    public int pageNumber;
    public int pageSize;
}
