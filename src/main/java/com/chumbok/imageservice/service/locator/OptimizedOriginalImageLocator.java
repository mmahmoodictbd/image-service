package com.chumbok.imageservice.service.locator;

import com.chumbok.imageservice.dto.ImageResponse;
import com.chumbok.imageservice.dto.ImageType;
import com.chumbok.imageservice.service.ImageProcessingService;
import com.chumbok.imageservice.service.S3Service;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import static com.chumbok.imageservice.util.FileUtil.guessContentType;
import static com.chumbok.imageservice.util.FileUtil.readAllBytes;
import static com.chumbok.imageservice.util.ReferenceUtil.buildS3ImagePath;
import static com.chumbok.imageservice.util.ReferenceUtil.convertToUniqueFilename;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.springframework.http.MediaType.parseMediaType;

public class OptimizedOriginalImageLocator extends CacheableImageLocator implements ImageLocator {

	private static final String ORIGINAL_IMAGE_TYPE = "original";

	private final S3Service s3Service;
	private final ImageProcessingService imageProcessingService;

	public OptimizedOriginalImageLocator(final S3Service s3Service, final ImageProcessingService imageProcessingService) {
		super(s3Service);
		this.s3Service = s3Service;
		this.imageProcessingService = imageProcessingService;
	}

	@Override
	public Optional<ImageResponse> findImage(final ImageType imageType, final String reference) {
		var s3OriginalImagePath = buildS3ImagePath(ORIGINAL_IMAGE_TYPE, reference);
		if (!s3Service.isImageExist(s3OriginalImagePath)) {
			return empty();
		}

		var originalImageBytes = readAllBytes(s3Service.getImage(s3OriginalImagePath).inputStream());
		var optimizedImageBytes = imageProcessingService.process(originalImageBytes, imageType);
		cacheImage(buildS3ImagePath(imageType.name(), reference), optimizedImageBytes);

		return of(new ImageResponse(
			convertToUniqueFilename(reference),
			parseMediaType(guessContentType(optimizedImageBytes)),
			new ByteArrayInputStream(optimizedImageBytes),
			optimizedImageBytes.length
		));
	}
}