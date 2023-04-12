package com.chumbok.imageservice.util;

import com.chumbok.imageservice.exception.IORuntimeException;
import lombok.experimental.UtilityClass;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;

@UtilityClass
public class FileUtil {

	public static final String DIR_SEPARATOR = "/";

	public static String getFileName(String imagePath) {
		var lastIndex = imagePath.lastIndexOf(DIR_SEPARATOR);
		return lastIndex > -1 ? imagePath.substring(lastIndex + 1) : imagePath;
	}

	public static String guessContentTypeFromStream(byte[] imageBytes) {
		var inputStream = new ByteArrayInputStream(imageBytes);
		try {
//			return "image/jpeg";
			return URLConnection.guessContentTypeFromStream(inputStream);
		} catch (IOException e) {
			throw new IORuntimeException("Could not guess content type from image bytes.", e);
		}
	}
}
