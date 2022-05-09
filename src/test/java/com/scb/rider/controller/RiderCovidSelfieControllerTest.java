package com.scb.rider.controller;

import com.scb.rider.exception.FileConversionException;
import com.scb.rider.exception.InvalidImageExtensionException;
import com.scb.rider.model.document.RiderCovidSelfie;
import com.scb.rider.model.dto.RiderCovidSelfieData;
import com.scb.rider.model.dto.RiderCovidSelfieDataList;
import com.scb.rider.service.document.RiderCovidSelfieService;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderCovidSelfieControllerTest {

    @InjectMocks
    private RiderCovidSelfieController riderCovidSelfieController;

    @Mock
    private RiderCovidSelfieService service;


    @Test
    public void testCovidSelfieFileUploadTest() throws InvalidImageExtensionException, FileConversionException {

        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png",
                MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());

        LocalDateTime localDateTime = LocalDateTime.now();

        RiderCovidSelfie riderCovidSelfie =
                RiderCovidSelfie.builder().id("primaryKey").riderId("12345").fileName("url")
                        .mimeType(mockMultipartFile.getContentType())
                        .uploadedTime(localDateTime).build();

        when(service.uploadCovidSelfie(any(MultipartFile.class), eq("12345"),
                eq(localDateTime))).thenReturn(riderCovidSelfie);

        ResponseEntity<RiderCovidSelfie> responseEntity =
                (ResponseEntity<RiderCovidSelfie>) riderCovidSelfieController.uploadCovidSelfieFile("12345",
                        mockMultipartFile, localDateTime);
        assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

    }

    @Test(expected = InvalidImageExtensionException.class)
    public void testCovidSelfieFileUploadInvalidImageExtensionTest() throws InvalidImageExtensionException, FileConversionException {

        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png",
                MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());

        LocalDateTime localDateTime = LocalDateTime.now();

        RiderCovidSelfie riderCovidSelfie =
                RiderCovidSelfie.builder().id("primaryKey").riderId("12345").fileName("url")
                        .mimeType(mockMultipartFile.getContentType())
                        .uploadedTime(localDateTime).build();

        when(service.uploadCovidSelfie(any(MultipartFile.class), eq("12345"),
                eq(localDateTime))).thenThrow(InvalidImageExtensionException.class);

        ResponseEntity<RiderCovidSelfie> responseEntity =
                (ResponseEntity<RiderCovidSelfie>) riderCovidSelfieController.uploadCovidSelfieFile("12345",
                        mockMultipartFile, localDateTime);
        assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

    }

    @Test(expected = FileConversionException.class)
    public void testCovidSelfieFileUploadFileConversionExceptionTest() throws InvalidImageExtensionException, FileConversionException {

        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png",
                MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());

        LocalDateTime localDateTime = LocalDateTime.now();

        RiderCovidSelfie riderCovidSelfie =
                RiderCovidSelfie.builder().id("primaryKey").riderId("12345").fileName("url")
                        .mimeType(mockMultipartFile.getContentType())
                        .uploadedTime(localDateTime).build();

        when(service.uploadCovidSelfie(any(MultipartFile.class), eq("12345"),
                eq(localDateTime))).thenThrow(FileConversionException.class);

        ResponseEntity<RiderCovidSelfie> responseEntity =
                (ResponseEntity<RiderCovidSelfie>) riderCovidSelfieController.uploadCovidSelfieFile("12345",
                        mockMultipartFile, localDateTime);
        assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

    }

    @Test
    public void testCovidSelfieFileFetch() throws InvalidImageExtensionException, FileConversionException {

        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png",
                MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());

        LocalDateTime localDateTime = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0,5);
        RiderCovidSelfie riderCovidSelfie =
                RiderCovidSelfie.builder().id("primaryKey").riderId("12345").fileName("url")
                        .mimeType(mockMultipartFile.getContentType())
                        .uploadedTime(localDateTime).build();

        ArrayList<RiderCovidSelfie> list = new ArrayList<>();
        list.add(riderCovidSelfie);
        Page<RiderCovidSelfie> page = new PageImpl<>(list, pageable, list.size());

        when(service.uploadCovidSelfie(any(MultipartFile.class), eq("12345"),
                eq(localDateTime))).thenReturn(riderCovidSelfie);

        when(service.getAllCovidSelfie(eq("12345"),
                eq(localDateTime), eq(localDateTime), eq(pageable))).thenReturn(page);

        ResponseEntity<Page<RiderCovidSelfie>> responseEntity =
                (ResponseEntity<Page<RiderCovidSelfie>>) riderCovidSelfieController
                        .getUploadedFile("12345", localDateTime, localDateTime, 0,5);

        assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody().getContent().get(0).getFileName());
        assertNotNull(responseEntity.getBody().getContent().get(0).getRiderId());
        assertNotNull(responseEntity.getBody().getContent().get(0).getId());
        assertNotNull(responseEntity.getBody().getContent().get(0).getUploadedTime());

    }

    @Test
    public void testCovidSelfieFileTestFetchNotFound()
            throws InvalidImageExtensionException, FileConversionException {

        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png",
                MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());

        Pageable pageable = PageRequest.of(0,5);

        LocalDateTime localDateTime = LocalDateTime.now();

        RiderCovidSelfie riderCovidSelfie =
                RiderCovidSelfie.builder().id("primaryKey").riderId("12345").fileName("url")
                        .mimeType(mockMultipartFile.getContentType())
                        .uploadedTime(localDateTime).build();

        ArrayList<RiderCovidSelfie> list = new ArrayList<>();
        Page<RiderCovidSelfie> page = new PageImpl<>(list, pageable, list.size());

        when(service.uploadCovidSelfie(any(MultipartFile.class), eq("12345"),
                eq(localDateTime))).thenReturn(riderCovidSelfie);

        when(service.getAllCovidSelfie(eq("12345"),
                eq(localDateTime), eq(localDateTime),eq(pageable))).thenReturn(page);

        ResponseEntity<Page<RiderCovidSelfie>> responseEntity =
                (ResponseEntity<Page<RiderCovidSelfie>>) riderCovidSelfieController
                        .getUploadedFile("12345", localDateTime, localDateTime, 0,5);

        assertTrue(ObjectUtils.isEmpty(responseEntity.getBody().getContent()));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(0, responseEntity.getBody().getContent().size());
        assertNotNull(responseEntity.getBody().getContent().size());


    }


    @Test
    public void testCovidSelfieDownloadFileTest()
            throws Exception, InvalidImageExtensionException, FileConversionException {

        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png",
                MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());

        LocalDateTime localDateTime = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0,5, Sort.by("uploadedTime").descending());

        RiderCovidSelfieDataList riderCovidSelfieDataList = new RiderCovidSelfieDataList("hello.png", localDateTime,
                mockMultipartFile.getContentType(), "Hello, World!".getBytes());
        RiderCovidSelfieData riderCovidSelfieData = new RiderCovidSelfieData();
        riderCovidSelfieData.setCurrentPage(0);
        riderCovidSelfieData.setTotalCount(new Long(1));
        riderCovidSelfieData.setTotalPages(1);
        ArrayList<RiderCovidSelfieDataList> list = new ArrayList<>();
        list.add(riderCovidSelfieDataList);
        RiderCovidSelfie riderCovidSelfie =
                RiderCovidSelfie.builder().id("primaryKey").riderId("12345").fileName("url")
                        .uploadedTime(localDateTime).build();

        ArrayList<RiderCovidSelfie> rlist = new ArrayList<>();
        rlist.add(riderCovidSelfie);
        Page<RiderCovidSelfie> page = new PageImpl<>(rlist, pageable, rlist.size());

        riderCovidSelfieData.setRiderCovidSelfieDataLists(list);
        when(service.downloadCovidSelfie(eq("12345"),
                eq(localDateTime), eq(localDateTime), eq(pageable))).thenReturn(riderCovidSelfieData);

        when(service.getAllCovidSelfie(eq("12345"),
                eq(localDateTime), eq(localDateTime), eq(pageable))).thenReturn(page);

        ResponseEntity<RiderCovidSelfieData> responseEntity =
                (ResponseEntity<RiderCovidSelfieData>) riderCovidSelfieController
                        .getDownloadFile("12345", localDateTime, localDateTime, 0,5);


        assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());


    }


}
