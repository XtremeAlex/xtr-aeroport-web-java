package com.xtremealex.aeroport.web.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/** RestClient verso l'API interna (ClusterIP). Timeout brevi per fallire in fretta. */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ApiClientProperties.class)
public class RestClientConfig {

    @Bean
    RestClient aeroportRestClient(ApiClientProperties props) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.connectTimeoutMs());
        factory.setReadTimeout(props.readTimeoutMs());
        return RestClient.builder()
                .baseUrl(props.baseUrl())
                .requestFactory(factory)
                .build();
    }
}
