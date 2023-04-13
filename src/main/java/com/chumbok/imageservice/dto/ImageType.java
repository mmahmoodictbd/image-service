package com.chumbok.imageservice.dto;

public record ImageType(String name,
						int height,
						int width,
						int quality,
						ScaleType scaleType,
						String fillColorHexCode,
						FileExtension type
) {}