package uk.ac.swansea.autograder.api.services;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.ac.swansea.autograder.api.services.dto.piston.PistonExecuteRequest;
import uk.ac.swansea.autograder.api.services.dto.piston.PistonExecuteResponse;
import uk.ac.swansea.autograder.api.services.dto.piston.PistonRuntime;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class PistonClient {
    @Value("${piston.url}")
    private String pistonApiUrl;

    private RestClient restClient;

    @PostConstruct
    public void initRestClient() {
        // Use Apache HttpClient5 for more predictable HTTP behavior
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        this.restClient = RestClient.builder()
                .baseUrl(pistonApiUrl)
                .requestFactory(requestFactory)
                .build();

        log.info("RestClient built with Apache HttpClient5, URL: {}", pistonApiUrl);
    }

    public PistonExecuteResponse execute(PistonExecuteRequest request) {
        try {
            return restClient.post()
                    .uri("/api/v2/execute")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(PistonExecuteResponse.class);
        } catch (Exception e) {
            log.error("Failed to execute code via Piston: {}", e.getMessage());
            throw new RuntimeException("Code execution failed: " + e.getMessage(), e);
        }
    }

    public List<PistonRuntime> getRuntimes() {
        try {
            PistonRuntime[] runtimes = restClient.get()
                    .uri("/api/v2/runtimes")
                    .retrieve()
                    .body(PistonRuntime[].class);

            if (runtimes == null) {
                log.warn("Piston returned null runtimes list");
                return List.of();
            }

            log.info("Retrieved {} available runtimes from Piston", runtimes.length);
            return Arrays.asList(runtimes);
        } catch (Exception e) {
            if (e instanceof org.springframework.web.client.HttpClientErrorException) {
                log.error("Piston API Error: {} - {}", ((org.springframework.web.client.HttpClientErrorException) e).getStatusCode(), ((org.springframework.web.client.HttpClientErrorException) e).getResponseBodyAsString());
            }
            log.error("Failed to fetch runtimes from Piston: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch available runtimes: " + e.getMessage(), e);
        }
    }
}