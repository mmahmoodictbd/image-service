package com.chumbok.imageservice.util;

import com.chumbok.imageservice.exception.IORuntimeException;
import lombok.experimental.UtilityClass;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

@UtilityClass
public class FileUtil {

	public static final String DIR_SEPARATOR = "/";

	public static String getFileName(final String imagePath) {
		var lastIndex = imagePath.lastIndexOf(DIR_SEPARATOR);
		return lastIndex > -1 ? imagePath.substring(lastIndex + 1) : imagePath;
	}

	public static String guessContentType(final byte[] imageBytes) {
		var inputStream = new ByteArrayInputStream(imageBytes);
		try {
			return URLConnection.guessContentTypeFromStream(inputStream);
		} catch (IOException e) {
			throw new IORuntimeException("Could not guess content type from image bytes.", e);
		}
	}

	public static byte[] readAllBytes(final InputStream inputStream) {
		try {
			return inputStream.readAllBytes();
		} catch (IOException e) {
			throw new IORuntimeException("Could not read inputStream.", e);
		}
	}
}
