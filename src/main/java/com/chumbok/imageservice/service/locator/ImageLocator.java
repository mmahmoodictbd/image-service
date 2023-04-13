package com.chumbok.imageservice.service.locator;

import com.chumbok.imageservice.dto.ImageResponse;
import com.chumbok.imageservice.dto.ImageType;

import java.util.Optional;

@FunctionalInterface
public interface ImageLocator {

	Optional<ImageResponse> findImage(final ImageType imageType, final String reference);

	default ImageLocator orElse(final ImageLocator next) {
		return new ChainedImageLocator(this, next);
	}
}
