package com.tutor.tutorplatform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutor.tutorplatform.config.WxConfig;
import com.tutor.tutorplatform.dto.WxSessionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WxLoginService {

    @Autowired
    private WxConfig wxConfig;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 模拟登录：当 code 为 "test123" 时，直接返回模拟的 openid
    private static final String MOCK_CODE = "test123";
    private static final String MOCK_OPENID = "mock_openid_123456";

    public WxSessionInfo getSessionInfo(String code) {
        // 开发调试：使用模拟 code 时，直接返回模拟数据，不调用微信接口
        if (MOCK_CODE.equals(code)) {
            WxSessionInfo mock = new WxSessionInfo();
            mock.setOpenid(MOCK_OPENID);
            mock.setSession_key("mock_session_key");
            return mock;
        }

        // 真实调用微信接口
        String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                wxConfig.getLoginUrl(), wxConfig.getAppid(), wxConfig.getSecret(), code);

        try {
            // 先获取原始字符串响应（微信可能返回 JSON 或纯文本错误）
            String rawResponse = restTemplate.getForObject(url, String.class);
            System.out.println("微信接口返回原始内容: " + rawResponse);

            // 尝试将字符串解析为 WxSessionInfo
            WxSessionInfo sessionInfo = objectMapper.readValue(rawResponse, WxSessionInfo.class);

            // 检查微信返回的错误码
            if (sessionInfo.getErrcode() != null && sessionInfo.getErrcode() != 0) {
                throw new RuntimeException("微信登录失败: " + sessionInfo.getErrmsg());
            }
            return sessionInfo;
        } catch (Exception e) {
            throw new RuntimeException("调用微信接口失败: " + e.getMessage(), e);
        }
    }

    // 预设的模拟 code 及对应的用户信息（与数据库现有用户匹配）
    private static final Map<String, String> MOCK_OPENID_MAP = new HashMap<>();
    static {
        MOCK_OPENID_MAP.put("test123", "mock_student_openid");      // 学员测试
        MOCK_OPENID_MAP.put("teacher002", "mock_teacher_002");      // 教员1（张老师）
        MOCK_OPENID_MAP.put("teacher003", "mock_teacher_003");      // 教员2（李老师）
        MOCK_OPENID_MAP.put("admin001", "mock_admin_openid");      //管理员
    }

    public boolean isMockCode(String code) {
        return MOCK_OPENID_MAP.containsKey(code);
    }

    public String getMockOpenid(String code) {
        return MOCK_OPENID_MAP.get(code);
    }
}

