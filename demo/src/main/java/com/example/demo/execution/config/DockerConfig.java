package com.example.demo.execution.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Docker Engine API 클라이언트 구성.
 */
@Configuration
public class DockerConfig {
  @Bean
  public DockerClient dockerClient() {
    // ✅ .withDockerHost를 통해 소켓 위치를 명시합니다.
    DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .withDockerHost("unix:///var/run/docker.sock")
            .build();

    // Docker는 HTTP로 통신
    ApacheDockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
            .dockerHost(config.getDockerHost())
            .sslConfig(config.getSSLConfig())
            .build();

    return DockerClientImpl.getInstance(config, httpClient);
  }
}
