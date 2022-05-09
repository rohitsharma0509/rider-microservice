package com.scb.rider.controller;

import static com.scb.rider.constants.UrlMappings.RIDER_API;
import static com.scb.rider.constants.UrlMappings.FileHandler.DOWNLOAD_FILE;
import static com.scb.rider.constants.UrlMappings.FileHandler.MULTI_UPLOAD_FILE;
import static com.scb.rider.constants.UrlMappings.FileHandler.UPLOAD_FILE;
import java.io.IOException;
import java.util.Optional;
import javax.validation.constraints.NotEmpty;

import com.scb.rider.constants.Constants;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import com.scb.rider.constants.DocumentType;
import com.scb.rider.exception.FileConversionException;
import com.scb.rider.exception.InvalidImageExtensionException;
import com.scb.rider.model.document.RiderUploadedDocument;
import com.scb.rider.model.dto.RiderUploadedDocumentResponse;
import com.scb.rider.service.document.RiderUploadedDocumentService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(RIDER_API)
public class FileHandlerController {

	@Autowired
	private RiderUploadedDocumentService uploadedDocumentService;

	/**
	 * To upload the documents
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(value = UPLOAD_FILE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	// @formatter:off
	@ApiOperation(nickname = "upload-rider-driving-license", value = "Upload Rider Driving License Document", notes = "", produces = "application/json", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "One records created successfully"),
			@ApiResponse(code = 400, message = "Could not create records for supplied input"),
			@ApiResponse(code = 404, message = "The API could not be found") })
	public ResponseEntity<RiderUploadedDocumentResponse> uploadFile(
			@RequestAttribute(name = Constants.X_USER_ID, required = false) String userId,
			@ApiParam(value = "Record id", example = "0a800160-6c23-121e-816c-2737d6610003", required = true) @PathVariable(name = "id", required = true) @NotEmpty final String id,
			@ApiParam(name = "file", value = "Select the file to Upload", required = true) @RequestPart("file") MultipartFile file,
			@RequestParam(name = "docType") DocumentType documentType,
			@RequestParam(name = "foodBoxSize", required = false) String foodBoxSize)
			throws InvalidImageExtensionException, FileConversionException {
		RiderUploadedDocument uploadedDocument = uploadedDocumentService.uploadedDocument(file, id, documentType, userId, foodBoxSize);
		return ResponseEntity.status(HttpStatus.CREATED).body(RiderUploadedDocumentResponse.of(uploadedDocument));

	}

	@GetMapping(value = UPLOAD_FILE, produces = MediaType.APPLICATION_JSON_VALUE)
	// @formatter:off
	@ApiOperation(nickname = "upload-rider-driving-license", value = "Upload Rider Driving License Document", notes = "", produces = "application/json")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "One records fetch successfully"),
			@ApiResponse(code = 404, message = "The API could not be found") })
	public ResponseEntity getUploadedFile(
			@ApiParam(value = "Record id", example = "0a800160-6c23-121e-816c-2737d6610003", required = true) @PathVariable(name = "id", required = true) @NotEmpty final String id,
			@RequestParam(name = "docType", required = true) DocumentType documentType) {

		Optional<RiderUploadedDocument> uploadedDocument = uploadedDocumentService.fetchDocument(id, documentType);

		if (!uploadedDocument.isPresent()) {
			HttpHeaders headers = new HttpHeaders();
			headers.add("X-info", "Document not found with given profile id");
			return ResponseEntity.notFound().headers(headers).build();
		}
		return ResponseEntity.status(HttpStatus.OK).body(RiderUploadedDocumentResponse.of(uploadedDocument.get()));
	}

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@PostMapping(value = MULTI_UPLOAD_FILE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RiderUploadedDocumentResponse> uploadMultipleDocument(
			@RequestAttribute(name = Constants.X_USER_ID, required = false) String userId,
			@PathVariable(name = "id") @NotEmpty final String riderId,
			@RequestParam(name = "docType") DocumentType documentType,
			@RequestParam(name = "replaceExisting", defaultValue = "true") Boolean replaceExisting,
			@RequestPart("files") MultipartFile[] files) throws InvalidImageExtensionException, IOException {
		log.info("uploading multiple {} for riderId {}", documentType, riderId);
		RiderUploadedDocument uploadedDocument = uploadedDocumentService.uploadMultipleDocument(riderId, documentType, files, userId, replaceExisting);
		return ResponseEntity.status(HttpStatus.CREATED).body(RiderUploadedDocumentResponse.of(uploadedDocument));
	}

	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(value = DOWNLOAD_FILE, produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE})
	// @formatter:off
	@ApiOperation(nickname = "download-rider-documents", value = "Download Rider Driving License Document", notes = "", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "One records fetch successfully"),
			@ApiResponse(code = 404, message = "The API could not be found") })
	public ResponseEntity<byte[]> getDownloadFile(
			@ApiParam(value = "Record id", example = "0a800160-6c23-121e-816c-2737d6610003", required = true) @PathVariable(name = "id", required = true) @NotEmpty final String id,
			@RequestParam(name = "documentKey", required = false) String documentkey,
			@RequestParam(name = "docType", required = false) DocumentType documentType) throws IOException {

		log.debug(String.format("Rider id - %s document key - %s downloaded", id, documentkey));
		if (!ObjectUtils.isEmpty(documentType)) {
			documentkey = uploadedDocumentService.getDownloadFileName(id, documentType);
		}
		byte[] resource = uploadedDocumentService.downloadDocument(id, documentkey);

		return ResponseEntity.ok().header("Content-type", "application/octet-stream")
				.header("Content-disposition", "attachment; filename=\"" + documentkey + "\"").body(resource);

	}
}
