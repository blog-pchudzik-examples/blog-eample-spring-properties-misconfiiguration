package com.pchudzik.blog.example.propertiesconfiguration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class PropertiesConfigurationApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext app = SpringApplication.run(PropertiesConfigurationApplication.class, args);
        System.out.println("\n\nproperty value is " + app.getBean(PropertyHolder.class).propertyValue);
    }

    @Component
    static class PropertyHolder {
        @Value("${key}")
        public String propertyValue;
    }

}
