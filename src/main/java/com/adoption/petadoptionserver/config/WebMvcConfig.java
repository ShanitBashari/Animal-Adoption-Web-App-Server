package com.adoption.petadoptionserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/**
 * Web MVC configuration for serving static resources.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Maps requests to /uploads/** to the physical uploads' directory
     * in the server file system.
     *
     * @param registry the resource handler registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Path.of("uploads");
        String uploadPath = uploadDir.toAbsolutePath().toUri().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}