package com.chumbok.imageservice.service.locator;

import com.chumbok.imageservice.dto.ImageResponse;
import com.chumbok.imageservice.dto.ImageType;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class ChainedImageLocator implements ImageLocator {

	private final ImageLocator current;
	private final ImageLocator next;

	@Override
	public Optional<ImageResponse> findImage(final ImageType imageType, final String reference) {
		return current.findImage(imageType, reference)
			.map(Optional::of)
			.orElseGet(() -> next.findImage(imageType, reference));
	}
}
