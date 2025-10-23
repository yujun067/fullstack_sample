package com.moviesearch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;
import io.netty.channel.ChannelOption;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import java.time.Duration;

@Configuration
@Slf4j
public class WebClientConfig {

    @Value("${omdb.api.url}")
    private String baseUrl;

    @Bean
    public WebClient webClient() {
        // Configure HTTP client with timeouts and compression support
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 5 second connection timeout
                .responseTimeout(Duration.ofSeconds(10)) // 10 second response timeout
                .compress(true); // Enable automatic compression/decompression

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)) // 1MB
                .defaultHeader("User-Agent", "MovieSearchApp/1.0")
                .defaultHeader("Accept", "application/json")
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (clientRequest.logPrefix().contains("omdbapi.com")) {
                log.debug("Request: " + clientRequest.method() + " " + clientRequest.url());
            }
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.headers().asHttpHeaders().getFirst("Content-Type") != null) {
                log.debug("Response: " + clientResponse.statusCode());
            }
            return Mono.just(clientResponse);
        });
    }
}
