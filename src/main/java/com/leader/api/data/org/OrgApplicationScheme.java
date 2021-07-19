package com.leader.api.data.org;

import java.util.ArrayList;

public class OrgApplicationScheme {

    public boolean open;
    public int maximumApplication;  // -1 means no limit
    public boolean appointDepartment;
    public ArrayList<OrgApplicationQuestion> questions;
}
