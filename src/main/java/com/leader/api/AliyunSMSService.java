package com.leader.api;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.teaopenapi.models.Config;
import com.leader.api.resource.sms.SMSService;
import com.leader.api.util.ThrowableConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AliyunSMSService implements SMSService {

    @Value("${aliyun.sms.endpoint}")
    private String endpoint;

    @Value("${aliyun.sms.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.sms.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.sms.sign-name}")
    private String signName;

    @Value("${aliyun.sms.template-code}")
    private String templateCode;

    @Value("${aliyun.sms.authcode-param-name}")
    private String authcodeParamName;

    public Client createClient() throws Exception {
        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret);
        config.endpoint = endpoint;
        return new Client(config);
    }

    public void useClient(ThrowableConsumer<Client> consumer) {
        try {
            Client client = createClient();
            consumer.accept(client);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendAuthCode(String phone, String authcode) {
        useClient(client -> {
            SendSmsRequest sendSmsRequest = new SendSmsRequest()
                    .setPhoneNumbers(phone)
                    .setSignName(signName)
                    .setTemplateCode(templateCode)
                    .setTemplateParam("{\"" + authcodeParamName + "\":\"" + authcode + "\"}");
            String code = client.sendSms(sendSmsRequest).getBody().code;
            if (!"OK".equals(code)) {
                throw new RuntimeException("Send authcode failed. Receiving code: " + code);
            }
        });
    }
}
