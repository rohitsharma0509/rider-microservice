package com.scb.rider.service.job;

import com.scb.rider.model.enumeration.RiderJobStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RiderJobFactory {
    private static final Map<RiderJobStatus, RiderJobService> map = new HashMap<>();
    @Autowired
    private RiderJobAcceptedService riderJobAcceptedService;
    @Autowired
    private RiderCalledMerchantService riderCalledMerchantService;
    @Autowired
    private RiderArrivedAtMerchantService riderArrivedAtMerchantService;
    @Autowired
    private RiderMealPickedUpService riderMealPickedUpService;
    @Autowired
    private RiderParkingReceiptPhotoService riderParkingReceiptPhotoService;
    @Autowired
    private RiderFoodDeliveredService riderFoodDeliveredService;
    @Autowired
    private RiderJobCancelByOperationService riderJobCancelByOperationService;
    @Autowired
    private RiderArrivedAtCustLocationService riderArrivedAtCustLocationService;
    @Autowired
    private List<RiderJobService> services;

    @PostConstruct
    public void initServiceCache() {
        for (RiderJobService service : services) {
            map.put(service.getStatusType(), service);
        }
    }

    public RiderJobService getRiderJob(RiderJobStatus jobStatus) {
        RiderJobService riderJobServiceSupplier = map.get(jobStatus);
        if (riderJobServiceSupplier != null) {
            return riderJobServiceSupplier;
        }
        throw new IllegalArgumentException("No such Job Status " + jobStatus.name());
    }

    public void setServices(List<RiderJobService> services) {
        this.services = services;
    }
}
