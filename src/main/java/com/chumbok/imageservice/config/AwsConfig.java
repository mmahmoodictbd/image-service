package com.chumbok.imageservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

import static software.amazon.awssdk.auth.credentials.AwsBasicCredentials.create;

@Configuration
@RequiredArgsConstructor
public class AwsConfig {

	private final AwsProperties properties;

	@Bean
	public S3Client s3Client() {
		var awsCredentials = create(properties.accessKey(), properties.secretKey());
		return S3Client.builder()
			.endpointOverride(URI.create(properties.s3Endpoint()))
			.credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
			.region(Region.of(properties.region()))
			.build();
	}
}
