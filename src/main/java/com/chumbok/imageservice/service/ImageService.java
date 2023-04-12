package com.chumbok.imageservice.service;

import com.chumbok.imageservice.config.ImageTypesConfigProperties;
import com.chumbok.imageservice.dto.ImageResponse;
import com.chumbok.imageservice.dto.ImageType;
import com.chumbok.imageservice.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

import static com.chumbok.imageservice.util.FileUtil.DIR_SEPARATOR;
import static com.chumbok.imageservice.util.FileUtil.getFileName;
import static com.chumbok.imageservice.util.FileUtil.guessContentTypeFromStream;
import static com.chumbok.imageservice.util.FileUtil.readAllBytes;
import static com.chumbok.imageservice.util.ReferenceUtil.convertToS3FilePath;
import static org.springframework.http.MediaType.parseMediaType;

@Service
@RequiredArgsConstructor
public class ImageService {

	private static final String ORIGINAL_IMAGE_TYPE = "original";

	private final S3Service s3Service;
	private final ImageFetchService imageFetchService;
	private final ImageProcessingService imageProcessingService;
	private final ImageTypesConfigProperties imageTypesProperties;

	public ImageResponse getImage(String imageTypeString, String reference) {
		var imageType = getImageType(imageTypeString);
		var s3ImagePath = buildS3ImagePath(imageType, reference);
		if (s3Service.isImageExist(s3ImagePath)) {
			return s3Service.getImage(s3ImagePath);
		}

		byte[] originalImageBytes;
		var s3OriginalImagePath = buildS3ImagePath(ORIGINAL_IMAGE_TYPE, reference);
		if (s3Service.isImageExist(s3OriginalImagePath)) {
			originalImageBytes = readAllBytes(s3Service.getImage(s3OriginalImagePath).inputStream());
		} else {
			originalImageBytes = imageFetchService.fetchOriginalImage(reference)
				.orElseThrow(() -> new NotFoundException("The requested source image does not exist."));
			s3Service.saveImageAsync(s3OriginalImagePath, originalImageBytes);
		}

		var optimizedImageBytes = imageProcessingService.process(originalImageBytes, imageType);
		s3Service.saveImageAsync(s3ImagePath, optimizedImageBytes);

		return new ImageResponse(
			getFileName(s3ImagePath),
			parseMediaType(guessContentTypeFromStream(optimizedImageBytes)),
			new ByteArrayInputStream(optimizedImageBytes),
			optimizedImageBytes.length
		);
	}

	public void flush(String imageTypeString, String reference) {
		var imageType = getImageType(imageTypeString);
		if (ORIGINAL_IMAGE_TYPE.equals(imageType.name())) {
			imageTypesProperties.imageTypes()
				.stream()
				.map(type -> buildS3ImagePath(type, reference))
				.forEach(s3ImagePath -> s3Service.deleteImage(s3ImagePath));
		} else {
			s3Service.deleteImage(buildS3ImagePath(imageType, reference));
		}
	}

	private ImageType getImageType(String imageTypeString) {
		return imageTypesProperties.imageTypes()
			.stream()
			.filter(predefinedImageType -> predefinedImageType.name().equals(imageTypeString.toLowerCase()))
			.findFirst()
			.orElseThrow(() -> new NotFoundException("The requested predefined image type does not exist."));
	}

	private String buildS3ImagePath(ImageType imageType, String reference) {
		return imageType.name() + DIR_SEPARATOR + convertToS3FilePath(reference);
	}

	private String buildS3ImagePath(String imageType, String reference) {
		return imageType + DIR_SEPARATOR + convertToS3FilePath(reference);
	}
}
