package com.chumbok.imageservice.config;


import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.aws")
public record AwsProperties(@NotEmpty String region, @NotEmpty String accessKey, @NotEmpty String secretKey, @NotEmpty String s3Endpoint,
							@NotEmpty String s3RootBucket) {}
