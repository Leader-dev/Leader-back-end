package com.leader.api.resource.sms;

public interface SMSService {

    void sendAuthCode(String phone, String authcode);
}
