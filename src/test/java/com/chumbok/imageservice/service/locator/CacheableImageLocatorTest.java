package com.chumbok.imageservice.service.locator;

import com.chumbok.imageservice.Application;
import com.chumbok.imageservice.TestConfig;
import com.chumbok.imageservice.dto.ImageResponse;
import com.chumbok.imageservice.dto.ImageType;
import com.chumbok.imageservice.service.ImageFetchService;
import com.chumbok.imageservice.service.ImageProcessingService;
import com.chumbok.imageservice.service.S3Service;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;

import static com.chumbok.imageservice.dto.FileExtension.PNG;
import static com.chumbok.imageservice.dto.ScaleType.SKEW;
import static com.chumbok.imageservice.util.FileUtil.readAllBytes;
import static java.io.InputStream.nullInputStream;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.IMAGE_JPEG;
import static org.springframework.http.MediaType.IMAGE_PNG;

@ActiveProfiles("it")
@AutoConfigureMockMvc
@TestInstance(PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class, TestConfig.class})
class CacheableImageLocatorTest {

	private static final ImageType THUMBNAIL_IMAGE_TYPE
		= new ImageType("thumbnail", 100, 100, 50, SKEW, "#000000", PNG);
	private static final String REFERENCE = "gallery/photos/van-gogh.png";
	private static final String S3_IMAGE_PATH_ORIGINAL = "original/gall/ery_/gallery_photos_van-gogh.png";
	private static final String S3_IMAGE_PATH_THUMBNAIL = "thumbnail/gall/ery_/gallery_photos_van-gogh.png";
	private static final String IMAGE_FILE_NAME = "gallery_photos_van-gogh.png";
	private static final String TEST_IMAGE_PATH = "static/beautiful-test-image.png";
	private static final int LENGTH_TEST_IMAGE = 48061;

	@MockBean private S3Service mockS3Service;
	@MockBean private ImageProcessingService mockImageProcessingService;
	@MockBean private ImageFetchService mockImageFetchService;
	@Autowired private ImageLocator imageLocator;

	@Test
	void testFindImageExistInS3() {
		var imageResponse = new ImageResponse(IMAGE_FILE_NAME, IMAGE_JPEG, nullInputStream(), 0);
		when(mockS3Service.isImageExist(S3_IMAGE_PATH_THUMBNAIL)).thenReturn(true);
		when(mockS3Service.getImage(S3_IMAGE_PATH_THUMBNAIL)).thenReturn(imageResponse);

		assertEquals(imageResponse, imageLocator.findImage(THUMBNAIL_IMAGE_TYPE, REFERENCE).get());
	}

	@Test
	void testFindImageOriginalImageProcess() {
		var testImageBytes = getTestImageBytes();
		var expectedImageResponse = new ImageResponse(IMAGE_FILE_NAME, IMAGE_PNG, new ByteArrayInputStream(testImageBytes), LENGTH_TEST_IMAGE);
		when(mockS3Service.isImageExist(S3_IMAGE_PATH_ORIGINAL)).thenReturn(true);
		when(mockS3Service.getImage(S3_IMAGE_PATH_ORIGINAL)).thenReturn(expectedImageResponse);
		when(mockImageProcessingService.process(any(byte[].class), eq(THUMBNAIL_IMAGE_TYPE))).thenReturn(testImageBytes);

		var imageResponse = imageLocator.findImage(THUMBNAIL_IMAGE_TYPE, REFERENCE);

		assertEquals(expectedImageResponse.fileName(), imageResponse.get().fileName());
		assertEquals(expectedImageResponse.contentType(), imageResponse.get().contentType());
		assertArrayEquals(testImageBytes, readAllBytes(imageResponse.get().inputStream()));
		assertEquals(expectedImageResponse.contentLength(), imageResponse.get().contentLength());
	}

	@Test
	void testFindImageCacheProcessedOriginalImage() {
		var testImageBytes = getTestImageBytes();
		var imageResponse = new ImageResponse(IMAGE_FILE_NAME, IMAGE_PNG, nullInputStream(), 0);
		when(mockS3Service.isImageExist(S3_IMAGE_PATH_ORIGINAL)).thenReturn(true);
		when(mockS3Service.getImage(S3_IMAGE_PATH_ORIGINAL)).thenReturn(imageResponse);
		when(mockImageProcessingService.process(any(byte[].class), eq(THUMBNAIL_IMAGE_TYPE))).thenReturn(testImageBytes);


		imageLocator.findImage(THUMBNAIL_IMAGE_TYPE, REFERENCE);

		var imageBytesCaptor = ArgumentCaptor.forClass(byte[].class);
		verify(mockS3Service, timeout(5000)).saveImageAsync(eq(S3_IMAGE_PATH_THUMBNAIL), imageBytesCaptor.capture());
		assertArrayEquals(testImageBytes, imageBytesCaptor.getValue());
	}

	@Test
	void testFindImageNoS3CacheFetchRemoteSourceImage() {
		var testImageBytes = getTestImageBytes();
		var expectedImageResponse = new ImageResponse(IMAGE_FILE_NAME, IMAGE_PNG, new ByteArrayInputStream(testImageBytes), LENGTH_TEST_IMAGE);
		when(mockImageFetchService.fetchOriginalImage(REFERENCE)).thenReturn(of(testImageBytes));
		when(mockImageProcessingService.process(any(byte[].class), eq(THUMBNAIL_IMAGE_TYPE))).thenReturn(testImageBytes);

		var imageResponse = imageLocator.findImage(THUMBNAIL_IMAGE_TYPE, REFERENCE);

		assertEquals(expectedImageResponse.fileName(), imageResponse.get().fileName());
		assertEquals(expectedImageResponse.contentType(), imageResponse.get().contentType());
		assertArrayEquals(testImageBytes, readAllBytes(imageResponse.get().inputStream()));
		assertEquals(expectedImageResponse.contentLength(), imageResponse.get().contentLength());
	}

	@Test
	void testFindImageCacheFetchRemoteSourceImage() {
		var testImageBytes = getTestImageBytes();
		when(mockImageFetchService.fetchOriginalImage(REFERENCE)).thenReturn(of(testImageBytes));
		when(mockImageProcessingService.process(any(byte[].class), eq(THUMBNAIL_IMAGE_TYPE))).thenReturn(testImageBytes);

		imageLocator.findImage(THUMBNAIL_IMAGE_TYPE, REFERENCE);

		var imageBytesCaptor = ArgumentCaptor.forClass(byte[].class);
		verify(mockS3Service, timeout(5000)).saveImageAsync(eq(S3_IMAGE_PATH_THUMBNAIL), imageBytesCaptor.capture());
		assertArrayEquals(testImageBytes, imageBytesCaptor.getValue());
	}

	@SneakyThrows
	private byte[] getTestImageBytes() {
		return readAllBytes(new ClassPathResource(TEST_IMAGE_PATH).getInputStream());
	}
}