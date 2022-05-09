package com.scb.rider.service.job;

import com.scb.rider.model.enumeration.RiderJobStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderJobFactoryTest {

    @InjectMocks
    private RiderJobFactory factory;

    @Mock
    private RiderJobAcceptedService riderJobAcceptedService;
    @Mock
    private RiderCalledMerchantService riderCalledMerchantService;
    @Mock
    private RiderArrivedAtMerchantService riderArrivedAtMerchantService;
    @Mock
    private RiderMealPickedUpService riderMealPickedUpService;
    @Mock
    private RiderParkingReceiptPhotoService riderParkingReceiptPhotoService;
    @Mock
    private RiderFoodDeliveredService riderFoodDeliveredService;
    @Mock
    private RiderJobCancelByOperationService riderJobCancelByOperationService;
    @Mock
    private RiderArrivedAtCustLocationService riderArrivedAtCustLocationService;

    private List<RiderJobService> services;

    @Before
    public void setup() throws Exception {
        services = Arrays.asList(riderJobAcceptedService, riderCalledMerchantService,
                riderArrivedAtMerchantService,riderMealPickedUpService,
                riderParkingReceiptPhotoService,riderFoodDeliveredService ,
                riderJobCancelByOperationService,riderArrivedAtCustLocationService);
        factory.setServices(services);
        when(riderJobAcceptedService.getStatusType()).thenReturn(RiderJobStatus.JOB_ACCEPTED);
        when(riderCalledMerchantService.getStatusType()).thenReturn(RiderJobStatus.CALLED_MERCHANT);
        when(riderArrivedAtMerchantService.getStatusType()).thenReturn(RiderJobStatus.ARRIVED_AT_MERCHANT);
        when(riderMealPickedUpService.getStatusType()).thenReturn(RiderJobStatus.MEAL_PICKED_UP);
        when(riderParkingReceiptPhotoService.getStatusType()).thenReturn(RiderJobStatus.PARKING_RECEIPT_PHOTO);
        when(riderFoodDeliveredService.getStatusType()).thenReturn(RiderJobStatus.FOOD_DELIVERED);
        when(riderJobCancelByOperationService.getStatusType()).thenReturn(RiderJobStatus.ORDER_CANCELLED_BY_OPERATOR);
        when(riderArrivedAtCustLocationService.getStatusType()).thenReturn(RiderJobStatus.ARRIVED_AT_CUST_LOCATION);

        factory.initServiceCache();
    }
    @Test
    public void getRiderJobTest(){
        RiderJobService riderJobService = factory.getRiderJob(RiderJobStatus.JOB_ACCEPTED);
        assertTrue(riderJobService instanceof  RiderJobAcceptedService);

        riderJobService = factory.getRiderJob(RiderJobStatus.CALLED_MERCHANT);
        assertTrue(riderJobService instanceof  RiderCalledMerchantService);

        riderJobService = factory.getRiderJob(RiderJobStatus.ARRIVED_AT_MERCHANT);
        assertTrue(riderJobService instanceof  RiderArrivedAtMerchantService);

        riderJobService = factory.getRiderJob(RiderJobStatus.MEAL_PICKED_UP);
        assertTrue(riderJobService instanceof  RiderMealPickedUpService);

        riderJobService = factory.getRiderJob(RiderJobStatus.PARKING_RECEIPT_PHOTO);
        assertTrue(riderJobService instanceof  RiderParkingReceiptPhotoService);

        riderJobService = factory.getRiderJob(RiderJobStatus.FOOD_DELIVERED);
        assertTrue(riderJobService instanceof  RiderFoodDeliveredService);

        riderJobService = factory.getRiderJob(RiderJobStatus.ORDER_CANCELLED_BY_OPERATOR);
        assertTrue(riderJobService instanceof  RiderJobCancelByOperationService);

        riderJobService = factory.getRiderJob(RiderJobStatus.ARRIVED_AT_CUST_LOCATION);
        assertTrue(riderJobService instanceof  RiderArrivedAtCustLocationService);


    }

    @Test(expected = IllegalArgumentException.class)
    public void getRiderJobIllegalArgumentExceptionTest() {
        RiderJobService riderJobService = factory.getRiderJob(RiderJobStatus.valueOf("ABC"));
        assertTrue(riderJobService instanceof RiderJobAcceptedService);
    }
}
