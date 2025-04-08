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
    private String productUploadDir;

    @Value("${app.upload.dir.professionals:uploads/professionals}")
    private String professionalUploadDir;
    /**
     * per configurare i gestori di risorse statiche
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
         // Percorso per immagini dei prodotti
         Path productUploadPath = Paths.get(productUploadDir);
         String absoluteProductUploadPath = productUploadPath.toFile().getAbsolutePath();
         
         // Percorso per immagini dei professionisti
         Path professionalUploadPath = Paths.get(professionalUploadDir);
         String absoluteProfessionalUploadPath = professionalUploadPath.toFile().getAbsolutePath();
         
         // Risorse per prodotti
         registry.addResourceHandler("/images/products/**")
                 .addResourceLocations("file:" + absoluteProductUploadPath + "/");
         
         // Risorse per professionisti
         registry.addResourceHandler("/images/professionals/**")
                 .addResourceLocations("file:" + absoluteProfessionalUploadPath + "/");   

        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");

        System.out.println("Configurate risorse statiche per immagini: " 
                + absoluteProductUploadPath + ", " 
                + absoluteProfessionalUploadPath);         
    }
}