package com.chumbok.imageservice.service;

import com.chumbok.imageservice.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException.NotFound;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageFetchService {

	private final RetryTemplate retryTemplate;
	private final RestTemplate restTemplate;

	@Value("${app.source-root-url}") private String sourceRootUrl;

	public byte[] fetchOriginalImage(String reference) {
		try {
			return retryTemplate.execute((RetryCallback<byte[], Throwable>) context -> fetchImage(buildUrl(reference)));
		} catch (Throwable e) {
			log.error("Could not access/connect with the URL with retry.", e);
			throw new NotFoundException(e.getMessage(), e);
		}
	}

	private byte[] fetchImage(String url) {
		try {
			return restTemplate.getForEntity(url, byte[].class).getBody();
		} catch (NotFound exception) {
			log.info("The requested source image does not exist.", exception);
			throw new NotFoundException("The requested source image does not exist.", exception);
		}
	}

	private String buildUrl(String reference) {
		return sourceRootUrl.concat(reference);
	}
}
