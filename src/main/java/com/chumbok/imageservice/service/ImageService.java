package com.chumbok.imageservice.service;

import com.chumbok.imageservice.config.ImageTypesConfigProperties;
import com.chumbok.imageservice.dto.ImageResponse;
import com.chumbok.imageservice.dto.ImageType;
import com.chumbok.imageservice.exception.NotFoundException;
import com.chumbok.imageservice.service.locator.ImageLocator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.chumbok.imageservice.util.ReferenceUtil.buildS3ImagePath;

@Service
@RequiredArgsConstructor
public class ImageService {

	private static final String ORIGINAL_IMAGE_TYPE = "original";
	private static final String IMAGE_TYPE_DOES_NOT_EXIST_MESSAGE = "The requested predefined image type does not exist.";
	private static final String IMAGE_DOES_NOT_FOUND_MESSAGE = "The requested source image does not exist.";

	private final S3Service s3Service;
	private final ImageLocator cacheableImageLocator;

	private final ImageTypesConfigProperties imageTypesProperties;

	public ImageResponse getImage(final String imageTypeString, final String reference) {
		var imageType = findPredefinedImageType(imageTypeString)
			.orElseThrow(() -> new NotFoundException(IMAGE_TYPE_DOES_NOT_EXIST_MESSAGE));
		return cacheableImageLocator.findImage(imageType, reference)
			.orElseThrow(() -> new NotFoundException(IMAGE_DOES_NOT_FOUND_MESSAGE));
	}

	public void flush(final String imageTypeString, final String reference) {
		var imageType = findPredefinedImageType(imageTypeString)
			.orElseThrow(() -> new NotFoundException(IMAGE_TYPE_DOES_NOT_EXIST_MESSAGE));
		if (ORIGINAL_IMAGE_TYPE.equals(imageType.name())) {
			imageTypesProperties.imageTypes().stream()
				.map(type -> buildS3ImagePath(type.name(), reference))
				.forEach(s3Service::deleteImage);
		} else {
			s3Service.deleteImage(buildS3ImagePath(imageType.name(), reference));
		}
	}

	private Optional<ImageType> findPredefinedImageType(final String imageTypeString) {
		return imageTypesProperties.imageTypes()
			.stream()
			.filter(predefinedImageType -> predefinedImageType.name().equalsIgnoreCase(imageTypeString))
			.findFirst();
	}
}
