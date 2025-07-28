package com.example.easychat_server.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {

    // 定义文件上传的存储路径
    // 注意：这只是一个简单的本地存储示例。生产环境应使用更可靠的路径或云存储。
    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("请选择一个文件上传");
        }

        // 确保上传目录存在
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 生成唯一的文件名，防止重名覆盖
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        // 拼接文件的完整存储路径
        File dest = new File(UPLOAD_DIR + uniqueFileName);

        try {
            // 保存文件到目标位置
            file.transferTo(dest);

            // 返回文件的可访问路径
            // 暂时返回一个简单的相对路径，后续需要配置静态资源映射才能访问
            String fileUrl = "/uploads/" + uniqueFileName;

            // 我们返回一个包含 url 的对象
            return ResponseEntity.ok().body(java.util.Collections.singletonMap("url", fileUrl));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("文件上传失败: " + e.getMessage());
        }
    }
}