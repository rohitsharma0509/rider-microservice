package com.scb.rider.IntegrationTest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import com.scb.rider.client.BroadCastServiceFeignClient;
import com.scb.rider.client.JobServiceFeignClient;
import com.scb.rider.constants.Constants;
import com.scb.rider.exception.FileConversionException;
import com.scb.rider.exception.InvalidImageExtensionException;
import com.scb.rider.kafka.KafkaPublisher;
import com.scb.rider.model.RiderJobStatusEventModel;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.BroadcastJobResponse;
import com.scb.rider.model.dto.JobDetails;
import com.scb.rider.model.dto.RiderJobAcceptedDetails;
import com.scb.rider.model.enumeration.RiderJobStatus;
import com.scb.rider.service.AmazonS3ImageService;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)

public class RiderJobDetailsControllerIntegrationTest extends AbstractRestApiIntegrationTest {

	static final String URL = "/profile/";

	@MockBean
	private KafkaPublisher kafkaPublisher;
	@MockBean
	private KafkaTemplate<String, RiderJobStatusEventModel> kafkaTemplate;
	@MockBean
	private AmazonS3ImageService amazonS3ImageService;
	@Mock
	private BroadCastServiceFeignClient broadCastServiceFeignClient;
	@Mock
	private JobServiceFeignClient jobServiceFeignClient;

	@Before
	public void setUp() throws ExecutionException, InterruptedException {
		doNothing().when(kafkaPublisher).publish(any(RiderJobStatusEventModel.class));
		doNothing().when(kafkaTemplate).send(any(), any(), any());
	}

