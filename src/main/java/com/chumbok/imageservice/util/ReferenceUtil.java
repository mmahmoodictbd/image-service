package com.chumbok.imageservice.util;

import lombok.experimental.UtilityClass;

import static com.chumbok.imageservice.util.FileUtil.DIR_SEPARATOR;

@UtilityClass
public class ReferenceUtil {

	private static final char FORWARD_SLASH = '/';
	private static final char UNDERSCORE = '_';

	public static String buildS3ImagePath(String imageType, String reference) {
		return imageType + DIR_SEPARATOR + convertToS3FilePath(reference);
	}

	public static String convertToS3FilePath(String reference) {
		var filename = convertToUniqueFilename(stripFileExtension(reference));

		var pathStringBuilder = new StringBuilder();
		if (filename.length() > 8) {
			pathStringBuilder.append(filename, 0, 4);
			pathStringBuilder.append(FORWARD_SLASH);
			pathStringBuilder.append(filename, 4, 8);
			pathStringBuilder.append(FORWARD_SLASH);
		} else if (filename.length() > 4 && filename.length() <= 8) {
			pathStringBuilder.append(filename, 0, 4);
			pathStringBuilder.append(FORWARD_SLASH);
		}

		return pathStringBuilder.append(filename).append(".").append(getFileExtension(reference)).toString();
	}

	public static String convertToUniqueFilename(String reference) {
		return reference.replace(FORWARD_SLASH, UNDERSCORE);
	}

	private static String stripFileExtension(String reference) {
		var indexOfFileExtension = reference.lastIndexOf('.');
		return indexOfFileExtension > -1 ? reference.substring(0, indexOfFileExtension) : reference;
	}

	private static String getFileExtension(String reference) {
		var indexOfFileExtension = reference.lastIndexOf('.');
		return indexOfFileExtension > -1 ? reference.substring(indexOfFileExtension + 1) : "";
	}
}
