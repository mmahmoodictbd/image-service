package com.chumbok.imageservice.service;

import com.chumbok.imageservice.Application;
import com.chumbok.imageservice.TestConfig;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.EMPTY;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@ActiveProfiles("it")
@AutoConfigureMockMvc
@TestInstance(PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class, TestConfig.class})
class ImageFetchServiceTest {

	private static final String REFERENCE = "gallery/photos/van-gogh.png";
	private static final String REMOTE_SOURCE_EXPECTED_URL = "http://localhost/gallery/photos/van-gogh.png";

	@Autowired private ImageFetchService imageFetchService;
	@MockBean private RestTemplate mockRestTemplate;

	@Test
	void testFetchOriginalImage() {
		var remoteSourceImageBytes = new byte[0];
		when(mockRestTemplate.getForEntity(REMOTE_SOURCE_EXPECTED_URL, byte[].class))
			.thenReturn(new ResponseEntity(remoteSourceImageBytes, OK));

		assertEquals(of(remoteSourceImageBytes), imageFetchService.fetchOriginalImage(REFERENCE));
	}

	@Test
	void testFetchOriginalImageRetry3Times() {
		when(mockRestTemplate.getForEntity(REMOTE_SOURCE_EXPECTED_URL, byte[].class)).
			thenThrow(HttpClientErrorException.create(NOT_FOUND, "NOT_FOUND", EMPTY, null, null));

		imageFetchService.fetchOriginalImage(REFERENCE);

		verify(mockRestTemplate, times(3)).getForEntity(REMOTE_SOURCE_EXPECTED_URL, byte[].class);
	}

	@Test
	void testFetchOriginalImageReturnOptionalEmptyOn404() {
		when(mockRestTemplate.getForEntity(REMOTE_SOURCE_EXPECTED_URL, byte[].class)).
			thenThrow(HttpClientErrorException.create(NOT_FOUND, "NOT_FOUND", EMPTY, null, null));

		assertEquals(empty(), imageFetchService.fetchOriginalImage(REFERENCE));
	}
}