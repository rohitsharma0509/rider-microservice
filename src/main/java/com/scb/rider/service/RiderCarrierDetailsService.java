package com.scb.rider.service;

import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.model.dto.RiderCarrierDetailsRequestDto;
import com.scb.rider.model.document.RiderCarrierDetails;
import com.scb.rider.repository.RiderCarrierDetailsRepository;
import com.scb.rider.util.CustomBeanUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class RiderCarrierDetailsService {

    @Autowired
    private RiderCarrierDetailsRepository riderCarrierDetailsRepository;

    public RiderCarrierDetails saveCarrierDetails(String riderId, RiderCarrierDetailsRequestDto updateRequestDto) {
        RiderCarrierDetails riderCarrierDetails = riderCarrierDetailsRepository.findByRiderId(riderId);
        if(ObjectUtils.isEmpty(riderCarrierDetails)){
            log.info("adding carrier details for rider with id {}", riderId);
            riderCarrierDetails = new RiderCarrierDetails();
            riderCarrierDetails.setRiderId(riderId);
        }
        log.info("updating carrier details for rider with id : {}", riderId);
        CustomBeanUtils.copyNonNullProperties(updateRequestDto, riderCarrierDetails);
        riderCarrierDetailsRepository.save(riderCarrierDetails);
        return riderCarrierDetails;
    }

    public RiderCarrierDetails getCarrierDetails(String riderId) {
        RiderCarrierDetails carrierDetails = riderCarrierDetailsRepository.findByRiderId(riderId);
        if(ObjectUtils.isNotEmpty(carrierDetails))
            return carrierDetails;
        else
            throw new DataNotFoundException("Record not found for id " + riderId);
    }
}
