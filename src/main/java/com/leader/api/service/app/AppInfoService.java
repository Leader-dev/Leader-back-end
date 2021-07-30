package com.leader.api.service.app;

import com.leader.api.data.app.AppInfo;
import com.leader.api.data.app.AppInfoRepository;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppInfoService {

    public static final String AGREEMENT = "agreement";
    public static final String PRIVACY = "privacy";

    private final AppInfoRepository appInfoRepository;

    @Autowired
    public AppInfoService(AppInfoRepository appInfoRepository) {
        this.appInfoRepository = appInfoRepository;
    }

    private AppInfo getAppInfo() {
        AppInfo info = appInfoRepository.findFirstBy();
        if (info == null) {
            info = new AppInfo();
            info.info = new Document();
            appInfoRepository.insert(info);
        }
        return info;
    }

    public <T> T getProperty(String key, T defaultValue) {
        return getAppInfo().info.get(key, defaultValue);
    }

    public void setProperty(String key, Object value) {
        AppInfo info = getAppInfo();
        info.info.put(key, value);
        appInfoRepository.save(info);
    }

    public String getAgreement() {
        return getProperty(AGREEMENT, "");
    }

    public void setAgreement(String agreement) {
        setProperty(AGREEMENT, agreement);
    }

    public String getPrivacy() {
        return getProperty(PRIVACY, "");
    }

    public void setPrivacy(String privacy) {
        setProperty(PRIVACY, privacy);
    }
}
