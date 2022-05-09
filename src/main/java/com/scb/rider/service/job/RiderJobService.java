package com.scb.rider.service.job;

import com.scb.rider.exception.FoodAlreadyDeliveredException;
import com.scb.rider.exception.InvalidStateTransitionException;
import com.scb.rider.exception.JobCancelledByOperatorException;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.enumeration.CancellationSource;
import com.scb.rider.model.enumeration.RiderJobStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public interface RiderJobService {

    RiderJobStatus getStatusType();

    RiderJobDetails performActionRiderJobStatus(MultipartFile file, String profileId, String jobId,
                                                BigDecimal fee, BigDecimal jobPrice, Boolean isJobPriceModified, String remark, CancellationSource source, LocalDateTime timeStamp, String updatedBy);

    default void validateStateTransition(RiderJobStatus prevStatus, RiderJobDetails jobDetail, String jobId) {


        if (RiderJobStatus.ORDER_CANCELLED_BY_OPERATOR.equals(jobDetail.getJobStatus())) {
            throw new JobCancelledByOperatorException("Order is cancelled for the job id" + jobId);
        }
        if (RiderJobStatus.FOOD_DELIVERED.equals(jobDetail.getJobStatus())) {
            throw new FoodAlreadyDeliveredException("Food already delivered for job id:" + jobId);
        }
        if (!prevStatus.equals(jobDetail.getJobStatus())) {
            throw new InvalidStateTransitionException("Invalid job state transition for job id "+jobId);
        }
    }
}
