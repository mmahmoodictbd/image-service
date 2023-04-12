package com.chumbok.imageservice.service;

import com.chumbok.imageservice.dto.ImageType;
import org.springframework.stereotype.Service;

@Service
public class ImageProcessingService {

	public byte[] process(byte[] image, ImageType type) {
		byte[] resized = resize(image, type);
		return optimize(resized, type);
	}

	private byte[] resize(byte[] image, ImageType type) {
		// TODO: this method should image resize operation based on the image type
		return image;
	}

	private byte[] optimize(byte[] image, ImageType type) {
		// TODO: this method should image optimize operation based on the image type
		return image;
	}
}
