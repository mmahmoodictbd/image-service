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

import static java.awt.Color.WHITE;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static javax.imageio.ImageWriteParam.MODE_EXPLICIT;
import static org.imgscalr.Scalr.Mode.AUTOMATIC;
import static org.imgscalr.Scalr.OP_ANTIALIAS;

@Service
public class ImageProcessingService {

	private static final String DEFAULT_IMAGE_FORMAT = "png";

	public byte[] process(byte[] imageBytes) {
		BufferedImage bufferedImage = toBufferedImage(imageBytes);
		byte[] resized = resize(bufferedImage, bufferedImage.getWidth(), bufferedImage.getHeight(), DEFAULT_IMAGE_FORMAT);
		return optimize(toBufferedImage(resized), DEFAULT_IMAGE_FORMAT, 1f);
	}

	public byte[] process(byte[] imageBytes, ImageType type) {
		var format = type.type().name().toLowerCase();
		byte[] resized = resize(toBufferedImage(imageBytes), type.width(), type.height(), format);
		return optimize(toBufferedImage(resized), format, (float) type.quality() / 100);
	}

	private byte[] resize(BufferedImage bufferedImage, int width, int height, String format) {
		return toByteArray(Scalr.resize(bufferedImage, Scalr.Method.AUTOMATIC, AUTOMATIC, width, height, OP_ANTIALIAS), format);
	}

	private byte[] optimize(BufferedImage bufferedImage, String format, float quality) {
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

	private byte[] toByteArray(BufferedImage bufferedImage, String format) {
		try (var byteArrayOutputStream = new ByteArrayOutputStream()) {
			var formatConvertedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), TYPE_INT_RGB);
			formatConvertedImage.createGraphics().drawImage(bufferedImage, 0, 0, WHITE, null);
			ImageIO.write(formatConvertedImage, format, byteArrayOutputStream);
			return byteArrayOutputStream.toByteArray();
		} catch (IOException e) {
			throw new IORuntimeException("Could not convert BufferedImage to bytes", e);
		}
	}

	private BufferedImage toBufferedImage(byte[] bytes) {
		try (var inputStream = new ByteArrayInputStream(bytes)) {
			return ImageIO.read(inputStream);
		} catch (IOException e) {
			throw new IORuntimeException("Could not read image bytes", e);
		}
	}
}
