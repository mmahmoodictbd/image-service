package com.chumbok.imageservice.service;

import com.chumbok.imageservice.dto.ImageType;
import com.chumbok.imageservice.exception.IORuntimeException;
import org.junit.jupiter.api.Test;

import static com.chumbok.imageservice.dto.FileExtension.JPG;
import static com.chumbok.imageservice.dto.ScaleType.FILL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImageProcessingServiceTest {

	private static final ImageType DETAIL_LARGE_IMAGE_TYPE
		= new ImageType("detail-large", 1000, 1000, 100, FILL, "#FFFFFF", JPG);

	private final ImageProcessingService imageProcessingService = new ImageProcessingService();

	@Test
	void testProcessOnInvalidImageBytesThrowsIORuntimeException() {
		var exceptionThatWasThrown = assertThrows(IORuntimeException.class, () ->
			imageProcessingService.process(new byte[0])
		);
		assertEquals("Invalid image bytes", exceptionThatWasThrown.getMessage());
	}

	@Test
	void testProcessWithImageTypeOnInvalidImageBytesThrowsIORuntimeException() {
		var exceptionThatWasThrown = assertThrows(IORuntimeException.class, () ->
			imageProcessingService.process(new byte[0], DETAIL_LARGE_IMAGE_TYPE)
		);
		assertEquals("Invalid image bytes", exceptionThatWasThrown.getMessage());
	}
}