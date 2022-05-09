package com.scb.rider.service;

import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.enumeration.RiderStatus;
import com.scb.rider.repository.RiderJobDetailsRepository;
import com.scb.rider.repository.RiderProfileRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.scb.rider.model.enumeration.RiderStatus.AUTHORIZED;

@Service
@Log4j2
public class RiderApprovalDateService {

    private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    @Autowired
    private RiderProfileRepository riderProfileRepository;

    @Autowired
    private RiderJobDetailsRepository riderJobDetailsRepository;

    public Long update() {
        Pageable pageable = PageRequest.of(0, 100, Sort.by("profileId"));
        List<RiderStatus> status = new ArrayList<>();
        status.add(AUTHORIZED);
        Page page = null;
        do {
            page = riderProfileRepository.findAllByStatusInAndApprovalDateTimeIsNull(status, pageable);
            if(ObjectUtils.isNotEmpty(page.getContent())){
                List<RiderProfile> riders = page.getContent();
                if(ObjectUtils.isNotEmpty(riders)){
                    riders.forEach(this::updateRiderApprovalTime);
                }
            }

            pageable = page.nextPageable();
        }
        while (page.hasNext());

        Long pending = riderProfileRepository.countByStatusInAndApprovalDateTimeIsNull(status);
        log.info("pending riders -> {}",pending );

        return pending;
    }

    private void updateRiderApprovalTime(RiderProfile rider) {
        List<RiderJobDetails> riderJobDetails = riderJobDetailsRepository.findTop1ByProfileIdOrderByJobAcceptedTime(rider.getId());
        ZonedDateTime zonedUTC;
        if(ObjectUtils.isNotEmpty(riderJobDetails)){
            RiderJobDetails job = riderJobDetails.get(0);
            log.info("rider job details -> {} at time {}", job.getJobId(), job.getJobAcceptedTime());
            zonedUTC = job.getJobAcceptedTime().atZone(ZoneId.of("UTC"));
        }
        else{
            log.info("assigning current time for rider {}", rider.getRiderId());
            zonedUTC = LocalDateTime.now().atZone(ZoneId.of("UTC"));
        }
        ZonedDateTime zonedBangkok = zonedUTC.withZoneSameInstant(ZoneId.of("Asia/Bangkok"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN);
        String approvalDateTime = zonedBangkok.format(formatter);
        rider.setApprovalDateTime(approvalDateTime);
        riderProfileRepository.save(rider);
    }
}
