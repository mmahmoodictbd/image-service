package com.chumbok.imageservice.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import software.amazon.awssdk.core.exception.SdkException;

@Configuration
public class ImageSaveRetryConfig {

	@Bean
	public RetryTemplate imageSaveRetryTemplate() {
		return RetryTemplate.builder().maxAttempts(2).fixedBackoff(200).retryOn(SdkException.class).build();
	}
}
