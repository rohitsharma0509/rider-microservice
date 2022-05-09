package com.scb.rider.service;


import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.document.RiderRemarksDetails;
import com.scb.rider.model.dto.SearchResponseDto;
import com.scb.rider.model.enumeration.RiderStatus;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.repository.RiderRemarksDetailRepository;
import com.scb.rider.service.document.RiderRemarksService;


@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderRemarkServiceTest {

    @Mock
	private RiderProfileRepository riderProfileRepository;
    @Mock
	private RiderRemarksDetailRepository detailRepository;
   
    @InjectMocks
    private RiderRemarksService riderRemarksService;
   
    private String riderId = "1234";


    @Test
    public void saveRiderRemarkTest() {

        RiderRemarksDetails rTest = RiderRemarksDetails.builder()
        		.date(LocalDate.now())
        		.time(LocalTime.now())
        		.build();
        
        RiderProfile riderProfile = new RiderProfile();
        riderProfile.setId("1234");
        riderProfile.setPhoneNumber("1231313");
        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        when(riderProfileRepository.findById(riderId)).thenReturn(Optional.of(riderProfile));

        when(detailRepository.save(rTest)).thenReturn(rTest);
        
        RiderRemarksDetails result = riderRemarksService.saveRiderRemarksInfo("1234", rTest);

        assertTrue(ObjectUtils.isNotEmpty(result));
    }

    
    @Test
    public void deleteRiderRemarkTest() {

        RiderRemarksDetails rTest = RiderRemarksDetails.builder()
        		.date(LocalDate.now())
        		.time(LocalTime.now())
        		.build();
       
        when(detailRepository.findById("213")).thenReturn(Optional.of(rTest));
        
        riderRemarksService.deleteRiderRemarksInfo("213");
    }
    
    @Test
    public void getAllRiderRemarkTest() {

    	ArrayList<RiderRemarksDetails> riderRemark = new ArrayList<>(); 
    	
    	RiderRemarksDetails rTest = RiderRemarksDetails.builder()
        		.date(LocalDate.now())
        		.time(LocalTime.now())
        		.build();
    	riderRemark.add(rTest);
    	
		Page<RiderRemarksDetails> data=new PageImpl<>(riderRemark);
		List<Order> order = new ArrayList<>();
		order.add(new Order(Sort.Direction.ASC , "date"));
		 Pageable pageable=PageRequest.of(1, 1,Sort.by(order));
		 List<String> filterquery=new ArrayList<>();
		 filterquery.add("time:12:30");
		 filterquery.add("date:12");
		 
		 when(detailRepository.getAllRemarks("123", "", "", "123", pageable)).thenReturn(data);
		 
		 SearchResponseDto result = riderRemarksService.getRemarksBySearchTermWithFilter("123", filterquery, pageable);
	        assertTrue(ObjectUtils.isNotEmpty(result));
		 
		 
		 
    }
    
    @Test
    public void getAllRiderRemarkTestWithOutFilter() {

    	ArrayList<RiderRemarksDetails> riderRemark = new ArrayList<>(); 
    	
    	RiderRemarksDetails rTest = RiderRemarksDetails.builder()
        		.date(LocalDate.now())
        		.time(LocalTime.now())
        		.build();
    	riderRemark.add(rTest);
    	
		Page<RiderRemarksDetails> data=new PageImpl<>(riderRemark);
		 Pageable pageable=PageRequest.of(1, 1);
		 List<String> filterquery=new ArrayList<>();
		 
		 when(detailRepository.findByRiderId("123", pageable)).thenReturn(data);
		 
		 SearchResponseDto result = riderRemarksService.getRemarksBySearchTermWithFilter("123", filterquery, pageable);
	        assertTrue(ObjectUtils.isNotEmpty(result));
		 
		 
		 
    }
 
    @Test
    public void getAllRiderRemarkTestWithOutRemark() {

    	ArrayList<RiderRemarksDetails> riderRemark = new ArrayList<>(); 
    	
    	Page<RiderRemarksDetails> data=new PageImpl<>(riderRemark);
		 Pageable pageable=PageRequest.of(1, 1);
		 List<String> filterquery=new ArrayList<>();
		 
		 when(detailRepository.findByRiderId("123", pageable)).thenReturn(data);
		 
		 SearchResponseDto result = riderRemarksService.getRemarksBySearchTermWithFilter("123", filterquery, pageable);
	        assertTrue(ObjectUtils.isNotEmpty(result));
		 
		 
		 
    }
    
    
}
