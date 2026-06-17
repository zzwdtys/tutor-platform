package com.tutor.tutorplatform.controller;

import com.tutor.tutorplatform.utils.JwtUtil;
import com.tutor.tutorplatform.exception.UnauthenticatedException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

public class BaseController {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 从请求头中解析用户ID
     * @param request HttpServletRequest
     * @return 用户ID
     */
    protected Long getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        System.out.println("[DEBUG] Authorization header: " + authHeader);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("[DEBUG] Missing or malformed Authorization header");
            throw new UnauthenticatedException("未登录或token无效");
        }
        String token = authHeader.substring(7);
        try {
            // 打印 token 的 claims 以便调试
            io.jsonwebtoken.Claims claims = jwtUtil.getClaimsFromToken(token);
            System.out.println("[DEBUG] Token subject=" + claims.getSubject() + ", role=" + claims.get("role"));
        } catch (Exception e) {
            System.out.println("[DEBUG] Failed to parse token: " + e.getMessage());
            throw new UnauthenticatedException("未登录或token无效");
        }
        return jwtUtil.getUserIdFromToken(token);
    }

    /**
     * 从请求头中解析用户角色
     * @param request HttpServletRequest
     * @return 角色 0学员 1教员 2管理员
     */
    protected Integer getRoleFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        System.out.println("[DEBUG] Authorization header: " + authHeader);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("[DEBUG] Missing or malformed Authorization header");
            throw new UnauthenticatedException("未登录或token无效");
        }
        String token = authHeader.substring(7);
        try {
            io.jsonwebtoken.Claims claims = jwtUtil.getClaimsFromToken(token);
            System.out.println("[DEBUG] Token subject=" + claims.getSubject() + ", role=" + claims.get("role"));
        } catch (Exception e) {
            System.out.println("[DEBUG] Failed to parse token: " + e.getMessage());
            throw new UnauthenticatedException("未登录或token无效");
        }
        return jwtUtil.getRoleFromToken(token);
    }
}