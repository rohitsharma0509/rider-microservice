package com.scb.rider.util;

import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.RiderProfileDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
class CustomBeanUtilsTest {

    @BeforeAll
    static void setup(){

    }

    @Test
    public void testCopyProperties(){
        RiderProfileDto riderProfileDto = RiderProfileDto.builder()
                .accountNumber("12345678912345").build();
        RiderProfile riderProfile = new RiderProfile();
        riderProfile.setPhoneNumber("9999999999");
        CustomBeanUtils.copyNonNullProperties(riderProfileDto, riderProfile);
        assertNotNull(riderProfile.getPhoneNumber());
        assertNotNull(riderProfile.getAccountNumber());
    }

}
