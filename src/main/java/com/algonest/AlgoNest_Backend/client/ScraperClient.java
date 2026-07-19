package com.algonest.AlgoNest_Backend.client;

import com.algonest.AlgoNest_Backend.dto.ScrapeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ScraperClient {

    private final WebClient webClient;

    public ScraperClient(@Value("${scraper.service.url:http://localhost:8001}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public Mono<ScrapeResponse> scrapePlatform(String platform, String handle) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/scrape")
                        .queryParam("platform", platform)
                        .queryParam("handle", handle)
                        .build())
                .retrieve()
                .bodyToMono(ScrapeResponse.class)
                .onErrorResume(e -> {
                    ScrapeResponse errorResponse = new ScrapeResponse();
                    errorResponse.setPlatform(platform);
                    errorResponse.setHandle(handle);
                    errorResponse.setError("Scraper service error: " + e.getMessage());
                    return Mono.just(errorResponse);
                });
    }
}