package com.chumbok.imageservice.dto;

import org.springframework.http.MediaType;

import java.io.InputStream;

public record ImageResponse(String fileName, MediaType contentType, InputStream inputStream, long contentLength) {}