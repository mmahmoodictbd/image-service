package com.chumbok.imageservice.service.locator;

public interface CacheableImage {

	void cacheImage(final String s3ImagePath, final byte[] imageBytes);
}
