package com.chumbok.imageservice.service;

import com.chumbok.imageservice.Application;
import com.chumbok.imageservice.TestConfig;
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
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;

import static com.chumbok.imageservice.util.FileUtil.readAllBytes;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.IMAGE_PNG;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;
import static software.amazon.awssdk.services.s3.model.ObjectCannedACL.PUBLIC_READ;

@ActiveProfiles("it")
@AutoConfigureMockMvc
@TestInstance(PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class, TestConfig.class})
class S3ServiceTest {

	private static final String S3_IMAGE_PATH = "original/gall/_ery/gall_ery_van-gogh.png";
	private static final String TEST_IMAGE_PATH = "static/beautiful-test-image.png";
	private static final int LENGTH_TEST_IMAGE = 48061;
	private static final String IMAGES_BUCKET = "images";

	@Autowired private S3Service s3Service;
	@MockBean private S3Client mockS3Client;

	@Test
	void testGetImage() {
		when(mockS3Client.getObject(GetObjectRequest.builder().bucket(IMAGES_BUCKET).key(S3_IMAGE_PATH).build()))
			.thenReturn(buildResponseInputStream());

		var imageResponse = s3Service.getImage(S3_IMAGE_PATH);

		assertEquals("gall_ery_van-gogh.png", imageResponse.fileName());
		assertEquals(IMAGE_PNG, imageResponse.contentType());
		assertArrayEquals(readAllBytes(getTestImageInputStream()), readAllBytes(imageResponse.inputStream()));
		assertEquals(LENGTH_TEST_IMAGE, imageResponse.contentLength());
	}

	@Test
	void testSaveImageAsync() {
		when(mockS3Client.headBucket(any(HeadBucketRequest.class))).thenReturn(HeadBucketResponse.builder().build());

		s3Service.saveImageAsync(S3_IMAGE_PATH, readAllBytes(getTestImageInputStream()));

		var putObjectRequestArgumentCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
		verify(mockS3Client, timeout(5000)).putObject(putObjectRequestArgumentCaptor.capture(), any(RequestBody.class));
		assertEquals(
			PutObjectRequest.builder().bucket(IMAGES_BUCKET).key(S3_IMAGE_PATH).contentType(IMAGE_PNG_VALUE).acl(PUBLIC_READ).build(),
			putObjectRequestArgumentCaptor.getValue()
		);
	}

	@Test
	void testDeleteImage() {
		s3Service.deleteImage(S3_IMAGE_PATH);

		verify(mockS3Client).deleteObject(DeleteObjectRequest.builder().bucket(IMAGES_BUCKET).key(S3_IMAGE_PATH).build());
	}

	@SneakyThrows
	private ResponseInputStream<GetObjectResponse> buildResponseInputStream() {
		InputStream inputStream = getTestImageInputStream();
		return new ResponseInputStream<>(
			GetObjectResponse.builder().contentType(IMAGE_PNG_VALUE).contentLength((long) inputStream.available()).build(),
			AbortableInputStream.create(inputStream, () -> {})
		);
	}

	@SneakyThrows
	private InputStream getTestImageInputStream() {
		return new ClassPathResource(TEST_IMAGE_PATH).getInputStream();
	}
}