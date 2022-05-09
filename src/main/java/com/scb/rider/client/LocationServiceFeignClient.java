package com.scb.rider.client;

import com.scb.rider.model.RiderFoodBoxSize;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.scb.rider.model.dto.DistanceResponseEntity;
import com.scb.rider.model.dto.ZoneEntity;

@FeignClient(name = "locationFeignClient", url = "${rider.client.location-service}")
public interface LocationServiceFeignClient {

    @GetMapping(value = "/api/zone/getZone/{riderId}", produces = MediaType.APPLICATION_JSON_VALUE)
	ZoneEntity getRiderActiveZone(@PathVariable("riderId") String riderId) ;
    
    @GetMapping (value = "/api/distance", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<DistanceResponseEntity> getDistance(@RequestParam("longitudeFrom") Double longitudeFrom, @RequestParam("latitudeFrom") Double latitudeFrom
            , @RequestParam("longitudeTo") Double longitudeTo, @RequestParam("latitudeTo") Double latitudeTo);

    @DeleteMapping(value = "/api/rider/")
    void deleteRider(@RequestParam("riderId") String riderId);

    @PutMapping(value = "/profile/rider/food-box")
    RiderFoodBoxSize updateRiderFoodBoxSize(@RequestBody RiderFoodBoxSize riderFoodBoxSize);

    @PutMapping(value = "/profile/rider/express-rider")
    Boolean updateRiderToExpressRider(@RequestParam("riderId") String riderId, @RequestParam("isRiderExpress") Boolean isRiderExpress);

    @PutMapping(value = "/profile/rider/pointx-rider")
    Boolean updateRiderToPointXRider(@RequestParam("riderId") String riderId, @RequestParam("isRiderPointX") Boolean isRiderPointX);

    @PutMapping(value = "/profile/rider/mart-rider")
    Boolean updateRiderToMartRider(@RequestParam("riderId") String riderId, @RequestParam("isMartRider") Boolean isMartRider);
}
