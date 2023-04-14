package com.chumbok.imageservice.service;

import com.chumbok.imageservice.dto.ImageType;
import com.chumbok.imageservice.exception.IORuntimeException;
import org.imgscalr.Scalr;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

import static java.awt.Color.WHITE;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.util.Optional.ofNullable;
import static javax.imageio.ImageWriteParam.MODE_EXPLICIT;
import static org.imgscalr.Scalr.Mode.AUTOMATIC;
import static org.imgscalr.Scalr.OP_ANTIALIAS;

@Service
public class ImageProcessingService {

	private static final String DEFAULT_IMAGE_FORMAT = "png";

	public byte[] process(final byte[] imageBytes) {
		var bufferedImage = findBufferedImage(imageBytes).orElseThrow(() -> new IORuntimeException("Invalid image bytes"));
		var resizedBytes = resize(bufferedImage, bufferedImage.getWidth(), bufferedImage.getHeight(), DEFAULT_IMAGE_FORMAT);
		return optimize(findBufferedImage(resizedBytes).get(), DEFAULT_IMAGE_FORMAT, 1f);
	}

	public byte[] process(final byte[] imageBytes, final ImageType type) {
		var bufferedImage = findBufferedImage(imageBytes).orElseThrow(() -> new IORuntimeException("Invalid image bytes"));
		var format = type.type().name().toLowerCase();
		var resizedBytes = resize(bufferedImage, type.width(), type.height(), format);
		return optimize(findBufferedImage(resizedBytes).get(), format, (float) type.quality() / 100);
	}

	private byte[] resize(final BufferedImage bufferedImage, final int width, final int height, final String format) {
		return toByteArray(Scalr.resize(bufferedImage, Scalr.Method.AUTOMATIC, AUTOMATIC, width, height, OP_ANTIALIAS), format);
	}

	private byte[] optimize(final BufferedImage bufferedImage, final String format, final float quality) {
		var imageWriterIterator = ImageIO.getImageWritersByFormatName(format);
		var imageWriter = imageWriterIterator.next();
		var param = imageWriter.getDefaultWriteParam();
		param.setCompressionMode(MODE_EXPLICIT);
		param.setCompressionQuality(quality);

		try (var byteArrayOutputStream = new ByteArrayOutputStream(); var ios = ImageIO.createImageOutputStream(byteArrayOutputStream)) {
			imageWriter.setOutput(ios);
			imageWriter.write(null, new IIOImage(bufferedImage, null, null), param);
			imageWriter.dispose();
			return byteArrayOutputStream.toByteArray();
		} catch (IOException e) {
			throw new IORuntimeException("Could not optimize image.", e);
		}
	}

	private byte[] toByteArray(final BufferedImage bufferedImage, final String format) {
		try (var byteArrayOutputStream = new ByteArrayOutputStream()) {
			var formatConvertedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), TYPE_INT_RGB);
			formatConvertedImage.createGraphics().drawImage(bufferedImage, 0, 0, WHITE, null);
			ImageIO.write(formatConvertedImage, format, byteArrayOutputStream);
			return byteArrayOutputStream.toByteArray();
		} catch (IOException e) {
			throw new IORuntimeException("Could not convert BufferedImage to bytes", e);
		}
	}

	private Optional<BufferedImage> findBufferedImage(final byte[] bytes) {
		try (var inputStream = new ByteArrayInputStream(bytes)) {
			return ofNullable(ImageIO.read(inputStream));
		} catch (IOException e) {
			throw new IORuntimeException("Could not read image bytes", e);
		}
	}
}
