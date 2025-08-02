package com.example.easychat_server.config; // 确保你的包名正确

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter; // 1. 确保 import 是这个
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置一个全局的 CORS 过滤器 Bean。
     * 这种方式优先级较高，能确保在 Spring Security 之前处理跨域问题。
     */
    @Bean
    public CorsFilter corsFilter() {
        // 1. 创建一个基于 URL 的 CORS 配置源
        // 确保 import 的是 org.springframework.web.cors.UrlBasedCorsConfigurationSource
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // 2. 创建一个详细的 CORS 配置对象
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // 允许发送 Cookie 等凭证
        config.setAllowedOriginPatterns(List.of("http://localhost:*"));
        config.addAllowedHeader("*"); // 允许所有请求头
        config.addAllowedMethod("*"); // 允许所有 HTTP 方法 (GET, POST, PUT, etc.)
        config.setMaxAge(3600L); // 预检请求的缓存时间

        // 3. 将这个配置应用到所有 /api/ 开头的路径上
        source.registerCorsConfiguration("/api/**", config);

        // 4. 返回一个包含我们配置的 CorsFilter 实例
        return new CorsFilter(source);
    }

    /**
     * 配置静态资源映射 (这部分是正确的)
     * 用于访问 /uploads/ 目录下的文件
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 由于我们切换到了OSS，这个方法其实可以被移除或注释掉了
        // 为了保持干净，我们先注释掉
        /*
        String uploadDir = System.getProperty("user.dir") + "/uploads/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir);
        */
    }

    /**
     * 我们已经使用了 CorsFilter Bean，就不再需要这个 addCorsMappings 方法了。
     * 将其注释或删除，避免配置冲突。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // WebMvcConfigurer.super.addCorsMappings(registry);
    }
}