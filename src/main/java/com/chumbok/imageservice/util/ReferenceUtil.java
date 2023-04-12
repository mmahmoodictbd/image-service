package com.chumbok.imageservice.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ReferenceUtil {

	private static final String FORWARD_SLASH = "/";
	private static final String UNDERSCORE = "_";

	public static String convertToS3FilePath(String reference) {
		var filename = convertToUniqueFilename(stripFileExtension(reference));

		var pathStringBuilder = new StringBuilder();
		if (filename.length() > 8) {
			pathStringBuilder.append(filename, 0, 4);
			pathStringBuilder.append("/");
			pathStringBuilder.append(filename, 4, 8);
			pathStringBuilder.append("/");
		} else if (filename.length() > 4 && filename.length() <= 8) {
			pathStringBuilder.append(filename, 0, 4);
			pathStringBuilder.append("/");
		}

		return pathStringBuilder.append(filename).append(".").append(getFileExtension(reference)).toString();
	}

	private static String convertToUniqueFilename(String reference) {
		return reference.replaceAll(FORWARD_SLASH, UNDERSCORE);
	}

	private static String stripFileExtension(String reference) {
		var indexOfFileExtension = reference.lastIndexOf(".");
		return indexOfFileExtension > -1 ? reference.substring(0, indexOfFileExtension) : reference;
	}

	private static String getFileExtension(String reference) {
		var indexOfFileExtension = reference.lastIndexOf(".");
		return indexOfFileExtension > -1 ? reference.substring(indexOfFileExtension + 1) : "";
	}
}
