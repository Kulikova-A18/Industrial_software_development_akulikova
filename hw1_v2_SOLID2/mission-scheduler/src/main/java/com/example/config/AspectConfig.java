package com.example.config;

import com.example.aspects.TimingAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class AspectConfig {

    @Bean
    public TimingAspect timingAspect() {
        return new TimingAspect();
    }
}