package com.chumbok.imageservice.controller;


import com.chumbok.imageservice.service.ImageService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@Controller
@Validated
@RequestMapping("/image")
@RequiredArgsConstructor
public class ImageController {

	private final ImageService imageService;

	@ResponseBody
	@GetMapping({"/show/{imageType}/*"})
	public ResponseEntity<InputStreamResource> getImage(
		@NotBlank(message = "imageType can not be empty") @PathVariable("imageType") String imageType,
		@NotBlank(message = "Request param reference can not be empty.") @Length(max = 1024) @RequestParam String reference) {

		var imageResponse = imageService.getImage(imageType, reference);
		var contentDisposition = ContentDisposition.inline().filename(imageResponse.fileName()).build();
		return ResponseEntity.ok()
			.header(CONTENT_DISPOSITION, contentDisposition.toString())
			.contentType(imageResponse.contentType())
			.contentLength(imageResponse.contentLength())
			.body(new InputStreamResource(imageResponse.inputStream()));
	}

	@ResponseStatus(NO_CONTENT)
	@DeleteMapping("/flush/{imageType}")
	public void flush(@NotBlank(message = "imageType can not be empty") @PathVariable("imageType") String imageType,
					  @NotBlank(message = "Request param reference can not be empty.") @Length(max = 1024)  @RequestParam String reference) {
		imageService.flush(imageType, reference);
	}
}
