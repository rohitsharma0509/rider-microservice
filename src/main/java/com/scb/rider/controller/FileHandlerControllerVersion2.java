package com.scb.rider.controller;

import static com.scb.rider.constants.UrlMappings.RIDER_API;
import static com.scb.rider.constants.UrlMappings.VERSION_v2;
import static com.scb.rider.constants.UrlMappings.FileHandler.MULTI_UPLOAD_FILE;
import static com.scb.rider.constants.UrlMappings.FileHandler.UPLOAD_FILE;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

import javax.validation.constraints.NotEmpty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.scb.rider.constants.Constants;
import com.scb.rider.constants.DocumentType;
import com.scb.rider.exception.FileConversionException;
import com.scb.rider.exception.InvalidImageExtensionException;
import com.scb.rider.model.document.RiderUploadedDocument;
import com.scb.rider.model.dto.ImageDto;
import com.scb.rider.model.dto.RiderUploadedDocumentResponse;
import com.scb.rider.service.document.RiderUploadedDocumentService;
import com.scb.rider.util.CustomMultipartFile;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/" + VERSION_v2 + RIDER_API)
public class FileHandlerControllerVersion2 {

	@Autowired
	private RiderUploadedDocumentService uploadedDocumentService;

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(nickname = "upload-rider-driving-license", value = "Upload Rider Driving License Document", notes = "", produces = "application/json", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "One records created successfully"),
			@ApiResponse(code = 400, message = "Could not create records for supplied input"),
			@ApiResponse(code = 404, message = "The API could not be found") })
	@PostMapping(value = UPLOAD_FILE)
	public ResponseEntity<RiderUploadedDocumentResponse> uploadFile(
			@ApiParam(value = "profileid", example = "6033912d5fc9421cd4b0d71a", required = true) @PathVariable(name = "id", required = true) @NotEmpty final String id,
			@RequestBody ImageDto imageDto,
			@RequestAttribute(name = Constants.X_USER_ID, required = false) String userId,
			@RequestParam(name = "docType", required = true) DocumentType documentType,
			@RequestParam(name = "foodBoxSize", required = false) String foodBoxSize)
			throws InvalidImageExtensionException, FileConversionException {

		byte[] imageByte = Base64.getDecoder().decode(imageDto.getImageValue());
		CustomMultipartFile customMultipartFile = new CustomMultipartFile(imageByte,
				imageDto.getImageName() + "." + imageDto.getImageExt());
		log.info("base64 converted to multipart for riderId-{}", id);
		RiderUploadedDocument uploadedDocument = uploadedDocumentService.uploadedDocument(customMultipartFile, id,
				documentType, userId, foodBoxSize);
		return ResponseEntity.status(HttpStatus.CREATED).body(RiderUploadedDocumentResponse.of(uploadedDocument));

	}

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(value = MULTI_UPLOAD_FILE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RiderUploadedDocumentResponse> uploadMultipleDocument(
			@RequestAttribute(name = Constants.X_USER_ID, required = false) String userId,
			@PathVariable(name = "id") @NotEmpty final String riderId,
			@RequestParam(name = "docType") DocumentType documentType,
			@RequestParam(name = "replaceExisting", defaultValue = "true") Boolean replaceExisting,
			@RequestBody @NotEmpty ImageDto[] imageDto) throws InvalidImageExtensionException, IOException {

		log.info("uploading multiple {} for riderId {}", documentType, riderId);

		CustomMultipartFile[] files = Arrays.stream(imageDto).map(image -> {
			byte[] imageByte = Base64.getDecoder().decode(image.getImageValue());
			return new CustomMultipartFile(imageByte, image.getImageName() + "." + image.getImageExt());
		}).toArray(CustomMultipartFile[]::new);

		RiderUploadedDocument uploadedDocument = uploadedDocumentService.uploadMultipleDocument(riderId, documentType,
				files, userId, replaceExisting);
		return ResponseEntity.status(HttpStatus.CREATED).body(RiderUploadedDocumentResponse.of(uploadedDocument));
	}

}
