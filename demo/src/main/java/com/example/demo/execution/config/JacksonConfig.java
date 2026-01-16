package com.example.demo.execution.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("executionJacksonConfig")
public class JacksonConfig {

  @Bean(name = "executionObjectMapper")
  public ObjectMapper executionObjectMapper() {
    return new ObjectMapper();
  }
}
