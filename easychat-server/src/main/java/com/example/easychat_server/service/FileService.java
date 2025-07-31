package com.example.easychat_server.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    // 从 application.properties 中注入 OSS 的配置
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;
    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;
    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;
    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    /**
     * 上传文件到阿里云 OSS
     *
     * @param file 用户通过 HTTP 请求上传的文件
     * @return 成功上传后，文件在 OSS 上的完整可访问 URL；如果上传失败，则返回 null。
     */
    public String uploadFile(MultipartFile file) {
        // 1. 创建 OSSClient 实例。
        // 这是与 OSS 服务进行交互的主要对象。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        String fileUrl = null;
        try {
            // 2. 获取文件的输入流
            // try-with-resources 语句可以确保 inputStream 在使用完毕后被自动关闭
            try (InputStream inputStream = file.getInputStream()) {

                // 3. 构建在 OSS 中存储的唯一文件名 (Object Name)
                // a. 获取原始文件名和扩展名
                String originalFilename = file.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.lastIndexOf('.') != -1) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
                }

                // b. 按日期创建文件夹结构，方便管理，例如：easychat/2025/07/30/
                String datePath = new SimpleDateFormat("yyyy/MM/dd/").format(new Date());

                // c. 使用 UUID 生成一个随机的文件名，防止文件名冲突被覆盖
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

                // d. 拼接成最终在 Bucket 中的完整路径
                String objectName = "easychat/" + datePath + uniqueFileName;

                // 4. 执行文件上传
                log.info("开始上传文件到 OSS, Bucket: {}, ObjectName: {}", bucketName, objectName);
                ossClient.putObject(bucketName, objectName, inputStream);
                log.info("文件上传成功！");

                // 5. 拼接文件的公网访问 URL
                // 格式: https://<BucketName>.<Endpoint>/<ObjectName>
                fileUrl = "https://" + bucketName + "." + endpoint + "/" + objectName;
            }
        } catch (IOException e) {
            log.error("文件上传失败，获取输入流时出错: {}", e.getMessage());
            e.printStackTrace();
            return null; // 返回 null 表示失败
        } finally {
            // 6. 关闭 OSSClient。
            // 无论上传成功还是失败，都必须关闭客户端以释放资源。
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        return fileUrl;
    }
}