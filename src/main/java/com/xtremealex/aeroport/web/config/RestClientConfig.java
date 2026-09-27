package com.xtremealex.aeroport.web.config;

import com.xtremealex.aeroport.web.client.AeroportApi;
import com.xtremealex.aeroport.web.client.ApiBadRequestException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executors;

/**
 * Client verso l'API interna basato su JDK HttpClient con connection pool e
 * virtual threads: riusa le connessioni (bene con tante chiamate) e scala su più pod.
 * L'API dichiarativa {@link AeroportApi} è esposta come proxy HTTP Interface.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ApiClientProperties.class)
public class RestClientConfig {

    @Bean
    RestClient aeroportRestClient(ApiClientProperties props) {
        // JDK HttpClient: pool di connessioni riutilizzabili, HTTP/1.1 keep-alive,
        // dispatch su virtual threads (una richiesta bloccante non consuma platform thread).
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(props.connectTimeoutMs()))
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        var factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(props.readTimeoutMs()));

        return RestClient.builder()
                .baseUrl(props.baseUrl())
                .requestFactory(factory)
                // 4xx = richiesta non valida (non "API giù"): distinta dagli errori di rete
                .defaultStatusHandler(status -> status.is4xxClientError(),
                        (request, response) -> {
                            throw new ApiBadRequestException(
                                    "Richiesta non valida verso l'API (" + response.getStatusCode() + ")");
                        })
                .build();
    }

    @Bean
    AeroportApi aeroportApi(RestClient aeroportRestClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(aeroportRestClient))
                .build();
        return factory.createClient(AeroportApi.class);
    }
}
