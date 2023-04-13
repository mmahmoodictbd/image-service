package com.chumbok.imageservice.controller;

import com.chumbok.imageservice.Application;
import com.chumbok.imageservice.TestConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import static com.chumbok.imageservice.util.FileUtil.readAllBytes;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("it")
@TestInstance(PER_CLASS)
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class, TestConfig.class})
class ImageControllerTest {

	@Autowired private MockMvc mockMvc;
	@Autowired private WebApplicationContext context;
	@MockBean private RestTemplate mockRestTemplate;
	@MockBean private S3Client mockS3Client;

	@BeforeAll
	void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	@Test
	void testShowImageEndpointFetchRemoteSourceImage() throws Exception {
		when(mockRestTemplate.getForEntity("http://localhost/webp/gallery/beautiful-test-image.png", byte[].class)).
			thenReturn(new ResponseEntity(fetchedImageBytes(), OK));

		mockMvc.perform(get("/image/show/detail-large/seo-friendly-url").queryParam("reference", "webp/gallery/beautiful-test-image.png"))
			.andExpect(status().isOk())
			.andExpect(content().contentType("image/jpeg"))
			.andExpect(header().string(CONTENT_DISPOSITION, "inline; filename=\"webp_gallery_beautiful-test-image.png\""));
	}

	@Test
	void testVerifyFlushImageEndpointCallS3Client() throws Exception {
		mockMvc.perform(delete("/image/flush/detail-large").queryParam("reference", "webp/gallery/beautiful-test-image.png"))
			.andExpect(status().isNoContent());

		verify(mockS3Client)
			.deleteObject(DeleteObjectRequest.builder().bucket("images").key("detail-large/webp/_gal/webp_gallery_beautiful-test-image.png").build());
	}

	@Test
	void testVerifyFlushOriginalImageEndpointCallS3Client() throws Exception {
		mockMvc.perform(delete("/image/flush/original").queryParam("reference", "webp/gallery/beautiful-test-image.png"))
			.andExpect(status().isNoContent());

		verify(mockS3Client)
			.deleteObject(DeleteObjectRequest.builder().bucket("images").key("original/webp/_gal/webp_gallery_beautiful-test-image.png").build());
		verify(mockS3Client)
			.deleteObject(DeleteObjectRequest.builder().bucket("images").key("thumbnail/webp/_gal/webp_gallery_beautiful-test-image.png").build());
		verify(mockS3Client)
			.deleteObject(DeleteObjectRequest.builder().bucket("images").key("detail-large/webp/_gal/webp_gallery_beautiful-test-image.png").build());
	}

	private byte[] fetchedImageBytes() {
		return readAllBytes(getClass().getResourceAsStream("/static/beautiful-test-image.png"));
	}
}