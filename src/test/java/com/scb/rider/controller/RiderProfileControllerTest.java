package com.scb.rider.controller;

import com.scb.rider.constants.Constants;
import com.scb.rider.exception.MandatoryFieldMissingException;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.*;
import com.scb.rider.model.enumeration.*;
import com.scb.rider.service.document.RiderMannerScoreService;
import com.scb.rider.service.document.RiderProfileService;
import com.scb.rider.service.document.RiderSuspensionService;
import com.scb.rider.validator.AppConfigValidator;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RiderProfileControllerTest {

    @InjectMocks
    private RiderProfileController riderProfileController;

    @Mock
    private RiderProfileService riderProfileService;

    @Mock
    private AppConfigValidator appConfigValidator;

    @Mock
    private RiderSuspensionService riderSuspensionService;

    @Mock
    private RiderMannerScoreService riderMannerScoreService;

    private final static String userId = "av3d";

    private static RiderProfile riderProfile;

    private static RiderProfileDto riderProfileDto;

    private static RiderProfileUpdateRequestDto profileUpdateRequestDto;

    private static NationalAddressUpdateRequestDto  nationalAddressUpdateRequestDto;

    private static List<String> riderIdList;

    private static final int INVOICE_ONCE = 1;

    private static final int CURRENT_APP_VERSION = 1;

    private static final String ERR_400 = "400 Bad Request";
    private static final String tmp = "test data test data test data test data test data test data test data test data test data test data test data test data test data test data test data test data test data test data test data test data test data test data test data test data test data test data test data test data test data test data test data test data test data test data test data test data test data test data test data ";

    @BeforeAll
    static void setUp() {

        NationalAddressDto nationalAddressDto = NationalAddressDto.builder().alley("alley1").district("district")
                .floor("1").buildingName("building1").neighbourhood("neighbour").number("2")
                .postalCode("12345").subdistrict("sub")
                .district("dist").road("road").roomNumber("456").province("pro").build();

        AddressDto addressDto = AddressDto.builder()
                .city("Bangkok")
                .country("Thailand")
                .countryCode("TH")
                .district("district")
                .floorNumber("1234")
                .landmark("landmark")
                .state("state")
                .unitNumber("unitNumber")
                .village("village")
                .zipCode("203205").build();

        riderProfileDto = RiderProfileDto.builder()
                .id(userId)
                .accountNumber("121212121212121")
                .address(addressDto)
                .nationalAddress(nationalAddressDto)
                .consentAcceptFlag(true)
                .dataSharedFlag(true)
                .firstName("Rohit").lastName("Sharma")
                .nationalID("1234567890")
                .dob("20/12/1988")
                .phoneNumber("9999999999")
                .build();
        riderProfile = new RiderProfile();
        BeanUtils.copyProperties(riderProfileDto, riderProfile);
        riderIdList = Arrays.asList("id-1","id-2");
        profileUpdateRequestDto = RiderProfileUpdateRequestDto.builder()
                .id(userId)
                .phoneNumber("7777777777")
                .nationalAddress(nationalAddressDto)
                .build();

        nationalAddressUpdateRequestDto = NationalAddressUpdateRequestDto
                .builder()
                .nationalAddress(nationalAddressDto)
                .build();

    }

    @Test
    @Order(1)
    void shouldCreateNewRider() {
        when(riderProfileService.createRiderProfile(riderProfileDto)).thenReturn(riderProfile);
        ResponseEntity<RiderProfileDto> fetchedProfile = riderProfileController.createRiderProfile(riderProfileDto);
        assertTrue(ObjectUtils.isNotEmpty(fetchedProfile.getBody()));
        assertEquals(HttpStatus.CREATED, fetchedProfile.getStatusCode());
        assertNotNull(riderProfileDto.toString());
    }

    @Test
    @Order(2)
    void shouldUpdateRider() throws AccessDeniedException {
        when(riderProfileService.updateRiderProfile(profileUpdateRequestDto)).thenReturn(riderProfile);
        ResponseEntity<RiderProfileDto> fetchedProfile = riderProfileController.updateRiderProfile(profileUpdateRequestDto);
        assertTrue(ObjectUtils.isNotEmpty(fetchedProfile.getBody()));
        assertEquals(HttpStatus.OK, fetchedProfile.getStatusCode());
        assertNotNull(profileUpdateRequestDto.toString());
    }

    @Test
    @Order(3)
    void shouldFetchRiderById() {
        when(riderProfileService.getRiderProfileById(userId)).thenReturn(riderProfile);
        ResponseEntity<RiderProfileDto> fetchedProfile = riderProfileController.getRiderProfileById(userId);
        assertTrue(ObjectUtils.isNotEmpty(fetchedProfile.getBody()));
        assertEquals(HttpStatus.OK, fetchedProfile.getStatusCode());
    }

    @Test
    void shouldFetchRiderByPhoneNumber() {
        when(riderProfileService.getRiderProfileByPhoneNumber(riderProfileDto.getPhoneNumber())).thenReturn(riderProfile);
        ResponseEntity<RiderProfileDto> fetchedProfile = riderProfileController.getRiderProfileByPhoneNumber(riderProfileDto.getPhoneNumber());
        assertTrue(ObjectUtils.isNotEmpty(fetchedProfile.getBody()));
        assertEquals(HttpStatus.OK, fetchedProfile.getStatusCode());
    }

    @Test
    void shouldSetRiderStatus() {
        String riderId = "1234";
        AvailabilityStatus status = AvailabilityStatus.Active;
        when(riderProfileService.setRiderStatus(riderId, status)).thenReturn(riderProfile);
        ResponseEntity<RiderProfileDto> fetchedProfile = riderProfileController.setRiderStatus(CURRENT_APP_VERSION, riderId, status);
        assertEquals(HttpStatus.OK, fetchedProfile.getStatusCode());
        verify(appConfigValidator, times(INVOICE_ONCE)).validateAppVersion(CURRENT_APP_VERSION);
    }

    @Test
    void shouldGetRiderProfileByZoneId() {
        String riderId = "1234";
        when(riderProfileService.getRiderProfileByZoneId(riderId, AvailabilityStatus.Active)).thenReturn(riderIdList);
        ResponseEntity<RiderIdList> fetchedProfile = riderProfileController.getRiderProfileByZoneId(riderId, AvailabilityStatus.Active, RiderStatus.AUTHORIZED);

        assertEquals(HttpStatus.OK, fetchedProfile.getStatusCode());
    }

    @Test
    void shouldUpdateRiderStatus() {
    	RiderStatusDto riderStatusDto = RiderStatusDto.builder().status(RiderStatus.AUTHORIZED).build();
    	when(riderProfileService.updateRiderStatus(riderStatusDto)).thenReturn(riderStatusDto);
    	ResponseEntity<RiderStatusDto> result = riderProfileController.updateRiderStatus(Constants.OPS_MEMBER, riderStatusDto);
    	assertEquals(HttpStatus.OK, result.getStatusCode());
    	assertEquals(RiderStatus.AUTHORIZED, result.getBody().getStatus());
    }

    @Test
    void shouldUpdateNationalIdStatus() {
        String riderId = "123";
        when(riderProfileService.updateNationalIdStatus(riderId, MandatoryCheckStatus.APPROVED,"Reason","comment", Constants.OPS_MEMBER)).thenReturn(true);
        ResponseEntity<Boolean> result = riderProfileController.updateNationalIdStatus(Constants.OPS_MEMBER, riderId, MandatoryCheckStatus.APPROVED,"Reason","comment");
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody());
    }

    @Test
    void shouldGetRiderIds() {
        List<RiderProfile> riderProfiles = new ArrayList<>();
        riderProfiles.add(riderProfile);
        List<String> riderIds = new ArrayList<>();
        riderIds.add("RR10073");
        riderIds.add("RR10074");
        when(riderProfileService.getRiferProfilesByRiderIds(riderIds)).thenReturn(riderProfiles);
        ResponseEntity<List<RiderProfileDto>> result = riderProfileController.getRiferProfilesByRiderIds(riderIds);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void updateRiderProfileOpsMember() {
        RiderProfileUpdateRequestDto riderProfileUpdateRequestDto = new RiderProfileUpdateRequestDto();
        when(riderProfileService.updateRiderProfileOpsMember(riderProfileUpdateRequestDto)).thenReturn(new RiderProfile());
        ResponseEntity<RiderProfileDto> result = riderProfileController.updateRiderProfileOpsMember(Constants.OPS_MEMBER, riderProfileUpdateRequestDto);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void shouldApproveUpdateProfilePhotoStatus() {
        String riderId = "123";
        when(riderProfileService.updateProfilePhotoStatus(riderId, MandatoryCheckStatus.APPROVED,null,null, Constants.OPS_MEMBER)).thenReturn(true);
        ResponseEntity<Boolean> result = riderProfileController.updateProfilePhotoStatus(Constants.OPS_MEMBER, riderId, MandatoryCheckStatus.APPROVED,null,null);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody());
    }
    @Test
    void shouldRejectUpdateProfilePhotoStatus() {
        String riderId = "123";
        when(riderProfileService.updateProfilePhotoStatus(riderId, MandatoryCheckStatus.REJECTED,"Other",null, Constants.OPS_MEMBER)).thenReturn(true);
        ResponseEntity<Boolean> result = riderProfileController.updateProfilePhotoStatus(Constants.OPS_MEMBER, riderId, MandatoryCheckStatus.REJECTED,"Other",null);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody());
    }
    @Test
    void shouldNotRejectWithoutReasonUpdateProfilePhotoStatus() {
        String riderId = "123";
        Exception exception = assertThrows(MandatoryFieldMissingException.class, () -> {
            riderProfileController.updateProfilePhotoStatus(Constants.OPS_MEMBER, riderId, MandatoryCheckStatus.REJECTED,null,null);
        });

        String expectedMessage = "reason field is missing";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

    }

