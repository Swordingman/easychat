package com.example.easychat_server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置 CORS（跨域资源共享）
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // 1. 对所有 /api/ 开头的路径应用此规则
                .allowedOrigins("http://localhost:5173") // 2. 允许来自 Vite 开发服务器源的请求
                //    请根据你 `npm run dev` 启动时的端口号进行修改
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 3. 允许的 HTTP 方法
                .allowedHeaders("*")    // 4. 允许所有请求头
                .allowCredentials(true) // 5. 是否允许浏览器发送 Cookie 等凭证
                .maxAge(3600);          // 6. 预检请求（OPTIONS）的缓存时间，单位秒
    }

    /**
     * 配置静态资源映射
     * 这使得我们可以通过 URL (例如 /uploads/my-image.jpg) 访问到服务器上的物理文件
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目根目录下的 uploads 文件夹路径
        String uploadDir = System.getProperty("user.dir") + "/uploads/";

        // 将 /uploads/** 的 URL 路径映射到 "file:/path/to/your/project/uploads/"
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir);
    }
}
