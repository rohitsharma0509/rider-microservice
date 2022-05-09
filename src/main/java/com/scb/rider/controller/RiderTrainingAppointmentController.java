package com.scb.rider.controller;

import com.scb.rider.client.OperationFeignClient;
import com.scb.rider.constants.Constants;
import com.scb.rider.constants.UrlMappings.RiderTraining;
import com.scb.rider.model.dto.RiderSearchProfileDto;
import com.scb.rider.model.dto.training.RiderTrainingAppointmentDetailsDto;
import com.scb.rider.model.dto.training.RiderTrainingAppointmentStatusResponse;
import com.scb.rider.model.dto.training.RiderTrainingStatusUpdateDto;
import com.scb.rider.model.enumeration.TrainingType;
import com.scb.rider.service.document.RiderTrainingAppointmentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

@RestController
@Log4j2
@RequestMapping(RiderTraining.TRAINING)
@Api(value = "Rider Profile Training Appointment Endpoints")
public class RiderTrainingAppointmentController {

	private static final String OPS_CONFIG = "/ops/config";

  @Autowired
	RiderTrainingAppointmentService appointmentService;
	
	@Autowired
    OperationFeignClient operationFeignClient;
	
    @ApiOperation(nickname = "get-rider-profile-training-status-by-id",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
            value = "Gets Rider's Training Status by RiderID", 
            response = RiderTrainingAppointmentStatusResponse.class
    )
    @GetMapping(value = "/{riderId}" + RiderTraining.STATUS, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RiderTrainingAppointmentStatusResponse> getRiderTrainingStatusByRiderId(
            @ApiParam(value = "riderId", example = "RR01234", required = true) @PathVariable("riderId") String riderId
            , @RequestParam(name = "trainingType", required = false, defaultValue = "FOOD") TrainingType trainingType) {
        return ResponseEntity.ok(appointmentService.getAppointmentStatusByProfileId(riderId, trainingType));
    }
	
    @ApiOperation(nickname = "get-available-training-slots-for-rider",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
            value = "Gets Available Training Slots for Rider" 
    )
    @GetMapping("/available-slots" + "/{onboardDate}")
    public ResponseEntity<?> getAvailableTrainingSlotsForRider(@PathVariable("onboardDate") String onboardDate
            , @RequestParam(name = "trainingType", required = false) String trainingType) {
        return ResponseEntity.ok(appointmentService.getAvailableSlotsByRider(onboardDate, trainingType)) ;
    }
    
    @ApiOperation(nickname = "get-rider-profile-training-appointment-details-by-id",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
            value = "Gets Rider's Training Appointment Details by RiderID", 
            response = RiderTrainingAppointmentDetailsDto.class
    )
    @GetMapping(value = "/{riderId}" + RiderTraining.APPOINTMENT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RiderTrainingAppointmentDetailsDto> getRiderTrainingAppointmentDetailsByRiderId(
            @ApiParam(value = "riderId", example = "RR01234", required = true) @PathVariable("riderId") String riderId
            , @RequestParam(name = "trainingType", required = false, defaultValue = "FOOD") TrainingType trainingType) {
        return ResponseEntity.ok(RiderTrainingAppointmentDetailsDto.of(appointmentService.getAppointmentByProfileId(riderId, trainingType)));
    }

    @GetMapping(value = "/{riderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RiderTrainingAppointmentDetailsDto>> getAllAppointmentsByRiderId(
            @ApiParam(value = "riderId", example = "RR01234", required = true) @PathVariable("riderId") String riderId) {
        return ResponseEntity.ok(RiderTrainingAppointmentDetailsDto.of(appointmentService.getAllTrainingAppointmentsByRiderId(riderId)));
    }

    @ApiOperation(nickname = "get-rider-profile-training-status-by-id",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
            value = "Saves Rider's Training Status by RiderID", 
            response = RiderTrainingAppointmentStatusResponse.class
    ) 
	@PostMapping(value = RiderTraining.STATUS, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RiderTrainingStatusUpdateDto> updateRiderTrainingStatus(
            @RequestAttribute(name = Constants.X_USER_ID, required = false) String userId,
			@RequestBody @Valid RiderTrainingStatusUpdateDto trainingUpdateDto) {
        trainingUpdateDto.setTrainingType(Objects.nonNull(trainingUpdateDto.getTrainingType()) ? trainingUpdateDto.getTrainingType() : TrainingType.FOOD);
		return ResponseEntity.ok(appointmentService.updateAppointmentStatusByProfileId(trainingUpdateDto, userId));
    }

    @ApiOperation(nickname = "save-rider-profile-training-appointment-by-id",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
            value = "Saves Rider's Selected Training Appointment Details by RiderID", 
            response = RiderTrainingAppointmentDetailsDto.class
    )
    @PostMapping(value = RiderTraining.APPOINTMENT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RiderTrainingAppointmentDetailsDto> updateRiderTrainingAppointmentByRiderId(
            @RequestAttribute(name = Constants.X_USER_ID, required = false) String userId,
    		@RequestBody final RiderTrainingAppointmentDetailsDto appointmentDetails) {
        appointmentDetails.setUpdatedBy(userId);
        appointmentDetails.setTrainingType(Objects.nonNull(appointmentDetails.getTrainingType()) ? appointmentDetails.getTrainingType() : TrainingType.FOOD);
        return ResponseEntity.ok(RiderTrainingAppointmentDetailsDto.of(appointmentService.saveSelectedAppointment(appointmentDetails)));	
    }

    @ApiOperation(nickname = "get-rider-profile-list-by-training-slotId",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
            value = "Get List of Riders for particular Training Slot",
            response = RiderSearchProfileDto.class
    )
    @GetMapping(value = RiderTraining.APPOINTMENT+"/{appointmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RiderSearchProfileDto>> getRidersByTrainingAppointmentId(
            @ApiParam(value = "appointmentId", example = "600aa386ace87a69976c3dfc", required = true)
            @PathVariable("appointmentId") String appointmentId) {
        return ResponseEntity.ok(appointmentService.getRidersListBySlotId(appointmentId)) ;
    }
    
    @GetMapping(value = OPS_CONFIG + "/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getConfigData(
            @ApiParam(value = "key", example = "trainingConfigurableWeeks", required = true)
            @PathVariable("key") String key) {
        return ResponseEntity.ok(operationFeignClient.getConfigData(key));
    }
}
