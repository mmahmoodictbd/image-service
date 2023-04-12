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

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
		return restTemplateBuilder
			.setConnectTimeout(Duration.ofSeconds(3))
			.setReadTimeout(Duration.ofSeconds(3))
			.build();
	}

	@Bean
	public RetryTemplate retryTemplate() {
		return RetryTemplate.builder().maxAttempts(3).fixedBackoff(1000).retryOn(RestClientException.class).build();
	}
}
