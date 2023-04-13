package com.chumbok.imageservice.service.locator;

import com.chumbok.imageservice.dto.ImageResponse;
import com.chumbok.imageservice.dto.ImageType;
import com.chumbok.imageservice.service.S3Service;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.chumbok.imageservice.util.ReferenceUtil.buildS3ImagePath;
import static java.util.Optional.empty;
import static java.util.Optional.of;

@RequiredArgsConstructor
public class S3ImageLocator implements ImageLocator {

	private final S3Service s3Service;

	@Override
	public Optional<ImageResponse> findImage(final ImageType imageType, final String reference) {
		var s3ImagePath = buildS3ImagePath(imageType.name(), reference);
		if (s3Service.isImageExist(s3ImagePath)) {
			return of(s3Service.getImage(s3ImagePath));
		}
		return empty();
	}
}
