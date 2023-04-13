package com.chumbok.imageservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

import static software.amazon.awssdk.services.s3.model.BucketCannedACL.PUBLIC_READ;

@Configuration
@RequiredArgsConstructor
public class S3BucketInitConfig {

	private final S3Client s3Client;

	@Value("${app.aws.s3-root-bucket}") private String bucketName;

	@EventListener(ApplicationReadyEvent.class)
	public void createBucketIfNotExist() {
		var headBucketRequest = HeadBucketRequest.builder()
			.bucket(bucketName)
			.build();
		try {
			s3Client.headBucket(headBucketRequest);
		} catch (NoSuchBucketException e) {
			s3Client.createBucket(CreateBucketRequest.builder()
				.bucket(bucketName)
				.acl(PUBLIC_READ)
				.build()
			);
		}
	}
}
