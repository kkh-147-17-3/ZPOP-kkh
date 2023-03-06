package com.zpop.web.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        
        Path meetingImageLocation = Paths.get(meetingPath).toAbsolutePath().normalize();
        Path profileImageLocation = Paths.get(profilePath).toAbsolutePath().normalize();
        Path bannerImageLocation = Paths.get(bannerPath).toAbsolutePath().normalize();

        try{
                if(!Files.exists(meetingImageLocation)){
                        Files.createDirectories(meetingImageLocation);
                }
        } catch (Exception e){
                throw new RuntimeException("모임글 이미지 경로 생성에 실패했습니다.");
        }

        try{
                if(!Files.exists(profileImageLocation)){
                        Files.createDirectories(profileImageLocation);
                }
        }catch (Exception e){
                throw new RuntimeException("프로필 이미지 경로 생성에 실패했습니다.");
        }

        try{
                if(!Files.exists(bannerImageLocation)){
                        Files.createDirectories(bannerImageLocation);
                }
        }catch (Exception e){
                throw new RuntimeException("베너 이미지 경로 생성에 실패했습니다.");
        }

        
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