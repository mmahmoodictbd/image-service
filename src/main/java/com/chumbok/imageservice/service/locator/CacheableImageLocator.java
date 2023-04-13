package com.chumbok.imageservice.service.locator;

import com.chumbok.imageservice.service.S3Service;

public abstract class CacheableImageLocator extends S3CacheableImage implements ImageLocator, CacheableImage {

	protected CacheableImageLocator(final S3Service s3Service) {
		super(s3Service);
	}
}
