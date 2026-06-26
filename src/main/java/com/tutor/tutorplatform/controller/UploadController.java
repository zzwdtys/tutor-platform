package com.tutor.tutorplatform.controller;

import com.tutor.tutorplatform.common.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.matches(".*\\.(jpg|jpeg|png|gif)$")) {
            return Result.error("只支持图片格式");
        }
        try {
            // 转为 base64 data URL 存数据库，不依赖服务器文件系统
            byte[] bytes = file.getBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);
            String mime = originalFilename.endsWith(".png") ? "image/png" : "image/jpeg";
            String dataUrl = "data:" + mime + ";base64," + base64;
            return Result.success(dataUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("上传失败");
        }
    }
}