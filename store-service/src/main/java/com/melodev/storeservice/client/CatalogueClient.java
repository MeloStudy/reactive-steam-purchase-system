package com.melodev.storeservice.client;

import com.melodev.storeservice.exceptions.ApiServiceException;
import com.melodev.storeservice.exceptions.GameNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
public class CatalogueClient {
    private final WebClient webClient;
    private final int maxRetries;
    private final Duration initialBackOff;
    private final Duration timeout;
    private final Duration slaTimeout;

    public CatalogueClient(WebClient.Builder builder,
                           @Value("${services.catalogue.base-url}") String url,
                           @Value("${services.catalogue.timeout.response-ms}") int timeout,
                           @Value("${services.catalogue.timeout.sla-ms}") int slaTimeout,
                           @Value("${services.catalogue.retry.max-attempts}") int maxRetryAttempts,
                           @Value("${services.catalogue.retry.initial-backoff-ms}") int initialBackOff
    ) {
        this.webClient = builder.baseUrl(url).build();
        this.maxRetries = maxRetryAttempts;
        this.initialBackOff = Duration.ofMillis(initialBackOff);
        this.timeout = Duration.ofMillis(timeout);
        this.slaTimeout = Duration.ofMillis(slaTimeout);

    }

    public Mono<GameResponse> getGameWithSla(String gameId) {
        return getGame(gameId)
                .timeout(this.slaTimeout); // could improve this adding a custom exception as fallback
    }


    public Mono<GameResponse> getGame(String gameId) {
        String uri = "/games/{id}";
        log.info("Initiating API request to URI: {}", uri.replace("{id}", gameId));

        return webClient.get()
                .uri(uri, gameId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    // map error 404 to custom exception
                    if (response.statusCode() == HttpStatus.NOT_FOUND) {
                        log.warn("Game with id {} was not found", gameId);
                        return Mono.error(new GameNotFoundException(gameId));
                    }

                    // for other 4xx and 5xx use the default WebClientResponseException
                    return response.createException();
                })
                .bodyToMono(GameResponse.class)
                .timeout(this.timeout)
                .retryWhen(buildRetryStrategy())
                .doOnSuccess(response -> log.info("Game requested successfully."))
                .doOnError(e -> log.error("Error calling catalogue service: {}", e.getMessage()))
                .onErrorReturn(GameNotFoundException.class, GameResponse.dummy()); // only for a 404 scenario dummy is returned,
        // for other 4xx is WebClientResponseException and for 5xx scenarios, ApiServiceException
    }

    private Retry buildRetryStrategy() {
        //backoff = initial * 2 ^ #attempt
        // for initial = 100 -> 200, 400, 800 + Jitter variation
        return Retry.backoff(this.maxRetries, this.initialBackOff)
                .filter(this::isRetryableException)
                .doBeforeRetry(retrySignal -> {
                    long currentRetry = retrySignal.totalRetries() + 1;
                    double backoffTime = this.initialBackOff.toMillis() * Math.pow(2, retrySignal.totalRetries());
                    String cause = retrySignal.failure().getMessage();
                    log.warn("New retry #{} after {} ms -> Due: {}", currentRetry, backoffTime, cause);
                })
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                    log.error("All retries exhausted. Propagating final error.");
                    return new ApiServiceException("Catalogue Service unavailable after maximum retries", retrySignal.failure());
                });
    }

    private boolean isRetryableException(Throwable ex) {
        if (ex instanceof TimeoutException) {
            log.debug("[Retry] Timeout: Retry due {}", ex.getMessage());
            // Timeouts
            return true;
        }

        if (ex instanceof GameNotFoundException) {
            log.debug("[Retry] Business Error: No Retry even if {}", ex.getMessage());
            // Specific 404 mapped error
            return false;
        }

        if (ex instanceof WebClientResponseException httpEx) {
            // All errors 4xx and 5xx, is thrown as a WebClientResponseException
            log.debug("[Retry] HTTP Status: evaluate to Retry -> {}", httpEx.getStatusCode().value());
            return httpEx.getStatusCode().is5xxServerError(); // only 5xx are retryable
        }

        // Retryable other exceptions like connection refused, etc
        log.debug("[Retry] General exception: Retry due {}", ex.getMessage());
        return true;
    }

}