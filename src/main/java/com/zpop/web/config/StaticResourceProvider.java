package com.zpop.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.Setter;

@Configuration
@Setter
@ConfigurationProperties(prefix="static")
public class StaticResourceProvider implements WebMvcConfigurer {

    private String meetingUrl;
    private String meetingPath;
    private String bannerUrl;
    private String bannerPath;
    private String profileUrl;
    private String profilePath;

    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler(meetingUrl+"/**")
                .addResourceLocations("file:" + meetingPath);

        registry
                .addResourceHandler(bannerUrl+"/**")
                .addResourceLocations("file:" + bannerPath);

        registry
                .addResourceHandler(profileUrl+"/**")
                .addResourceLocations("file:" + profilePath);
    }
}