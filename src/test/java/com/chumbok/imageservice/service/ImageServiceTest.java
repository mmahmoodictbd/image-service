package com.chumbok.imageservice.service;

import com.chumbok.imageservice.config.ImageTypesConfigProperties;
import com.chumbok.imageservice.dto.ImageResponse;
import com.chumbok.imageservice.dto.ImageType;
import com.chumbok.imageservice.exception.NotFoundException;
import com.chumbok.imageservice.service.locator.ImageLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import java.util.List;

import static com.chumbok.imageservice.dto.FileExtension.JPG;
import static com.chumbok.imageservice.dto.FileExtension.PNG;
import static com.chumbok.imageservice.dto.ScaleType.FILL;
import static com.chumbok.imageservice.dto.ScaleType.NONE;
import static com.chumbok.imageservice.dto.ScaleType.SKEW;
import static java.io.InputStream.nullInputStream;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

	private static final ImageType ORIGINAL_IMAGE_TYPE
		= new ImageType("original", -1, -1, 100, NONE, "#000000", PNG);
	private static final ImageType THUMBNAIL_IMAGE_TYPE
		= new ImageType("thumbnail", 100, 100, 50, SKEW, "#000000", PNG);
	private static final ImageType DETAIL_LARGE_IMAGE_TYPE
		= new ImageType("detail-large", 1000, 1000, 100, FILL, "#FFFFFF", JPG);
	private static final ImageTypesConfigProperties IMAGE_TYPES_CONFIG_PROPERTIES
		= new ImageTypesConfigProperties(List.of(ORIGINAL_IMAGE_TYPE, THUMBNAIL_IMAGE_TYPE, DETAIL_LARGE_IMAGE_TYPE));
	private static final String REFERENCE = "gallery/photos/van-gogh.png";

	@Mock private S3Service mockS3Service;
	@Mock private ImageLocator mockImageLocator;

	private ImageService imageService;

	@BeforeEach
	void setup() {
		imageService = new ImageService(mockS3Service, mockImageLocator, IMAGE_TYPES_CONFIG_PROPERTIES);
	}

	@Test
	void testGetImage() {
		var imageResponse = new ImageResponse("hello.jpg", MediaType.IMAGE_JPEG, nullInputStream(), 0);
		when(mockImageLocator.findImage(THUMBNAIL_IMAGE_TYPE, REFERENCE)).thenReturn(of(imageResponse));

		assertEquals(imageResponse, imageService.getImage("thumbnail", REFERENCE));
	}

	@Test
	void testGetImageUnknownImageTypeThrowsNotFoundException() {
		var exceptionThatWasThrown = assertThrows(NotFoundException.class, () ->
			imageService.getImage("unknownImageType", REFERENCE)
		);
		assertEquals("The requested predefined image type does not exist.", exceptionThatWasThrown.getMessage());
	}

	@Test
	void testGetImageImageNotFoundThrowsNotFoundException() {
		var exceptionThatWasThrown = assertThrows(NotFoundException.class, () ->
			imageService.getImage("thumbnail", REFERENCE)
		);
		assertEquals("The requested source image does not exist.", exceptionThatWasThrown.getMessage());
	}

	@Test
	void testFlushImageTypeOriginalCallS3ServiceDeleteImageForAllImageTypes() {
		imageService.flush("original", REFERENCE);

		verify(mockS3Service).deleteImage("original/gall/ery_/gallery_photos_van-gogh.png");
		verify(mockS3Service).deleteImage("thumbnail/gall/ery_/gallery_photos_van-gogh.png");
		verify(mockS3Service).deleteImage("detail-large/gall/ery_/gallery_photos_van-gogh.png");
	}

	@Test
	void testFlushNonOriginalImageTypeCallS3ServiceDeleteImageForSpecificImageTypes() {
		imageService.flush("thumbnail", REFERENCE);

		verify(mockS3Service).deleteImage("thumbnail/gall/ery_/gallery_photos_van-gogh.png");
	}

	@Test
	void testFlushUnknownImageTypeThrowsNotFoundException() {
		var exceptionThatWasThrown = assertThrows(NotFoundException.class, () ->
			imageService.flush("unknownImageType", REFERENCE)
		);
		assertEquals("The requested predefined image type does not exist.", exceptionThatWasThrown.getMessage());
	}
}