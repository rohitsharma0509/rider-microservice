package com.scb.rider.IntegrationTest.controller;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.scb.rider.model.enumeration.TrainingType;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.client.OperationFeignClient;
import com.scb.rider.constants.DocumentType;
import com.scb.rider.exception.AppointmentIdNotFoundException;
import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.document.RiderSelectedTrainingAppointment;
import com.scb.rider.model.document.RiderUploadedDocument;
import com.scb.rider.model.dto.training.RiderTrainingAppointmentDetailsDto;
import com.scb.rider.model.dto.training.RiderTrainingAppointmentStatusResponse;
import com.scb.rider.model.dto.training.RiderTrainingStatusUpdateDto;
import com.scb.rider.model.dto.training.TrainingDto;
import com.scb.rider.model.enumeration.RiderTrainingStatus;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.repository.RiderTrainingAppointmentRepository;
import com.scb.rider.repository.RiderUploadedDocumentRepository;

@EnableConfigurationProperties
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class RiderTrainingAppointmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private RiderTrainingAppointmentRepository appointmentRepository;

    @MockBean
    private RiderUploadedDocumentRepository riderUploadedDocumentRepository;
    
    @Autowired
    private RiderProfileRepository profileRepository;

    @MockBean
    OperationFeignClient operationFeignClient;

    private static final String TRAINING_URL = "/profile/training";

    private static RiderTrainingAppointmentDetailsDto appointmentDetailsDto;
    private static RiderSelectedTrainingAppointment selectedTrainingAppointment;
    private static RiderProfile riderProfile;
    private static TrainingDto trainingDto;

    @BeforeAll
    static void setup() {
        appointmentDetailsDto = RiderTrainingAppointmentDetailsDto.builder()
                .riderId("R123456789qwertyuio")
                .trainingType(TrainingType.FOOD)
                .appointmentId("A12341234qqwertyuio")
                .venue("City Square")
                .date(LocalDate.now())
                .startTime(LocalTime.now())
                .endTime(LocalTime.now().plusHours(2))
                .status(RiderTrainingStatus.PENDING)
                .build();
        
        trainingDto = TrainingDto.builder()
				.id("A123456789qwertyuio")
				.venue("City Square")
				.startTime(LocalTime.now())
				.endTime(LocalTime.now().plusHours(2))
				.date(LocalDate.now())
				.build();

        riderProfile = new RiderProfile();
        riderProfile.setCreatedDate(LocalDateTime.now().minusDays(2));

        selectedTrainingAppointment = new RiderSelectedTrainingAppointment();
        selectedTrainingAppointment.setAppointmentId("A123456789qwertyuio");
        BeanUtils.copyProperties(appointmentDetailsDto, selectedTrainingAppointment);
    }

    @Test
    @Order(1)
    void test_UpdateRiderTrainingAppointmentByRiderId() throws Exception {
        RiderProfile rider = profileRepository.save(riderProfile);
        appointmentDetailsDto.setRiderId(rider.getId());
        Mockito.when(operationFeignClient.updateOccupiedSlotSeats(Mockito.any())).thenReturn(true);
		Mockito.when(operationFeignClient.getTrainingSlotDetails(Mockito.any())).thenReturn(trainingDto);

		RiderSelectedTrainingAppointment appoint=RiderSelectedTrainingAppointment.builder()
				.riderId(rider.getRiderId()).appointmentId(appointmentDetailsDto.getAppointmentId())
				.status(RiderTrainingStatus.PENDING).build();
		Mockito.when(appointmentRepository.findByRiderIdAndTrainingType(riderProfile.getId(), TrainingType.FOOD)).thenReturn(Optional.of(appoint));
		RiderSelectedTrainingAppointment riderAppoint=RiderSelectedTrainingAppointment.builder()
				.appointmentId(appointmentDetailsDto.getAppointmentId())
				.venue(appointmentDetailsDto.getVenue())
				.riderId(riderProfile.getRiderId()).build();
		
		Mockito.when(this.appointmentRepository.save(Mockito.any())).thenReturn(riderAppoint);
		
		Assumptions.assumeTrue(StringUtils.isNotBlank(appointmentDetailsDto.getRiderId()));
        String json = mapper.writeValueAsString(appointmentDetailsDto);

        MvcResult saveResponse = mockMvc.perform(MockMvcRequestBuilders.post(TRAINING_URL + "/appointment")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();

        assertEquals(HttpStatus.OK.value(), saveResponse.getResponse().getStatus());
        RiderTrainingAppointmentDetailsDto trainingAppointment = mapper.readValue(saveResponse.getResponse().getContentAsString(),
                RiderTrainingAppointmentDetailsDto.class);

        assertNotNull(trainingAppointment);
        assertEquals(appointmentDetailsDto.getAppointmentId(), trainingAppointment.getAppointmentId());
        assertEquals(appointmentDetailsDto.getVenue(), trainingAppointment.getVenue());

    }

    @Test
    @Order(2)
    void test_GetRiderTrainingAppointmentDetailsByRiderId() throws Exception {
    	RiderSelectedTrainingAppointment riderAppoint=RiderSelectedTrainingAppointment.builder()
				.appointmentId(appointmentDetailsDto.getAppointmentId())
				.venue(appointmentDetailsDto.getVenue())
				.status(RiderTrainingStatus.PENDING)
				.riderId(riderProfile.getRiderId()).build();
    	Mockito.when(this.appointmentRepository.findByRiderIdAndTrainingType(Mockito.any(), Mockito.eq(TrainingType.FOOD))).thenReturn(Optional.of(riderAppoint));
    	MvcResult appointmentResponse = mockMvc.perform(MockMvcRequestBuilders.get(TRAINING_URL + "/" + riderProfile.getRiderId() + "/appointment")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();

        assertEquals(HttpStatus.OK.value(), appointmentResponse.getResponse().getStatus());
        RiderTrainingAppointmentDetailsDto trainingAppointment = mapper.readValue(appointmentResponse.getResponse().getContentAsString(),
                RiderTrainingAppointmentDetailsDto.class);

        assertNotNull(trainingAppointment);
        assertEquals(appointmentDetailsDto.getAppointmentId(), trainingAppointment.getAppointmentId());
        assertEquals(appointmentDetailsDto.getVenue(), trainingAppointment.getVenue());
        assertEquals(RiderTrainingStatus.PENDING, trainingAppointment.getStatus());

    }

    @Test
    @Order(3)
    void test_UpdateRiderTrainingStatus() throws Exception {
    	RiderTrainingStatusUpdateDto statusUpdate = RiderTrainingStatusUpdateDto.builder()
                .riderId(appointmentDetailsDto.getRiderId())
                .status(RiderTrainingStatus.COMPLETED)
                .trainingType(TrainingType.FOOD)
                .completionDate(LocalDate.now())
                .build();
        String json = mapper.writeValueAsString(statusUpdate);
        RiderSelectedTrainingAppointment riderAppoint=RiderSelectedTrainingAppointment.builder()
				.appointmentId(appointmentDetailsDto.getAppointmentId())
				.venue(appointmentDetailsDto.getVenue())
				.status(RiderTrainingStatus.COMPLETED)
				.riderId(riderProfile.getRiderId()).build();
    	Mockito.when(this.appointmentRepository.findByRiderIdAndTrainingType(Mockito.any(), Mockito.eq(TrainingType.FOOD))).thenReturn(Optional.of(riderAppoint));
        
        RiderUploadedDocument doc=RiderUploadedDocument.builder()
        		.riderProfileId(appointmentDetailsDto.getRiderId())
        		.imageUrl("url.com").build();
        Optional<RiderUploadedDocument> document = Optional.of(doc);
        
        
        Mockito.when(riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType(riderProfile.getId(), DocumentType.BACKGROUND_VERIFICATION_FORM)).thenReturn(document);

        
        MvcResult appointmentStatusResponse = mockMvc.perform(MockMvcRequestBuilders.post(TRAINING_URL + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();

        assertEquals(HttpStatus.OK.value(), appointmentStatusResponse.getResponse().getStatus());
        RiderTrainingAppointmentDetailsDto trainingAppointment = mapper.readValue(appointmentStatusResponse.getResponse().getContentAsString(),
        		RiderTrainingAppointmentDetailsDto.class);

        assertNotNull(trainingAppointment);
        assertEquals(RiderTrainingStatus.COMPLETED, trainingAppointment.getStatus());
    }

    @Test
    @Order(4)
    void test_GetRiderTrainingStatusByRiderId() throws Exception {
    	RiderSelectedTrainingAppointment riderAppoint=RiderSelectedTrainingAppointment.builder()
				.appointmentId(appointmentDetailsDto.getAppointmentId())
				.venue(appointmentDetailsDto.getVenue())
				.status(RiderTrainingStatus.COMPLETED)
				.riderId(riderProfile.getRiderId()).build();
    	Mockito.when(this.appointmentRepository.findByRiderIdAndTrainingType(Mockito.any(), Mockito.eq(TrainingType.FOOD))).thenReturn(Optional.of(riderAppoint));
        
    	MvcResult appointmentStatusResponse = mockMvc.perform(MockMvcRequestBuilders.get(TRAINING_URL + "/" + appointmentDetailsDto.getRiderId() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();

        assertEquals(HttpStatus.OK.value(), appointmentStatusResponse.getResponse().getStatus());
        RiderTrainingAppointmentStatusResponse trainingAppointment = mapper.readValue(appointmentStatusResponse.getResponse().getContentAsString(),
                RiderTrainingAppointmentStatusResponse.class);

        assertNotNull(trainingAppointment);
        assertEquals(RiderTrainingStatus.COMPLETED, trainingAppointment.getStatus());

    }

    
    @Test
    @Order(5)
    void test_getAllSlots() throws Exception {
    	Mockito.when(operationFeignClient.getTrainingSlotsByDateForRider(Mockito.any(), Mockito.eq(TrainingType.FOOD.name()))).thenReturn(true);
		
    	MvcResult appointmentStatusResponse = mockMvc.perform(MockMvcRequestBuilders.get(TRAINING_URL +"/available-slots/2021-01-11")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();

        assertEquals(HttpStatus.OK.value(), appointmentStatusResponse.getResponse().getStatus());
       

    }
    
    @Test
    @Order(6)
    void test_getAllSlots_with_exception() throws Exception {
    	Mockito.when(operationFeignClient.getTrainingSlotsByDateForRider(Mockito.any(), Mockito.eq(TrainingType.FOOD.name()))).thenThrow(DataNotFoundException.class);
		
    	MvcResult appointmentStatusResponse = mockMvc.perform(MockMvcRequestBuilders.get(TRAINING_URL +"/available-slots/2021-01-11?trainingType=FOOD")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();

        assertEquals("Unable find training slots for the date -: 2021-01-11" , appointmentStatusResponse.getResolvedException().getMessage());
       

    }
    
    @Test
    @Order(7)
    void test_UpdateRiderTrainingAppointmentStatusByRiderId() throws Exception {
        RiderProfile rider = profileRepository.save(riderProfile);
        appointmentDetailsDto.setRiderId(rider.getId());
        Mockito.when(operationFeignClient.updateOccupiedSlotSeats(Mockito.any())).thenReturn(true);
		Mockito.when(operationFeignClient.getTrainingSlotDetails(Mockito.any())).thenReturn(trainingDto);

		RiderSelectedTrainingAppointment appoint=RiderSelectedTrainingAppointment.builder()
				.riderId(rider.getRiderId()).appointmentId(appointmentDetailsDto.getAppointmentId())
				.status(RiderTrainingStatus.COMPLETED).build();
		Mockito.when(appointmentRepository.findByRiderIdAndTrainingType(riderProfile.getId(), TrainingType.FOOD)).thenReturn(Optional.of(appoint));
		RiderSelectedTrainingAppointment riderAppoint=RiderSelectedTrainingAppointment.builder()
				.appointmentId(appointmentDetailsDto.getAppointmentId())
				.venue(appointmentDetailsDto.getVenue())
				.riderId(riderProfile.getRiderId()).build();
		
		Mockito.when(this.appointmentRepository.save(Mockito.any())).thenReturn(riderAppoint);
		
		Assumptions.assumeTrue(StringUtils.isNotBlank(appointmentDetailsDto.getRiderId()));
        String json = mapper.writeValueAsString(appointmentDetailsDto);

        MvcResult saveResponse = mockMvc.perform(MockMvcRequestBuilders.post(TRAINING_URL + "/appointment")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();

        assertEquals("The training has already been completed for the rider : "+rider.getRiderId(), saveResponse.getResolvedException().getMessage());
    }
    
    
    @Test
    @Order(8)
    void test_UpdateRiderTrainingAppointmentStatus() throws Exception {
        RiderProfile rider = profileRepository.save(riderProfile);
        appointmentDetailsDto.setRiderId(rider.getId());
        Mockito.when(operationFeignClient.updateOccupiedSlotSeats(Mockito.any())).thenReturn(true);
		Mockito.when(operationFeignClient.getTrainingSlotDetails(Mockito.any())).thenReturn(trainingDto);

		
		Mockito.when(appointmentRepository.findByRiderIdAndTrainingType(riderProfile.getId(), TrainingType.FOOD)).thenReturn(Optional.empty());
		
		RiderSelectedTrainingAppointment riderAppoint=RiderSelectedTrainingAppointment.builder()
				.appointmentId(appointmentDetailsDto.getAppointmentId())
				.venue(appointmentDetailsDto.getVenue())
				.status(RiderTrainingStatus.PENDING)
				.riderId(riderProfile.getRiderId()).build();
		
		Mockito.when(this.appointmentRepository.save(Mockito.any())).thenReturn(riderAppoint);
		
		Assumptions.assumeTrue(StringUtils.isNotBlank(appointmentDetailsDto.getRiderId()));
        String json = mapper.writeValueAsString(appointmentDetailsDto);

        MvcResult saveResponse = mockMvc.perform(MockMvcRequestBuilders.post(TRAINING_URL + "/appointment")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();
        RiderSelectedTrainingAppointment trainingAppointment = mapper.readValue(saveResponse.getResponse().getContentAsString(),
        		RiderSelectedTrainingAppointment.class);

        assertNotNull(trainingAppointment);
        assertEquals(appointmentDetailsDto.getAppointmentId(), trainingAppointment.getAppointmentId());
        assertEquals(appointmentDetailsDto.getVenue(), trainingAppointment.getVenue());
        assertEquals(RiderTrainingStatus.PENDING, trainingAppointment.getStatus());
        
    }
    
    @Test
    @Order(9)
    void test_getRidersByTrainingAppointmentIdWithEx() throws Exception {
    	TrainingDto trainingSlotDetails = new TrainingDto();
    	Mockito.when(operationFeignClient.getTrainingSlotDetails(Mockito.any())).thenReturn(trainingSlotDetails);
		
    	MvcResult appointmentStatusResponse = mockMvc.perform(MockMvcRequestBuilders.get(TRAINING_URL +"/appointment/"+appointmentDetailsDto.getAppointmentId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();

        assertEquals("No appointment booked for Slot ID : "+appointmentDetailsDto.getAppointmentId(), appointmentStatusResponse.getResolvedException().getMessage());
       

    }
    
    @Test
    @Order(10)
    void test_getRidersByTrainingAppointmentId() throws Exception {
    	 RiderProfile rider = profileRepository.save(riderProfile);
    	TrainingDto trainingSlotDetails = new TrainingDto();
    	trainingSlotDetails.setReserved(1);
    	Mockito.when(operationFeignClient.getTrainingSlotDetails(Mockito.any())).thenReturn(trainingSlotDetails);
    	RiderSelectedTrainingAppointment singleRiderAppoint=RiderSelectedTrainingAppointment.builder()
				.appointmentId(appointmentDetailsDto.getAppointmentId())
				.venue(appointmentDetailsDto.getVenue())
				.status(RiderTrainingStatus.PENDING)
				.riderId(riderProfile.getRiderId()).build();
    	List<RiderSelectedTrainingAppointment> riderAppoint=new ArrayList<>();
    	riderAppoint.add(singleRiderAppoint);
    	Mockito.when(appointmentRepository.findByAppointmentId(appointmentDetailsDto.getAppointmentId())).thenReturn(riderAppoint);

    	MvcResult appointmentStatusResponse = mockMvc.perform(MockMvcRequestBuilders.get(TRAINING_URL +"/appointment/"+appointmentDetailsDto.getAppointmentId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();

        assertEquals(HttpStatus.OK.value(), appointmentStatusResponse.getResponse().getStatus());


    }
    @Test
    @Order(11)
    void test_getRidersByTrainingAppointmentIdNotFound() throws Exception {
  
    	Mockito.when(operationFeignClient.getTrainingSlotDetails(Mockito.any())).thenThrow(AppointmentIdNotFoundException.class);
		
    	MvcResult appointmentStatusResponse = mockMvc.perform(MockMvcRequestBuilders.get(TRAINING_URL +"/appointment/"+appointmentDetailsDto.getAppointmentId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();

        assertEquals("Training Slot Not Found with Slot ID : "+appointmentDetailsDto.getAppointmentId(), appointmentStatusResponse.getResolvedException().getMessage());
       

    }
}

