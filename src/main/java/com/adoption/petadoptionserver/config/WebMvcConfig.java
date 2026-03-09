package com.adoption.petadoptionserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Path;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // מיפוי בקשות ל־/uploads/** לתיקיית uploads שב-filesystem של השרת
        Path uploadDir = Path.of("uploads");
        String uploadPath = uploadDir.toAbsolutePath().toUri().toString(); // file:/...
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}