	@Test
	public void testCreateRiderJobDetailsRequestSuccess() throws Exception {

		// prepare data and mock's behaviour
		RiderProfile riderProfile = createRiderProfileDb();
		RiderJobDetails riderJobDetail = createRiderJobDetailInDb(riderProfile.getId());
		MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png", MediaType.TEXT_PLAIN_VALUE,
				"Hello, World!".getBytes());
		// execute
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart(URL + riderProfile.getId() + "/job")
				// .file(mockMultipartFile)
				.param("jobStatus", "CALLED_MERCHANT").param("remark", "string")
				// .param("parkingFee", "12.8787")
				.param("jobId", riderJobDetail.getJobId()).contentType(MediaType.MULTIPART_FORM_DATA)
				.accept(MediaType.APPLICATION_JSON)).andDo(print()).andReturn();

		// verify
		int status = result.getResponse().getStatus();
		assertEquals(HttpStatus.CREATED.value(), status, "Incorrect Response Status");

		RiderJobAcceptedDetails riderJobDetails = objectMapper.readValue(result.getResponse().getContentAsString(),
				RiderJobAcceptedDetails.class);
		assertNotNull(riderJobDetails);
		assertNotNull(riderJobDetails.getId());
		assertEquals("9876543210", riderJobDetails.getJobId(), "Invalid Teacher Name");
	}

	@Test
	public void testCreateRiderJobDetailsRequestJob_AcceptedSuccessWithBad_Request() throws Exception {

		// prepare data and mock's behaviour
		RiderProfile riderProfile = createRiderProfileDb();
		BroadcastJobResponse broadcastJobResponse = BroadcastJobResponse.builder()
				.broadcastStatus(Constants.BROADCASTING).build();
		JobDetails jobDetails = JobDetails.builder().jobId("123").build();
		ResponseEntity<JobDetails> jobServiceResponse = ResponseEntity.ok(jobDetails);

		BroadCastServiceFeignClient broadCastServiceFeignClient = mock(BroadCastServiceFeignClient.class);
		JobServiceFeignClient jobServiceFeignClient = mock(JobServiceFeignClient.class);
		when(broadCastServiceFeignClient.getBroadcastData("123")).thenReturn(broadcastJobResponse);
		when(jobServiceFeignClient.getJobByJobId("123")).thenReturn(jobServiceResponse);
		// execute
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart(URL + riderProfile.getId() + "/job")
				// .file(mockMultipartFile)
				.param("jobStatus", "JOB_ACCEPTED").param("remark", "string")
				// .param("parkingFee", "12.8787")
				.param("jobId", "123")// riderJobDetail.getJobId()
				.contentType(MediaType.MULTIPART_FORM_DATA).accept(MediaType.APPLICATION_JSON)).andDo(print())
				.andReturn();

		// verify
		int status = result.getResponse().getStatus();
		assertEquals(HttpStatus.BAD_REQUEST.value(), status, "Incorrect Response Status");
	}

	@Test
	public void testCreateRiderJobDetailsRequestARRIVED_AT_MERCHANTSuccess() throws Exception {

		// prepare data and mock's behaviour
		RiderProfile riderProfile = createRiderProfileDb();
		RiderJobDetails riderJobDetail = createRiderJobDetailInDb(riderProfile.getId(), RiderJobStatus.CALLED_MERCHANT);
		MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png", MediaType.TEXT_PLAIN_VALUE,
				"Hello, World!".getBytes());
		// execute
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart(URL + riderProfile.getId() + "/job")
				// .file(mockMultipartFile)
				.param("jobStatus", "ARRIVED_AT_MERCHANT").param("remark", "string")
				// .param("parkingFee", "12.8787")
				.param("jobId", riderJobDetail.getJobId()).contentType(MediaType.MULTIPART_FORM_DATA)
				.accept(MediaType.APPLICATION_JSON)).andDo(print()).andReturn();

		// verify
		int status = result.getResponse().getStatus();
		assertEquals(HttpStatus.CREATED.value(), status, "Incorrect Response Status");

		RiderJobAcceptedDetails riderJobDetails = objectMapper.readValue(result.getResponse().getContentAsString(),
				RiderJobAcceptedDetails.class);
		assertNotNull(riderJobDetails);
		assertNotNull(riderJobDetails.getId());
		assertEquals("9876543210", riderJobDetails.getJobId(), "Invalid Teacher Name");
	}

	@Test
	public void testCreateRiderJobDetailsRequestMEAL_PICKED_UPSuccess()
			throws Exception, InvalidImageExtensionException, FileConversionException {

		// prepare data and mock's behaviour
		RiderProfile riderProfile = createRiderProfileDb();
		RiderJobDetails riderJobDetail = createRiderJobDetailInDb(riderProfile.getId(),
				RiderJobStatus.ARRIVED_AT_MERCHANT);
		riderJobDetail.setArrivedAtMerchantTime(LocalDateTime.now());
		riderJobDetailsRepository.save(riderJobDetail);
		MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png", MediaType.TEXT_PLAIN_VALUE,
				"Hello, World!".getBytes());
		// execute
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart(URL + riderProfile.getId() + "/job")
				.file(mockMultipartFile).param("jobStatus", "MEAL_PICKED_UP").param("remark", "string")
				// .param("parkingFee", "12.8787")
				.param("jobId", riderJobDetail.getJobId()).contentType(MediaType.MULTIPART_FORM_DATA)
				.accept(MediaType.APPLICATION_JSON)).andDo(print()).andReturn();
		when(amazonS3ImageService.uploadMultipartFile(any(MultipartFile.class), any(), any())).thenReturn("imageUrl");

		// verify
		int status = result.getResponse().getStatus();
		assertEquals(HttpStatus.CREATED.value(), status, "Incorrect Response Status");

		RiderJobAcceptedDetails riderJobDetails = objectMapper.readValue(result.getResponse().getContentAsString(),
				RiderJobAcceptedDetails.class);
		assertNotNull(riderJobDetails);
		assertNotNull(riderJobDetails.getId());
		assertEquals("9876543210", riderJobDetails.getJobId(), "Invalid Teacher Name");
	}

	@Test
	public void testCreateRiderJobDetailsRequestPARKING_RECEIPT_PHOTOSuccess()
			throws Exception, InvalidImageExtensionException, FileConversionException {

		// prepare data and mock's behaviour
		RiderProfile riderProfile = createRiderProfileDb();
		RiderJobDetails riderJobDetail = createRiderJobDetailInDb(riderProfile.getId());
		MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png", MediaType.TEXT_PLAIN_VALUE,
				"Hello, World!".getBytes());
		when(amazonS3ImageService.uploadMultipartFile(any(MultipartFile.class), any(), any())).thenReturn("imageUrl");

		// execute
		MvcResult result = mockMvc
				.perform(MockMvcRequestBuilders.multipart(URL + riderProfile.getId() + "/job").file(mockMultipartFile)
						.param("jobStatus", "PARKING_RECEIPT_PHOTO").param("remark", "string")
						.param("parkingFee", "12.8787").param("jobId", riderJobDetail.getJobId())
						.contentType(MediaType.MULTIPART_FORM_DATA).accept(MediaType.APPLICATION_JSON))
				.andDo(print()).andReturn();

		// verify
		int status = result.getResponse().getStatus();
		assertEquals(HttpStatus.CREATED.value(), status, "Incorrect Response Status");

		RiderJobAcceptedDetails riderJobDetails = objectMapper.readValue(result.getResponse().getContentAsString(),
				RiderJobAcceptedDetails.class);
		assertNotNull(riderJobDetails);
		assertNotNull(riderJobDetails.getId());
		assertEquals("9876543210", riderJobDetails.getJobId(), "Invalid Teacher Name");
	}

	@Test
	public void testCreateRiderJobDetailsRequestARRIVED_AT_CUST_LOCATIONSuccess() throws Exception {

		// prepare data and mock's behaviour
		RiderProfile riderProfile = createRiderProfileDb();
		RiderJobDetails riderJobDetail = createRiderJobDetailInDb(riderProfile.getId(), RiderJobStatus.MEAL_PICKED_UP);
		MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png", MediaType.TEXT_PLAIN_VALUE,
				"Hello, World!".getBytes());
		// execute
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart(URL + riderProfile.getId() + "/job")
				// .file(mockMultipartFile)
				.param("jobStatus", "ARRIVED_AT_CUST_LOCATION").param("remark", "string")
				// .param("parkingFee", "12.8787")
				.param("jobId", riderJobDetail.getJobId()).contentType(MediaType.MULTIPART_FORM_DATA)
				.accept(MediaType.APPLICATION_JSON)).andDo(print()).andReturn();

		// verify
		int status = result.getResponse().getStatus();
		assertEquals(HttpStatus.CREATED.value(), status, "Incorrect Response Status");

		RiderJobAcceptedDetails riderJobDetails = objectMapper.readValue(result.getResponse().getContentAsString(),
				RiderJobAcceptedDetails.class);
		assertNotNull(riderJobDetails);
		assertNotNull(riderJobDetails.getId());
		assertEquals("9876543210", riderJobDetails.getJobId(), "Invalid Teacher Name");
	}

	@Test
	public void testCreateRiderJobDetailsRequestFOOD_DELIVEREDSuccess() throws Exception {

		// prepare data and mock's behaviour
		RiderProfile riderProfile = createRiderProfileDb();
		RiderJobDetails riderJobDetail = createRiderJobDetailInDb(riderProfile.getId(),
				RiderJobStatus.ARRIVED_AT_CUST_LOCATION);
		MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png", MediaType.TEXT_PLAIN_VALUE,
				"Hello, World!".getBytes());
		// execute
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart(URL + riderProfile.getId() + "/job")
				// .file(mockMultipartFile)
				.param("jobStatus", "FOOD_DELIVERED").param("remark", "string")
				// .param("parkingFee", "12.8787")
				.param("jobId", riderJobDetail.getJobId()).contentType(MediaType.MULTIPART_FORM_DATA)
				.accept(MediaType.APPLICATION_JSON)).andDo(print()).andReturn();

		// verify
		int status = result.getResponse().getStatus();
		assertEquals(HttpStatus.CREATED.value(), status, "Incorrect Response Status");

		RiderJobAcceptedDetails riderJobDetails = objectMapper.readValue(result.getResponse().getContentAsString(),
				RiderJobAcceptedDetails.class);
		assertNotNull(riderJobDetails);
		assertNotNull(riderJobDetails.getId());
		assertEquals("9876543210", riderJobDetails.getJobId(), "Invalid Teacher Name");
	}

	@Test
	public void testCreateRiderJobDetailsRequestORDER_CANCELED_BY_OPERATIONSuccess() throws Exception {

		// prepare data and mock's behaviour
		RiderProfile riderProfile = createRiderProfileDb();
		RiderJobDetails riderJobDetail = createRiderJobDetailInDb(riderProfile.getId());
		MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png", MediaType.TEXT_PLAIN_VALUE,
				"Hello, World!".getBytes());
		// execute
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart(URL + riderProfile.getId() + "/job")
				// .file(mockMultipartFile)
				.param("jobStatus", "ORDER_CANCELLED_BY_OPERATOR").param("remark", "string")
				// .param("parkingFee", "12.8787")
				.param("jobId", riderJobDetail.getJobId()).contentType(MediaType.MULTIPART_FORM_DATA)
				.accept(MediaType.APPLICATION_JSON)).andDo(print()).andReturn();

		// verify
		int status = result.getResponse().getStatus();
		assertEquals(HttpStatus.CREATED.value(), status, "Incorrect Response Status");

		RiderJobAcceptedDetails riderJobDetails = objectMapper.readValue(result.getResponse().getContentAsString(),
				RiderJobAcceptedDetails.class);
		assertNotNull(riderJobDetails);
		assertNotNull(riderJobDetails.getId());
		assertEquals("9876543210", riderJobDetails.getJobId(), "Invalid Teacher Name");
	}
}
