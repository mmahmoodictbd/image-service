package com.chumbok.imageservice.util;

import lombok.experimental.UtilityClass;

import static com.chumbok.imageservice.util.FileUtil.DIR_SEPARATOR;

@UtilityClass
public class ReferenceUtil {

	private static final char FORWARD_SLASH = '/';
	private static final char UNDERSCORE = '_';

	public static String buildS3ImagePath(final String imageType, final String reference) {
		return imageType + DIR_SEPARATOR + convertToS3FilePath(reference);
	}

	/**
	 * Convert to s3 file path string.
	 * Filename is - reference with forward slash replaced
	 * If the length of the reference without the extension is
	 *                    more than 8, directory structure is 	- /<first4CharOfReference>/<second4CharOfReference>/filename.extension
	 *    more than 4 and less than 8, directory structure is 	- /<first4CharOfReference>/filename.extension
	 * @param reference the reference
	 * @return the string
	 */
	public static String convertToS3FilePath(final String reference) {
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

	public static String convertToUniqueFilename(final String reference) {
		return reference.replace(FORWARD_SLASH, UNDERSCORE);
	}

	private static String stripFileExtension(final String reference) {
		var indexOfFileExtension = reference.lastIndexOf('.');
		return indexOfFileExtension > -1 ? reference.substring(0, indexOfFileExtension) : reference;
	}

	private static String getFileExtension(final String reference) {
		var indexOfFileExtension = reference.lastIndexOf('.');
		return indexOfFileExtension > -1 ? reference.substring(indexOfFileExtension + 1) : "";
	}
}
