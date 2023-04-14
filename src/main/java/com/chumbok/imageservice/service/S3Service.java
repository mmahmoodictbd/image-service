package com.chumbok.imageservice.service;

import com.chumbok.imageservice.dto.ImageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static com.chumbok.imageservice.util.FileUtil.getFileName;
import static com.chumbok.imageservice.util.FileUtil.guessContentType;
import static org.springframework.http.MediaType.parseMediaType;
import static software.amazon.awssdk.services.s3.model.ObjectCannedACL.PUBLIC_READ;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

	private final S3Client s3Client;
	@Qualifier("imageSaveRetryTemplate") private final RetryTemplate retryTemplate;

	@Value("${app.aws.s3-root-bucket}") private String bucketName;

	public ImageResponse getImage(final String imagePath) {
		var responseInputStream = s3Client.getObject(buildGetObjectRequest(imagePath));
		return new ImageResponse(
			getFileName(imagePath),
			parseMediaType(responseInputStream.response().contentType()),
			responseInputStream,
			responseInputStream.response().contentLength()
		);
	}

	@Async
	public void saveImageAsync(final String imagePath, final byte[] imageBytes) {
		try {
			retryTemplate.execute((RetryCallback<Void, SdkException>) context -> {
				saveImage(imagePath, imageBytes);
				return null;
			});
		} catch (SdkException exception) {
			log.error("Could not write the image to the S3 after retry.", exception);
		}

	}

	public void deleteImage(final String imagePath) {
		s3Client.deleteObject(getDeleteObjectRequest(imagePath));
	}

	public boolean isImageExist(final String imagePath) {
		try {
			s3Client.headObject(buildHeadObjectRequest(imagePath));
		} catch (NoSuchKeyException noSuchKeyException) {
			return false;
		}
		return true;
	}

	private void saveImage(final String imagePath, final byte[] imageBytes) {
		try {
			s3Client.putObject(buildPutRequest(imagePath, guessContentType(imageBytes)), RequestBody.fromBytes(imageBytes));
		} catch (SdkException exception) {
			log.warn("Could not write the image to the S3.", exception);
		}
	}

	private HeadObjectRequest buildHeadObjectRequest(final String imagePath) {
		return HeadObjectRequest.builder().bucket(bucketName).key(imagePath).build();
	}

	private GetObjectRequest buildGetObjectRequest(final String originalImagePath) {
		return GetObjectRequest.builder().bucket(bucketName).key(originalImagePath).build();
	}

	private DeleteObjectRequest getDeleteObjectRequest(final String imagePath) {
		return DeleteObjectRequest.builder().bucket(bucketName).key(imagePath).build();
	}

	private PutObjectRequest buildPutRequest(final String imagePath, final String contentType) {
		return PutObjectRequest.builder().bucket(bucketName).key(imagePath).contentType(contentType).acl(PUBLIC_READ).build();
	}
}
