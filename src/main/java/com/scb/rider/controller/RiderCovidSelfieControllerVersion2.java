package com.scb.rider.controller;

import static com.scb.rider.constants.UrlMappings.RIDER_API;
import static com.scb.rider.constants.UrlMappings.VERSION_v2;
import static com.scb.rider.constants.UrlMappings.FileHandler.COVID_SELFIE;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;

import javax.validation.constraints.NotEmpty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.scb.rider.exception.FileConversionException;
import com.scb.rider.exception.InvalidImageExtensionException;
import com.scb.rider.model.document.RiderCovidSelfie;
import com.scb.rider.model.dto.ImageDto;
import com.scb.rider.service.document.RiderCovidSelfieService;
import com.scb.rider.util.CustomMultipartFile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/"+VERSION_v2+ RIDER_API)
public class RiderCovidSelfieControllerVersion2 {

	@Autowired
	private RiderCovidSelfieService covidSelfieService;

	/**
	 * To upload the covid selfie documents
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(value = COVID_SELFIE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RiderCovidSelfie> uploadCovidSelfieFile(
			@PathVariable(name = "id", required = true) @NotEmpty final String id,
			@RequestBody ImageDto imageDto,
			@RequestParam(name = "uploadedTime", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime uploadedTime)
			throws InvalidImageExtensionException, FileConversionException {

		byte[] imageByte = Base64.getDecoder().decode(imageDto.getImageValue());
		CustomMultipartFile customMultipartFile = new CustomMultipartFile(imageByte,
				imageDto.getImageName() + "." + imageDto.getImageExt());
		log.info("base64 converted to multipart for riderId-{}", id);

		log.info("Uploading Covid Selfie for Rider id - {} ", id);

		RiderCovidSelfie uploadedDocument = covidSelfieService.uploadCovidSelfie(customMultipartFile, id, uploadedTime);

		return ResponseEntity.status(HttpStatus.CREATED).body(uploadedDocument);

	}

}
