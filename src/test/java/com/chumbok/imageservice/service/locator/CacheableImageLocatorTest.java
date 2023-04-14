package com.chumbok.imageservice.service.locator;

import com.chumbok.imageservice.Application;
import com.chumbok.imageservice.TestConfig;
import com.chumbok.imageservice.dto.ImageResponse;
import com.chumbok.imageservice.dto.ImageType;
import com.chumbok.imageservice.service.ImageFetchService;
import com.chumbok.imageservice.service.S3Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.chumbok.imageservice.dto.FileExtension.PNG;
import static com.chumbok.imageservice.dto.ScaleType.SKEW;
import static java.io.InputStream.nullInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.Mockito.when;

@ActiveProfiles("it")
@AutoConfigureMockMvc
@TestInstance(PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class, TestConfig.class})
class CacheableImageLocatorTest {

	private static final ImageType THUMBNAIL_IMAGE_TYPE
		= new ImageType("thumbnail", 100, 100, 50, SKEW, "#000000", PNG);
	private static final String REFERENCE = "gallery/photos/van-gogh.png";
	private static final String S3_IMAGE_PATH = "thumbnail/gall/ery_/gallery_photos_van-gogh.png";

	@MockBean private S3Service mockS3Service;
	@MockBean private ImageFetchService mockImageFetchService;
	@Autowired private ImageLocator imageLocator;

	@Test
	void testFindImageExistInS3() {
		var imageResponse = new ImageResponse("gall_ery_van-gogh.png", MediaType.IMAGE_JPEG, nullInputStream(), 0);
		when(mockS3Service.isImageExist(S3_IMAGE_PATH)).thenReturn(true);
		when(mockS3Service.getImage(S3_IMAGE_PATH)).thenReturn(imageResponse);

		assertEquals(imageResponse, imageLocator.findImage(THUMBNAIL_IMAGE_TYPE, REFERENCE).get());
	}
}