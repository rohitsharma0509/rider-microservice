package com.scb.rider.controller;

import static com.scb.rider.constants.UrlMappings.RIDER_API;

import java.util.List;

import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.scb.rider.constants.Constants;
import com.scb.rider.model.document.RiderRemarksDetails;
import com.scb.rider.model.dto.SearchResponseDto;
import com.scb.rider.service.document.RiderRemarksService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.log4j.Log4j2;


@RestController
@RequestMapping("/" + RIDER_API)
@Log4j2
public class RiderRemarksController {

	
	@Autowired
	private RiderRemarksService riderRemarksService;
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(nickname = "add-rider-remarks", value = "add rider remarks", notes = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "One records created successfully"),
			@ApiResponse(code = 400, message = "Could not create records for supplied input"),
			@ApiResponse(code = 404, message = "The API could not be found") })
	@PostMapping(value = "/{id}/add-remarks")
	public ResponseEntity<RiderRemarksDetails> saveRemark(
			@PathVariable(name = "id", required = true) @NotEmpty final String riderId,
			@RequestBody RiderRemarksDetails riderRemarks,
			@RequestAttribute(name = Constants.X_USER_ID, required = false) String userId) {
		return ResponseEntity.status(HttpStatus.CREATED).body(riderRemarksService
				.saveRiderRemarksInfo(riderId, riderRemarks));

	}

	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(nickname = "add-rider-remarks", value = "add rider remarks", notes = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "One records created successfully"),
			@ApiResponse(code = 400, message = "Could not create records for supplied input"),
			@ApiResponse(code = 404, message = "The API could not be found") })
	@DeleteMapping(value = "/{id}/delete-remarks")
	public ResponseEntity<Boolean> deleteRemark(
			@PathVariable(name = "id", required = true) @NotEmpty final String messageId,
			@RequestAttribute(name = Constants.X_USER_ID, required = false) String userId) {
		riderRemarksService.deleteRiderRemarksInfo(messageId);
		return ResponseEntity.status(HttpStatus.OK).body(Boolean.TRUE);

	}
	
	 @ApiOperation(nickname = "get-search-remark-by-date-time-remark",
		      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
		      value = "Gets jobs details", response = RiderRemarksDetails.class)
	  @GetMapping("/get-remarks/{riderId}")
	  public ResponseEntity<SearchResponseDto> getRemarksBySearchTerm(
				@PathVariable(name = "riderId", required = true) @NotEmpty final String riderId,
	      @ApiParam(value = "filterquery", example = "remark:asd",
	          required = false) @RequestParam(name = "filterquery", required = false) List<String> filterquery,
	      @PageableDefault(page = 0, size = 5) @SortDefault.SortDefaults(@SortDefault(sort = "date",
	          direction = Sort.Direction.ASC)) Pageable pageable) {
	    
	    if(!ObjectUtils.isEmpty(filterquery)) {
	      filterquery.forEach(obj ->  log.info(obj.toString()));
	    }
	    
	  return  ResponseEntity.ok(riderRemarksService.getRemarksBySearchTermWithFilter(riderId,filterquery,pageable));

	  }

}
