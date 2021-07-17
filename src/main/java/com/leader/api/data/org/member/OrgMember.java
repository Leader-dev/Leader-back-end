package com.leader.api.data.org.member;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document(collection = "org_member")
public class OrgMember {

    @Id
    public ObjectId id;
    public String numberId;

    // combined unique fields
    public ObjectId orgId;
    public ObjectId userId;

    public ArrayList<OrgMemberRole> roles;

    public String name;
    public String title;
    public ArrayList<String> phone;
    public ArrayList<String> email;

    public boolean displayTitle;

    public boolean resigned;
}
