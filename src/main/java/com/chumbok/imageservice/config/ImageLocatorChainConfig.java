package com.chumbok.imageservice.config;

import com.chumbok.imageservice.service.ImageFetchService;
import com.chumbok.imageservice.service.ImageProcessingService;
import com.chumbok.imageservice.service.S3Service;
import com.chumbok.imageservice.service.locator.ImageLocator;
import com.chumbok.imageservice.service.locator.OptimizedOriginalImageLocator;
import com.chumbok.imageservice.service.locator.RemoteSourceImageLocator;
import com.chumbok.imageservice.service.locator.S3ImageLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImageLocatorChainConfig {

	@Bean
	public ImageLocator cacheableImageLocator(final S3Service s3Service,
											  final ImageProcessingService imageProcessingService,
											  final ImageFetchService imageFetchService) {
		return new S3ImageLocator(s3Service)
			.orElse(new OptimizedOriginalImageLocator(s3Service, imageProcessingService))
			.orElse(new RemoteSourceImageLocator(s3Service, imageFetchService, imageProcessingService));
	}
}
