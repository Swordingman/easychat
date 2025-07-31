package com.example.easychat_server.controller;

import com.example.easychat_server.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("请选择一个文件上传");
        }

        String url = fileService.uploadFile(file);

        if (url != null) {
            return ResponseEntity.ok().body(Collections.singletonMap("url", url));
        } else {
            return ResponseEntity.internalServerError().body("文件上传失败");
        }
    }
}