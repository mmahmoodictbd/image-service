package com.chumbok.imageservice.config;

import com.chumbok.imageservice.dto.ImageType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record ImageTypesConfigProperties(List<ImageType> imageTypes) {}
