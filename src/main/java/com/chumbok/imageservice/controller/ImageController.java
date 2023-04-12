package com.chumbok.imageservice.controller;


import com.chumbok.imageservice.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
@RequestMapping("/image")
@RequiredArgsConstructor
public class ImageController {

	private final ImageService imageService;

	@ResponseBody
	@GetMapping({"/show/{imageType}/{seoName}", "/show/{imageType}"})
	public ResponseEntity<InputStreamResource> getImage(@PathVariable("imageType") String imageType,
														@PathVariable(value = "seoName", required = false) String seoName,
														@RequestParam String reference) {
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
	public void flush(@PathVariable("imageType") String imageType, @RequestParam String reference) {
		imageService.flush(imageType, reference);
	}
}
