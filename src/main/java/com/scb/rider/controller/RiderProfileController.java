package com.scb.rider.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.constants.Constants;
import com.scb.rider.exception.MandatoryFieldMissingException;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.*;
import com.scb.rider.model.enumeration.AvailabilityStatus;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import com.scb.rider.model.enumeration.RiderStatus;
import com.scb.rider.service.document.RiderMannerScoreService;
import com.scb.rider.service.document.RiderProfileService;
import com.scb.rider.service.document.RiderSuspensionService;
import com.scb.rider.validator.AppConfigValidator;
import io.swagger.annotations.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

@RestController
@Log4j2
@RequestMapping("/profile")
@Api(value = "Rider Profile Endpoints")
public class RiderProfileController {
    @Autowired
    private RiderProfileService riderProfileService;

    @Autowired
    private AppConfigValidator appConfigValidator;

    @Autowired
    private RiderMannerScoreService riderMannerScoreService;

    @Autowired
    private RiderSuspensionService riderSuspensionService;

    public static final String RIDER_PROFILE_INFO = "Getting Rider Profile by id = {}";

    @ApiOperation(nickname = "create-rider-profile-details", value = "Creates Rider Profile Details", response = RiderProfileDto.class)
    @ApiResponses(value = {
            @ApiResponse(response = RiderProfileDto.class, code = 201, message = "One record created successfully"),
            @ApiResponse(code = 400, message = "Could not create records for supplied input")
    })
    @ApiImplicitParam(name = "id", dataType = "String", paramType = "path", dataTypeClass = RiderProfileDto.class, value = "Rider Profile Details")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RiderProfileDto> createRiderProfile(@RequestBody @Valid final RiderProfileDto profileDto) {
		if (ObjectUtils.isEmpty(profileDto.getAddress()) && ObjectUtils.isEmpty(profileDto.getNationalAddress())) {
			throw new MandatoryFieldMissingException("Address or national Address is missing");
		}

    	log.info("Creating rider with id {} name = {}", profileDto.getId(), profileDto.getFirstName());
        return ResponseEntity.status(HttpStatus.CREATED).body(RiderProfileDto.of(this.riderProfileService.createRiderProfile(profileDto)));
    }


    @ApiOperation(nickname = "update-rider-profile-details", value = "updates Rider Profile Details", response = RiderProfileDto.class)
    @ApiResponses(value = {
            @ApiResponse(response = RiderProfileDto.class, code = 201, message = "One record updated successfully"),
            @ApiResponse(code = 400, message = "Could not update records for supplied input")
    })
    @ApiImplicitParam(name = "id", dataType = "RiderProfileDto", paramType = "path", dataTypeClass = RiderProfileDto.class, value = "Rider Profile Details")
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RiderProfileDto> updateRiderProfile(@Valid @NotNull @RequestBody final RiderProfileUpdateRequestDto updateRequestDto) throws AccessDeniedException {
        log.info("updating rider with id {}", updateRequestDto.getId());
        return ResponseEntity.ok(RiderProfileDto.of(this.riderProfileService.updateRiderProfile(updateRequestDto)));
    }

