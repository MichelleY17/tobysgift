package com.tobysgift.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    
    
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Aggiungi controller per le pagine semplici
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/access-denied").setViewName("access-denied");
    }
    
    @Value("${app.upload.dir:uploads/products}")
    private String uploadDir;
    /**
     * per configurare i gestori di risorse statiche
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir);
        String absoluteUploadPath = uploadPath.toFile().getAbsolutePath();
        // per configurare i percorsi per le risorse statiche
        registry.addResourceHandler("/images/products/**")
                .addResourceLocations("file:" +absoluteUploadPath+"/");
                System.out.println("Configurate risorse statiche per immagini prodotti: " + absoluteUploadPath);        
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
    }
}