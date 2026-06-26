package com.tutor.tutorplatform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutor.tutorplatform.config.WxConfig;
import com.tutor.tutorplatform.dto.WxSessionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WxLoginService {

    @Autowired
    private WxConfig wxConfig;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WxSessionInfo getSessionInfo(String code) {
        String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                wxConfig.getLoginUrl(), wxConfig.getAppid(), wxConfig.getSecret(), code);

        try {
            String rawResponse = restTemplate.getForObject(url, String.class);
            WxSessionInfo sessionInfo = objectMapper.readValue(rawResponse, WxSessionInfo.class);
            if (sessionInfo.getErrcode() != null && sessionInfo.getErrcode() != 0) {
                throw new RuntimeException("微信登录失败: " + sessionInfo.getErrmsg());
            }
            return sessionInfo;
        } catch (Exception e) {
            throw new RuntimeException("调用微信接口失败: " + e.getMessage(), e);
        }
    }
}

