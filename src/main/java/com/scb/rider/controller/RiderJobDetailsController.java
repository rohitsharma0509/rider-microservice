package com.scb.rider.controller;

import static com.scb.rider.constants.UrlMappings.RIDER_API;
import static com.scb.rider.constants.UrlMappings.RiderJobDetailsUri.RIDER_JOB;
import static com.scb.rider.constants.UrlMappings.RiderJobDetailsUri.RIDER_RECONCILIATION;

import com.fasterxml.jackson.annotation.JsonView;
import com.scb.rider.constants.Constants;
import com.scb.rider.exception.FileConversionException;
import com.scb.rider.exception.InvalidImageExtensionException;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.dto.RiderJobDetailsDto;
import com.scb.rider.model.dto.RiderSettlementDetails;
import com.scb.rider.model.enumeration.CancellationSource;
import com.scb.rider.model.enumeration.RiderJobStatus;
import com.scb.rider.service.RiderSettlementDetailsService;
import com.scb.rider.service.job.RiderJobDetailsService;
import com.scb.rider.service.job.RiderJobFactory;
import com.scb.rider.service.job.RiderJobService;
import com.scb.rider.view.View;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(RIDER_API)
@Log4j2
public class RiderJobDetailsController {

    @Autowired
    private RiderJobFactory riderJobFactorySupplier;
    @Autowired
    private RiderSettlementDetailsService riderSettlementDetailsService;
    @Autowired
    private RiderJobDetailsService riderJobDetailsService;

    @PostMapping(value = RIDER_JOB, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    // @formatter:off
    @ApiOperation(nickname = "rider-job-details-handling",
            value = "Handling Job Details for Rider",
            notes = "",
            produces = "application/json", consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "One records created successfully"),
            @ApiResponse(code = 400, message = "Could not create records for supplied input"),
            @ApiResponse(code = 404, message = "The API could not be found")
    })
    @JsonView(value = View.RiderJobDetailsView.class)
    public ResponseEntity<RiderJobDetails> riderJobStatus(
            @RequestAttribute(name = Constants.X_USER_ID, required = false) String userId,
            @ApiParam(value = "Record id", example = "0a800160-6c23-121e-816c-2737d6610003", required = true)
            @PathVariable(name = "id", required = true) @NotEmpty final String id,
            @ApiParam(name = "file", value = "Select the file to Upload", required = false)
            @RequestParam(name = "file", required = false) MultipartFile file,
            @ApiParam(name = "jobId", value = "Job Id ", required = true)
            @RequestParam(name = "jobId", required = true) String jobId,
            @ApiParam(name = "jobStatus", value = "Job Status ", required = true)
            @RequestParam(name = "jobStatus", required = true) RiderJobStatus jobStatus,
            @ApiParam(name = "remark", value = "Remark  ", required = false)
            @RequestParam(name = "remark", required = false) String remark,
            @ApiParam(name = "parkingFee", value = "Parking Fee  ", required = false)
            @RequestParam(name = "parkingFee", required = false) BigDecimal parkingFee,
            @ApiParam(name = "jobPrice", value = "Job Price", required = false)
            @RequestParam(name = "jobPrice", required = false) BigDecimal jobPrice,
            @ApiParam(name = "isJobPriceModified", value = "Is Job Price Modified by Ops Member", required = false)
            @RequestParam(name = "isJobPriceModified", required = false) Boolean isJobPriceModified,
            @ApiParam(name = "source", value = "Cancellation Source", required = false)
            @RequestParam(name = "source", required = false, defaultValue = "RBH") CancellationSource source,
            @ApiParam(name = "timeStamp", value = "Time Stamp  ", example = "2020-12-13T17:09:42.411", required = false)
            @RequestParam(name = "timeStamp", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timeStamp)
            throws IOException, InvalidImageExtensionException, FileConversionException {

        log.info("Received request to update rider job status to {} for jobId {} and riderProfileId {} ", jobStatus, jobId, id);
        RiderJobService riderJobService = riderJobFactorySupplier.getRiderJob(jobStatus);

        return ResponseEntity.status(HttpStatus.CREATED).body(riderJobService
                .performActionRiderJobStatus(file, id, jobId, parkingFee, jobPrice, isJobPriceModified, remark, source ,timeStamp, userId));

    }

    @GetMapping(value = RIDER_RECONCILIATION, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RiderSettlementDetails>> fetchRiderJobDetailsStatus(
            @ApiParam(name = "startTime", value = "Time Stamp  ", example = "2020-12-13T17:09:42.411", required = false)
            @RequestParam(name = "startTime", required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @ApiParam(name = "endTime", value = "Time Stamp  ", example = "2020-12-13T17:09:42.411", required = false)
            @RequestParam(name = "endTime", required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        List<RiderSettlementDetails> riderJobService = riderSettlementDetailsService.getRiderSettlementDetails(startTime,
                endTime);

        return ResponseEntity.status(HttpStatus.OK).body(riderJobService);

    }


  @ApiOperation(nickname = "get-rider-job-details-by-job-id",
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
      value = "Get Rider Job details by Job Id", response = RiderJobDetailsDto.class
  )
  @GetMapping(value = "/job/{job-id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<RiderJobDetailsDto> fetchJobDetails(
      @ApiParam(value = "job-id", example = "S210104123456", required = true)
      @PathVariable("job-id") String jobId)
      {
        log.info("Getting Rider Job details by job id = {}", jobId);
    return ResponseEntity.status(HttpStatus.OK).body(riderJobDetailsService.getRiderJobDetails(jobId));
  }

}