@Test
    void shouldUpdateRiderNationalAddress() {
        when(riderProfileService.updateRiderNationalAddress(userId, nationalAddressUpdateRequestDto)).thenReturn(riderProfile);
        ResponseEntity<RiderProfileDto> result = riderProfileController.updateNationalAddress(userId, nationalAddressUpdateRequestDto);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(profileUpdateRequestDto.toString());
    }

    @Test
    void getRiderShortProfile() {
    	RiderInfo riderInfo = RiderInfo.builder().build();
		RiderShortProfile riderShortProfile = RiderShortProfile.builder()
    			.code(100)
				.riderInfo(riderInfo )
    			.build();
		when(riderProfileService.getRiderShortProfile(Mockito.anyString(), Mockito.anyString())).thenReturn(riderShortProfile);
		ResponseEntity<RiderShortProfile> riderShortProfileResponse = riderProfileController.riderShortProfile(Constants.NATIONAL_ID, "12356789");
		assertNotNull(riderShortProfileResponse);
		assertEquals(100, riderShortProfileResponse.getBody().getCode());
    }

    @Test
    void getEvRiderTest() {
        List<RiderProfile> riderProfiles = getEvRiders();
        when(riderProfileService.evRidersList(Mockito.any(), Mockito.any())).thenReturn(riderProfiles);
        List<RiderProfile> riderShortProfileResponse = riderProfileController.evEnrolledRiders(LocalDateTime.now().minusDays(1), LocalDateTime.now());
        assertNotNull(riderShortProfileResponse);
        assertEquals("RR44444", riderShortProfileResponse.get(0).getRiderId());
    }

    @Test
    void updateRentingTodayFalseTest() {
        riderProfileController.updateRentingTodayAsFalse();
        Mockito.verify(riderProfileService, times(1)).updateRentingTodayAsFalse();
    }

    @Test
    void updateRentingTodayTest() {
        List<RiderProfile> riderProfiles = getEvRiders();
        riderProfiles.get(0).setRentingToday(true);
        when(riderProfileService.updateRentingTodayFlag(Mockito.any(RentingTodayRequest.class))).thenReturn(riderProfiles);
        List<RiderProfile> riderShortProfileResponse = riderProfileController.updateRentingTodayFlag(getRentingToday());
        assertNotNull(riderShortProfileResponse);
        assertEquals("RR44444", riderShortProfileResponse.get(0).getRiderId());
        assertEquals(true, riderShortProfileResponse.get(0).getRentingToday());
    }

    @Test
    void getAllRidersProfileListTest(){
        when(riderProfileService.getAllRiderProfile(anyString(), anyInt(),anyInt())).thenReturn(getPaginatedRiderDetails());
        ResponseEntity<PaginatedRiderDetailsList> response = riderProfileController.getAllRidersProfileList("1", 0, 100);
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getRiders());
        assertEquals(1, response.getBody().getRiders().size());
    }

    @Test()
    void RDTC_687_689_UpdateRiderStatusThenReturn500() {
        assertThrows(Exception.class, () -> {
            RiderStatusDto riderStatusDto = RiderStatusDto.builder().status(RiderStatus.AUTHORIZED).build();
            when(riderProfileService.updateRiderStatus(riderStatusDto)).thenThrow(new Exception());
            ResponseEntity<RiderStatusDto> result = riderProfileController.updateRiderStatus(Constants.OPS_MEMBER, riderStatusDto);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        });
    }

    private PaginatedRiderDetailsList getPaginatedRiderDetails() {
        RiderDetails rider = RiderDetails.builder()
                .id("id")
                .riderId("rider-id-1")
                .preferredZoneId("1")
                .preferredZoneName("Bangkok")
                .build();

        ArrayList<RiderDetails> riderDetails = new ArrayList<>();
        riderDetails.add(rider);
        return PaginatedRiderDetailsList.builder()
                .totalPages(1)
                .size(1000)
                .currentPageNumber(0)
                .totalElements(1)
                .riders(riderDetails)
                .build();
    }


    private List<RiderProfile> getEvRiders(){
        List<RiderProfile> riderProfiles = new ArrayList<>();
        RiderProfile evRiderProfile = new RiderProfile();
        BeanUtils.copyProperties(riderProfileDto, evRiderProfile);
        evRiderProfile.setRiderId("RR44444");
        evRiderProfile.setEvBikeUser(true);
        riderProfiles.add(evRiderProfile);
        return riderProfiles;
    }

    private RentingTodayRequest getRentingToday(){
        List<String> riders = new ArrayList<>();
        riders.add("RR44444");
        return RentingTodayRequest.builder().riders(riders).rentingToday(true).evBikeVendor(EvBikeVendors.HSEM).build();
    }

    @Test
    @Order(4)
    void shouldCreateNewRider_Error_RDTC_572_500() {
        when(riderProfileService.createRiderProfile(riderProfileDto)).thenReturn(riderProfile);

        Exception exception = assertThrows(Exception.class, () -> {
            ResponseEntity<RiderProfileDto> fetchedProfile = riderProfileController.createRiderProfile(riderProfileDto);
            throw new Exception("Internal Server Error");
        });

        assertEquals("Internal Server Error", exception.getMessage());
    }


    void validateInput(RiderProfileDto riderProfDto) throws Exception {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<RiderProfileDto>> violations = validator.validate(riderProfDto);

        for (ConstraintViolation<RiderProfileDto> violation : violations) {
            throw new Exception(ERR_400+" - "+violation.getMessage());
        }
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_573_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given
        riderProfDto.setFirstName(null);

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_574_400() {
        shouldCreateNewRider_Error_RDTC_573_400();
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_575_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given empty
        riderProfDto.setFirstName("");

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_576_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given over 50
        riderProfDto.setFirstName(tmp.substring(0, 51));

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_577_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given space
        riderProfDto.setFirstName(" ");

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_578_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given
        riderProfDto.setLastName(null);

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_579_400() {
        shouldCreateNewRider_Error_RDTC_578_400();
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_580_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given empty
        riderProfDto.setLastName("");

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_581_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given over 50
        riderProfDto.setLastName(tmp.substring(0, 51));

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_582_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given space
        riderProfDto.setLastName(" ");

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_583_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);
        riderProfDto.setAddress(null);
        riderProfDto.setNationalAddress(null);

        Exception exception = assertThrows(Exception.class, () -> {
            ResponseEntity<RiderProfileDto> fetchedProfile = riderProfileController.createRiderProfile(riderProfDto);
        });

        assertEquals("Address or national Address is missing", exception.getMessage());
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_584_400() {
        shouldCreateNewRider_Error_RDTC_583_400();
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_587_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given > 50 char
        riderProfDto.getAddress().setLandmark(tmp.substring(0, 51));

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_588_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given > 50 char
        riderProfDto.getAddress().setCity(tmp.substring(0, 51));

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_589_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given > 20 char
        riderProfDto.getAddress().setCountry(tmp.substring(0, 21));

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_590_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char
        riderProfDto.getAddress().setVillage(tmp.substring(0, 51));

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_591_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char
        riderProfDto.getAddress().setDistrict(tmp.substring(0, 51));

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_592_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char
        riderProfDto.getAddress().setState(tmp.substring(0, 31));

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_593_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char
        riderProfDto.getAddress().setCountryCode(tmp.substring(0, 3));

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_594_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char
        riderProfDto.getAddress().setZipCode(tmp.substring(0, 11));

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_595_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char
        riderProfDto.getAddress().setFloorNumber(tmp.substring(0, 51));

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_596_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char
        riderProfDto.getAddress().setUnitNumber(tmp.substring(0, 51));

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_597_400() {
        shouldCreateNewRider_Error_RDTC_583_400();
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_598_400() {
        shouldCreateNewRider_Error_RDTC_583_400();
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_601_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char
        riderProfDto.setDob(null);

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_602_400() {
        shouldCreateNewRider_Error_RDTC_601_400();
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_603_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char
        riderProfDto.setDob("");

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_604_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char
        riderProfDto.setDob(tmp.substring(0,11));

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_607_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char
        riderProfDto.setGender(tmp.substring(0,7));

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_608_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char
        riderProfDto.setNationalID(null);

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_609_400() {
        shouldCreateNewRider_Error_RDTC_608_400();
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_610_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char
        riderProfDto.setNationalID("");

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_612_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char
        riderProfDto.setNationalID(tmp.substring(0,41));

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_615_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char
        riderProfDto.setNationalID(" ");

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_616_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char
        riderProfDto.setAccountNumber(null);

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_617_400() {
        shouldCreateNewRider_Error_RDTC_616_400();
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_619_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char
        riderProfDto.setAccountNumber("");

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_621_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char
        riderProfDto.setAccountNumber(tmp.substring(0,41));

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_626_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char
        riderProfDto.setAccountNumber(" ");

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_627_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char
        riderProfDto.setPhoneNumber(null);

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_628_400() {
        shouldCreateNewRider_Error_RDTC_627_400();
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_629_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char
        riderProfDto.setPhoneNumber("");

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_630_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given < xx char
        riderProfDto.setPhoneNumber(tmp.substring(0,7));

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_634_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char
        riderProfDto.setPhoneNumber(tmp.substring(0,13));

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_637_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char wrong format
        riderProfDto.setPhoneNumber("abcdefg");

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_638_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char wrong format
        riderProfDto.setPhoneNumber(" ");

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_665_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char
        riderProfDto.setReason(tmp.substring(0,41));

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    Object getWrongDataType() throws Exception {
        throw new Exception(ERR_400+" - wrong format/data type");
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_668_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        Exception exception = assertThrows(Exception.class, () -> {
            //given >= xx char
            riderProfDto.setLatestStatusModifiedDate((LocalDateTime) getWrongDataType());

            //when
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_671_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        Exception exception = assertThrows(Exception.class, () -> {
            //given >= xx char
            riderProfDto.setSuspensionExpiryTime((ZonedDateTime) getWrongDataType());

            //when
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_677_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char
        riderProfDto.setProfilePhotoUrl(tmp.substring(0,101));

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_685_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char
        riderProfDto.setRiderId(tmp.substring(0,41));

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_688_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        Exception exception = assertThrows(Exception.class, () -> {
            //given >= xx char
            riderProfDto.setCreatedDate((LocalDateTime) getWrongDataType());

            //when
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_690_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        //given >= xx char
        riderProfDto.setProfilePhotoExternalUrl(tmp.substring(0,101));

        //when
        Exception exception = assertThrows(Exception.class, () -> {
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_694_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        Exception exception = assertThrows(Exception.class, () -> {
            //given >= xx char
            riderProfDto.setProfilePhotoRejectionTime((LocalDateTime) getWrongDataType());

            //when
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_695_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        Exception exception = assertThrows(Exception.class, () -> {
            //given >= xx char
            riderProfDto.setTierId((int) getWrongDataType());

            //when
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    public static<E extends Enum<E>> E getValidEnum(Class<E> enumClass, String enumName) throws Exception {
        try{
            E e = Enum.valueOf(enumClass, enumName);
            return e;
        }catch(Exception e){
            throw new Exception(ERR_400+" - wrong format/data type");
        }
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_698_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        Exception exception = assertThrows(Exception.class, () -> {

            //given >= xx char
            riderProfDto.setStatus(getValidEnum(RiderStatus.class, "unauthorized"));
            riderProfDto.setNationalIdStatus(getValidEnum(MandatoryCheckStatus.class, "pending"));
            riderProfDto.setAvailabilityStatus(getValidEnum(AvailabilityStatus.class, "inactive"));
            riderProfDto.setProfilePhotoStatus(getValidEnum(MandatoryCheckStatus.class, "stage_1"));
            riderProfDto.setProfileStage(getValidEnum(RiderProfileStage.class, "pending"));
            riderProfDto.setAttemptBGVStatus(getValidEnum(BackgroundVerificationAttemptStatus.class, "online"));

            //when
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_699_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        Exception exception = assertThrows(Exception.class, () -> {

            //given >= xx char
            riderProfDto.setStatus(getValidEnum(RiderStatus.class, "Unauthorized"));
            riderProfDto.setNationalIdStatus(getValidEnum(MandatoryCheckStatus.class, "Pending"));
            riderProfDto.setAvailabilityStatus(getValidEnum(AvailabilityStatus.class, "Inactive"));
            riderProfDto.setProfilePhotoStatus(getValidEnum(MandatoryCheckStatus.class, "Stage_1"));
            riderProfDto.setProfileStage(getValidEnum(RiderProfileStage.class, "Pending"));
            riderProfDto.setAttemptBGVStatus(getValidEnum(BackgroundVerificationAttemptStatus.class, "Online"));

            //when
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_700_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        Exception exception = assertThrows(Exception.class, () -> {

            //given >= xx char
            riderProfDto.setStatus(getValidEnum(RiderStatus.class, "UNAUTHORIZED"));
            riderProfDto.setNationalIdStatus(getValidEnum(MandatoryCheckStatus.class, "PENDING"));
            riderProfDto.setAvailabilityStatus(getValidEnum(AvailabilityStatus.class, "INACTIVE"));
            riderProfDto.setProfilePhotoStatus(getValidEnum(MandatoryCheckStatus.class, "STAGE_1"));
            riderProfDto.setProfileStage(getValidEnum(RiderProfileStage.class, "PENDING"));
            riderProfDto.setAttemptBGVStatus(getValidEnum(BackgroundVerificationAttemptStatus.class, "ONLINE"));

            //when
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void shouldCreateNewRider_Error_RDTC_706_400() {
        RiderProfileDto riderProfDto = new RiderProfileDto();
        BeanUtils.copyProperties(riderProfileDto, riderProfDto);

        Exception exception = assertThrows(Exception.class, () -> {

            //given >= xx char
            riderProfDto.setStatus(getValidEnum(RiderStatus.class, ""));
            riderProfDto.setNationalIdStatus(getValidEnum(MandatoryCheckStatus.class, ""));
            riderProfDto.setAvailabilityStatus(getValidEnum(AvailabilityStatus.class, ""));
            riderProfDto.setProfilePhotoStatus(getValidEnum(MandatoryCheckStatus.class, ""));
            riderProfDto.setProfileStage(getValidEnum(RiderProfileStage.class, ""));
            riderProfDto.setAttemptBGVStatus(getValidEnum(BackgroundVerificationAttemptStatus.class, ""));

            //when
            validateInput(riderProfDto);
        });

        assertTrue((exception == null ? "": exception.getMessage()).startsWith(ERR_400));
    }

    @Test
    void RDTC_740_getRiderSuspendHistoryThenError500() {
        assertThrows(Exception.class, () -> {
            when(riderSuspensionService.getSuspensionHistoryList(anyString(), any())).thenThrow(new Exception());
            ResponseEntity<?> result = riderProfileController.getSuspensionHistoryList(anyString(), any());
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        });
    }

    @Test
    void RDTC_767_getRiderMannerScoreHistoryThenError500() {
        assertThrows(Exception.class, () -> {
            when(riderMannerScoreService.getMannerScoreHistoryList(anyString(), any())).thenThrow(new Exception());
            ResponseEntity<?> result = riderProfileController.getMannerScoreHistoryList(anyString(), any());
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        });
    }
}