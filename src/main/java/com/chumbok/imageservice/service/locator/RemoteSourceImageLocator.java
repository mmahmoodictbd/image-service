package com.chumbok.imageservice.service.locator;

import com.chumbok.imageservice.dto.ImageResponse;
import com.chumbok.imageservice.dto.ImageType;
import com.chumbok.imageservice.service.ImageFetchService;
import com.chumbok.imageservice.service.ImageProcessingService;
import com.chumbok.imageservice.service.S3Service;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import static com.chumbok.imageservice.util.FileUtil.guessContentType;
import static com.chumbok.imageservice.util.ReferenceUtil.buildS3ImagePath;
import static com.chumbok.imageservice.util.ReferenceUtil.convertToUniqueFilename;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.springframework.http.MediaType.parseMediaType;

public class RemoteSourceImageLocator extends CacheableImageLocator implements ImageLocator {

	private static final String ORIGINAL_IMAGE_TYPE = "original";

	private final ImageFetchService imageFetchService;
	private final ImageProcessingService imageProcessingService;

	public RemoteSourceImageLocator(final S3Service s3Service, final ImageFetchService imageFetchService, final ImageProcessingService imageProcessingService) {
		super(s3Service);
		this.imageFetchService = imageFetchService;
		this.imageProcessingService = imageProcessingService;
	}

	@Override
	public Optional<ImageResponse> findImage(final ImageType imageType, final String reference) {
		var imageBytes = imageFetchService.fetchOriginalImage(reference);
		if (!imageBytes.isPresent()) {
			return empty();
		}
		cacheImage(buildS3ImagePath(ORIGINAL_IMAGE_TYPE, reference), imageBytes.get());

		byte[] optimizedImageBytes;
		if (ORIGINAL_IMAGE_TYPE.equals(imageType.name())){
			optimizedImageBytes = imageProcessingService.process(imageBytes.get());
		} else {
			optimizedImageBytes = imageProcessingService.process(imageBytes.get(), imageType);
		}
		cacheImage(buildS3ImagePath(imageType.name(), reference), imageBytes.get());

		return of(new ImageResponse(
			convertToUniqueFilename(reference),
			parseMediaType(guessContentType(optimizedImageBytes)),
			new ByteArrayInputStream(optimizedImageBytes),
			optimizedImageBytes.length
		));
	}
}