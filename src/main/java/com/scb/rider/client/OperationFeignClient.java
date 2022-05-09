package com.scb.rider.client;

import com.scb.rider.model.dto.ConfigDataResponse;
import com.scb.rider.model.dto.training.SeatsUpdate;
import com.scb.rider.model.dto.training.TrainingDto;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;

@FeignClient(name = "operationFeignClient", url = "${rider.client.operation-service}")
public interface OperationFeignClient {

    String OPS_CONFIG = "ops/config";
    String OPS_TRAININGS_BASE_PATH = "ops/trainings";

	@PutMapping(value = OPS_TRAININGS_BASE_PATH + "/update-seats", consumes = MediaType.APPLICATION_JSON_VALUE)
    Boolean updateOccupiedSlotSeats(@RequestBody @Valid final SeatsUpdate seatsUpdate);

    @GetMapping(value = OPS_TRAININGS_BASE_PATH +"/{slotId}", produces = MediaType.APPLICATION_JSON_VALUE)
    TrainingDto getTrainingSlotDetails(@PathVariable("slotId") String slotId);

    @GetMapping(value = OPS_TRAININGS_BASE_PATH +  "/get-by-date-rider" + "/{onboardDate}", produces = MediaType.APPLICATION_JSON_VALUE)
	Object getTrainingSlotsByDateForRider(@PathVariable("onboardDate") String onboardDate
            , @RequestParam(name = "trainingType", required = false) String trainingType);

    @GetMapping(value = OPS_TRAININGS_BASE_PATH + "/required", produces = MediaType.APPLICATION_JSON_VALUE)
    List<String> getAllRequiredTrainings();

    @GetMapping(value = OPS_CONFIG + "/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    ConfigDataResponse getConfigData(@PathVariable("key") String key);

    @GetMapping(value = OPS_CONFIG + "/list/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    List<ConfigDataResponse> getListConfigData(@PathVariable("key") List<String> key);
}
