package com.chumbok.imageservice.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class ImageFetcherConfig {

	private static final int CONNECTION_TIMEOUT_MILLIS = 3000;
	private static final int READ_TIMEOUT_MILLIS = 3000;
	private static final int MAX_ATTEMPTS = 3;
	private static final int FIXED_BACKOFF_INTERVAL_MILLIS = 1000;

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
		return restTemplateBuilder
			.setConnectTimeout(Duration.ofMillis(CONNECTION_TIMEOUT_MILLIS))
			.setReadTimeout(Duration.ofMillis(READ_TIMEOUT_MILLIS))
			.build();
	}

	@Bean
	public RetryTemplate retryTemplate() {
		return RetryTemplate.builder()
			.maxAttempts(MAX_ATTEMPTS)
			.fixedBackoff(FIXED_BACKOFF_INTERVAL_MILLIS)
			.retryOn(RestClientException.class)
			.build();
	}
}