    @ApiOperation(nickname = "update-rider-profile-details-ops-member", value = "updates Rider Profile Details", response = RiderProfileDto.class)
    @ApiResponses(value = {
        @ApiResponse(response = RiderProfileDto.class, code = 201, message = "One record updated successfully"),
        @ApiResponse(code = 400, message = "Could not update records for supplied input")
    })
    @ApiImplicitParam(name = "id", dataType = "RiderProfileDto", paramType = "path", dataTypeClass = RiderProfileDto.class, value = "Rider Profile Details")
    @PutMapping(value = "/ops", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RiderProfileDto> updateRiderProfileOpsMember(
            @RequestAttribute(name = Constants.X_USER_ID, required = false) String userId,
            @Valid @NotNull @RequestBody final RiderProfileUpdateRequestDto updateRequestDto) throws AccessDeniedException {
        log.info("updating rider with id {}", updateRequestDto.getId());
        updateRequestDto.setUpdatedBy(userId);
        return ResponseEntity.ok(RiderProfileDto.of(this.riderProfileService.updateRiderProfileOpsMember(updateRequestDto)));
    }

    @ApiOperation(nickname = "get-rider-profile-by-id",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
            value = "Gets Rider profile by ID", response = RiderProfileDto.class
    )
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RiderProfileDto> getRiderProfileById(
            @ApiParam(value = "id", example = "5fc35ef7af8a144ac42a0a54", required = true)
            @PathVariable("id") String id) {
        log.info(RIDER_PROFILE_INFO, id);
        return ResponseEntity.ok(RiderProfileDto.of(this.riderProfileService.getRiderProfileById(id)));
    }

    @GetMapping(value = "/mob/{phoneNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RiderProfileDto> getRiderProfileByPhoneNumber(
            @ApiParam(value = "phoneNumber", example = "999999999", required = true)
            @PathVariable("phoneNumber") String phoneNumber) {
        log.info(RIDER_PROFILE_INFO, phoneNumber);
        return ResponseEntity.ok(RiderProfileDto.of(this.riderProfileService.getRiderProfileByPhoneNumber(phoneNumber)));
    }

    @PutMapping(value = "{riderId}/status/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RiderProfileDto> setRiderStatus(@RequestHeader(name = "appVersion", required = false, defaultValue = "0") Integer appVersion,
                                                          @PathVariable("riderId") String riderId,
                                                          @PathVariable("status")  AvailabilityStatus status) {
        log.info(RIDER_PROFILE_INFO, riderId);
        appConfigValidator.validateAppVersion(appVersion);
        return ResponseEntity.ok(RiderProfileDto.of(this.riderProfileService.setRiderStatus(riderId, status)));
    }

    @PutMapping(value = "/{id}/nationalAddress", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RiderProfileDto> updateNationalAddress( @PathVariable("id") String id,
                                                          @Valid @NotNull @RequestBody final NationalAddressUpdateRequestDto nationalAddressUpdateRequestDto) {
        log.info(RIDER_PROFILE_INFO, id);
        return ResponseEntity.ok(RiderProfileDto.of(this.riderProfileService.updateRiderNationalAddress(id, nationalAddressUpdateRequestDto)));
    }

    @ApiOperation(nickname = "get-rider-profile-id-list-by-zone-id",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
            value = "Gets Rider profile Id List by ID", response = RiderIdList.class
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RiderIdList> getRiderProfileByZoneId(
            @ApiParam(type = "String", value = "Zone ID", required = false)
            @RequestParam(value = "zoneId", required = false) final String zoneId,
            @ApiParam(type = "String", value = "Rider Availability Status")
            @RequestParam(value = "status", required = false) final AvailabilityStatus status,
            @RequestParam(value = "riderStatus", required = false) final RiderStatus riderStatus
    ) {
        log.info("Getting Rider Profile by zone id = {}", zoneId);
        if(StringUtils.isEmpty(zoneId)){
            return ResponseEntity.ok(RiderIdList.of(this.riderProfileService.getRiderProfileByAvailabilityStatus(status, riderStatus)));
        }
        return ResponseEntity.ok(RiderIdList.of(this.riderProfileService.getRiderProfileByZoneId(zoneId,status)));
    }

    @ApiOperation(nickname = "get-rider-profile-with-scoring-params",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
            value = "Gets Rider profiles with scoring params", response = RiderScoringParamsDto.class
    )
    @GetMapping(value = "/scoring-params", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity <List<RiderScoringParamsDto>> getRiderProfileWithScoringParams(
            @RequestParam(value = "status", required = false) final AvailabilityStatus status,
            @RequestParam(value = "riderStatus", required = false) final RiderStatus riderStatus
    ) {
        log.info("Inside getRiderProfileWithScoringParams");
        return ResponseEntity.ok((riderProfileService.getRiderProfileWithScoringParams(status, riderStatus)));
    }


    @PutMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(nickname = "update-rider-status", value = "Update status of Rider", notes = "Update status of Rider")
	public ResponseEntity<RiderStatusDto> updateRiderStatus(
            @RequestAttribute(name = Constants.X_USER_ID, required = false) String userId,
			@Valid @NotNull @RequestBody RiderStatusDto riderStatusDto) {
        riderStatusDto.setUpdatedBy(userId);
		RiderStatusDto returnedRiderStatusDto = riderProfileService.updateRiderStatus(riderStatusDto);
		return ResponseEntity.ok(returnedRiderStatusDto);
	}

    @PutMapping(value = "{id}/national-id-status/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(nickname = "update-national-id-status", value = "Update status of nationalId", notes = "Update status of nationalId")
    public ResponseEntity<Boolean> updateNationalIdStatus(
            @RequestAttribute(name = Constants.X_USER_ID, required = false) String userId,
            @PathVariable(name = "id") String riderId, @PathVariable(name="status") MandatoryCheckStatus status,
            @RequestParam(value = "reason", required = false) final String reason,
            @RequestParam(value = "comment", required = false) final String comment) {
    	if(MandatoryCheckStatus.REJECTED.equals(status) && ObjectUtils.isEmpty(reason)){
            log.info("Rejection Reason Not found provided");
             throw new MandatoryFieldMissingException("reason field is missing");
         }
        Boolean isUpdated = riderProfileService.updateNationalIdStatus(riderId, status, reason, comment, userId);
        return ResponseEntity.ok(isUpdated);
    }

    @PutMapping(value = "{id}/profile-photo-status/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(nickname = "update-profile-photo-status", value = "Update status of profile photo", notes = "Update status of profile photo")
    public ResponseEntity<Boolean> updateProfilePhotoStatus(
            @RequestAttribute(name = Constants.X_USER_ID, required = false) String userId,
            @PathVariable(name = "id") String riderId, @PathVariable(name="status") MandatoryCheckStatus status,
            @RequestParam(value = "reason", required = false) final String reason,
            @RequestParam(value = "comment", required = false) final String comment) {
        if(MandatoryCheckStatus.REJECTED.equals(status) && ObjectUtils.isEmpty(reason)){
           log.info("Rejection Reason Not found provided");
            throw new MandatoryFieldMissingException("reason field is missing");
        }
        Boolean isUpdated = riderProfileService.updateProfilePhotoStatus(riderId, status, reason, comment, userId);
        return ResponseEntity.ok(isUpdated);
    }

    @GetMapping(value =  "{mobileNum}/stage" , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getRiderProfileStage(@PathVariable("mobileNum") String mobileNum) throws Exception{
    	LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put("profile_stage", this.riderProfileService.getRiderProfileStage(mobileNum));
		String retVal =  new ObjectMapper().writeValueAsString(map);
    	return ResponseEntity.ok(retVal);
    }

    @PostMapping(value = "/details")
    public ResponseEntity<List<RiderProfileDto>> getRiferProfilesByRiderIds(@RequestBody @Valid final List<String> riderIds) {
        return ResponseEntity.ok(RiderProfileDto.of(this.riderProfileService.getRiferProfilesByRiderIds(riderIds)));
    }

    @GetMapping(value = "/shortProfile/{type}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RiderShortProfile> riderShortProfile(@PathVariable("type") String type, @PathVariable("id") String id){
        return ResponseEntity.status(HttpStatus.OK).body(riderProfileService.getRiderShortProfile(type, id));
    }

    @PutMapping("/rentingToday")
    public List<RiderProfile> updateRentingTodayFlag(@RequestBody RentingTodayRequest updateRentingTodayRequest) {


        return riderProfileService.updateRentingTodayFlag(updateRentingTodayRequest);
    }

    @PutMapping("/rentingTodayAsFalse")
    public void updateRentingTodayAsFalse(){
        riderProfileService.updateRentingTodayAsFalse();
    }

    @GetMapping("/evEnrolledRiders")
    public List<RiderProfile> evEnrolledRiders(@RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                                @RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate){
        return riderProfileService.evRidersList(startDate, endDate);
    }

    @ApiOperation(nickname = "get-all-riders-profile-id-list",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
            value = "Gets all authorized Rider profiles", response = PaginatedRiderDetailsList.class
    )
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedRiderDetailsList> getAllRidersProfileList(@RequestParam(value = "zoneId", required = false) String zoneId, @RequestParam("page") int page,
                                                                             @RequestParam("size") int size) {
        log.info("Getting All Rider Profiles");
        return ResponseEntity.ok(this.riderProfileService.getAllRiderProfile(zoneId, page, size));
    }
    @PutMapping(value = "/update/phonenumber")
    public ResponseEntity<RiderProfile> updateRiderPhoneNumber(@Valid @NotNull @RequestBody final RiderProfileUpdateRequestDto updateRequestDto){
        log.info("updating phone number for rider {}", updateRequestDto.getId());
        return ResponseEntity.ok(this.riderProfileService.updateRiderPhoneNumber(updateRequestDto));

    }

    @PutMapping(value = "/mannerScore", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RiderProfileUpdateMannerScoreResponseDto> riderProfileMannerScore(@Valid @NotNull @RequestBody final RiderProfileUpdateMannerScoreDto riderProfileUpdateMannerScoreDto
            , HttpServletRequest request) throws AccessDeniedException {
        log.info("updating manner score rider with id {}", riderProfileUpdateMannerScoreDto.getRiderId());
        return ResponseEntity.ok(this.riderProfileService.updateRiderProfileMannerScore(riderProfileUpdateMannerScoreDto, request));
    }

    @GetMapping(value = "/suspension-history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedRiderSuspensionHistoryList> getSuspensionHistoryList(
            @RequestParam(name = "riderId", required = true) String riderId,
            @PageableDefault(page = 0, size = 50) @SortDefault.SortDefaults(@SortDefault(sort = "createdDate", direction = Sort.Direction.DESC)) Pageable pageable) {
        log.info("Getting Suspension History");
        return ResponseEntity.ok(this.riderSuspensionService.getSuspensionHistoryList(riderId, pageable));
    }

    @GetMapping(value = "/manner-score-history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaginatedRiderMannerScoreHistoryList> getMannerScoreHistoryList(
            @RequestParam(name = "riderId", required = true) String riderId,
            @PageableDefault(page = 0, size = 50) @SortDefault.SortDefaults(@SortDefault(sort = "createdDate", direction = Sort.Direction.DESC)) Pageable pageable) {
        log.info("Getting Manner Score History");
        return ResponseEntity.ok(this.riderMannerScoreService.getMannerScoreHistoryList(riderId, pageable));
    }

    @GetMapping(value = "/validate/{accountNumber}")
    public ResponseEntity<Boolean> validateAccountNumber(
            @PathVariable("accountNumber") String accountNumber) {
        return ResponseEntity.ok(appConfigValidator.validateCreditAccountNumber(accountNumber));
    }
}