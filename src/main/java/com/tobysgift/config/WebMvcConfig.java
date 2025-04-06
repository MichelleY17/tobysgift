package com.tobysgift.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    /**
     * per configurare i controller per le viste semplici
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Aggiungi controller per le pagine semplici
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/access-denied").setViewName("access-denied");
    }
    
    /**
     * per congfigurare i gestori di risorse statiche
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // per configurare i percorsi per le risorse statiche
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
    }
}