package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AppProperties appProps;

    public WebConfig(AppProperties appProps) {
        this.appProps = appProps;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get(appProps.getUploads().getDir()).toAbsolutePath().normalize();
        String uri = uploadDir.toUri().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations(uri);
    }
}
