package com.chumbok.imageservice.service.locator;

import com.chumbok.imageservice.service.S3Service;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class S3CacheableImage implements CacheableImage {

	private final S3Service s3Service;

	public void cacheImage(final String s3ImagePath, final byte[] imageBytes) {
		s3Service.saveImageAsync(s3ImagePath, imageBytes);
	}
}
