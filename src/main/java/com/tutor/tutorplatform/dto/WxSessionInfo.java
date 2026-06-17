package com.tutor.tutorplatform.dto;

import lombok.Data;

@Data
public class WxSessionInfo {
    private String openid;
    private String session_key;
    private String unionid;
    private Integer errcode;
    private String errmsg;
}