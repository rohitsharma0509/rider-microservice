package com.scb.rider.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.scb.rider.constants.Constants;
import com.scb.rider.exception.FileConversionException;
import com.scb.rider.exception.InvalidImageExtensionException;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.dto.ImageDto;
import com.scb.rider.model.dto.JobDetails;
import com.scb.rider.model.dto.RiderJobAcceptedDetails;
import com.scb.rider.model.enumeration.CancellationSource;
import com.scb.rider.model.enumeration.RiderJobStatus;
import com.scb.rider.service.job.RiderJobDetailsService;
import com.scb.rider.service.job.RiderJobFactory;
import com.scb.rider.service.job.RiderJobService;
import com.scb.rider.util.CustomMultipartFile;
import com.scb.rider.view.View;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;

import static com.scb.rider.constants.UrlMappings.RIDER_API;
import static com.scb.rider.constants.UrlMappings.RiderJobDetailsUri.RIDER_JOB;
import static com.scb.rider.constants.UrlMappings.VERSION_v2;

@RestController
@RequestMapping("/" + VERSION_v2 + RIDER_API)
@Log4j2
public class RiderJobDetailsControllerVersion2 {

	@Autowired
	private RiderJobFactory riderJobFactorySupplier;
	@Autowired
	private RiderJobDetailsService riderJobDetailsService;

	@PostMapping(value = RIDER_JOB, consumes = { MediaType.APPLICATION_JSON_VALUE })
	@ApiOperation(nickname = "rider-job-details-handling", value = "Handling Job Details for Rider", notes = "", produces = "application/json", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "One records created successfully"),
			@ApiResponse(code = 400, message = "Could not create records for supplied input"),
			@ApiResponse(code = 404, message = "The API could not be found") })
	@JsonView(value = View.RiderJobDetailsView.class)
	public ResponseEntity<RiderJobAcceptedDetails> riderJobStatus(
			@RequestAttribute(name = Constants.X_USER_ID, required = false) String userId,
			@ApiParam(value = "Record id", example = "0a800160-6c23-121e-816c-2737d6610003", required = true) @PathVariable(name = "id", required = true) @NotEmpty final String id,
			@ApiParam(name = "jobId", value = "Job Id ", required = true) @RequestParam(name = "jobId", required = true) String jobId,
			@ApiParam(name = "jobStatus", value = "Job Status ", required = true) @RequestParam(name = "jobStatus", required = true) RiderJobStatus jobStatus,
			@ApiParam(name = "remark", value = "Remark  ", required = false) @RequestParam(name = "remark", required = false) String remark,
			@ApiParam(name = "parkingFee", value = "Parking Fee  ", required = false) @RequestParam(name = "parkingFee", required = false) BigDecimal parkingFee,
			@ApiParam(name = "jobPrice", value = "Job Price", required = false) @RequestParam(name = "jobPrice", required = false) BigDecimal jobPrice,
			@ApiParam(name = "isJobPriceModified", value = "Is Job Price Modified by Ops Member", required = false) @RequestParam(name = "isJobPriceModified", required = false) Boolean isJobPriceModified,
			@ApiParam(name = "source", value = "Cancellation Source", required = false) @RequestParam(name = "source", required = false, defaultValue = "RBH") CancellationSource source,
			@ApiParam(name = "timeStamp", value = "Time Stamp  ", example = "2020-12-13T17:09:42.411", required = false) @RequestParam(name = "timeStamp", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timeStamp,
			@RequestBody(required = false) ImageDto imageDto)
			throws IOException, InvalidImageExtensionException, FileConversionException {

		RiderJobService riderJobService = riderJobFactorySupplier.getRiderJob(jobStatus);
		CustomMultipartFile customMultipartFile = null;
		if (!ObjectUtils.isEmpty(imageDto)) {

			byte[] imageByte = Base64.getDecoder().decode(imageDto.getImageValue());
			customMultipartFile = new CustomMultipartFile(imageByte,
					imageDto.getImageName() + "." + imageDto.getImageExt());
			log.info("base64 converted to multipart for riderId-{}", id);
		}
		
		JobDetails jobDetails = null;
		if (RiderJobStatus.getStatusForCompleteJobResponse().contains(jobStatus)) {
			jobDetails = riderJobDetailsService.fetchJobDetails(jobId);
		}
		
		RiderJobDetails riderJobDetails = riderJobService.performActionRiderJobStatus(customMultipartFile,
				id, jobId, parkingFee, jobPrice, isJobPriceModified, remark, source, timeStamp, userId);

		RiderJobAcceptedDetails completeJobDetails = riderJobDetailsService.getCompleteJobDetails(riderJobDetails, jobDetails);

		jobDetails = riderJobDetailsService.fetchJobDetails(jobId);
		if(jobDetails != null) {
			completeJobDetails.setCustomerRemark(jobDetails.getCustomerRemark());
			completeJobDetails.setRemark(jobDetails.getRemark());
			completeJobDetails.setOrderItems(jobDetails.getOrderItems());
			completeJobDetails.setShopLandmark(jobDetails.getShopLandmark());
		}

		return ResponseEntity.status(HttpStatus.CREATED).body(completeJobDetails);

	}

}
