package com.chumbok.imageservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import software.amazon.awssdk.core.exception.SdkException;

@Configuration
public class ImageSaveRetryConfig {

	private static final int MAX_ATTEMPTS = 2;
	private static final int FIXED_BACKOFF_INTERVAL_MILLIS = 200;

	@Bean
	public RetryTemplate imageSaveRetryTemplate() {
		return RetryTemplate.builder()
			.maxAttempts(MAX_ATTEMPTS)
			.fixedBackoff(FIXED_BACKOFF_INTERVAL_MILLIS)
			.retryOn(SdkException.class)
			.build();
	}
}
