package com.scb.rider.controller;

import com.scb.rider.exception.FileConversionException;
import com.scb.rider.exception.InvalidImageExtensionException;
import com.scb.rider.model.document.RiderCovidSelfie;
import com.scb.rider.model.dto.RiderCovidSelfieData;
import com.scb.rider.model.dto.RiderCovidSelfieDataList;
import com.scb.rider.service.document.RiderCovidSelfieService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.time.LocalDateTime;

import static com.scb.rider.constants.UrlMappings.FileHandler.COVID_SELFIE;
import static com.scb.rider.constants.UrlMappings.FileHandler.DOWNLOAD_COVID_SELFIE;
import static com.scb.rider.constants.UrlMappings.RIDER_API;

@Slf4j
@RestController
@RequestMapping(RIDER_API)
public class RiderCovidSelfieController {

    @Autowired
    private RiderCovidSelfieService covidSelfieService;

    /**
     * To upload the covid selfie documents
     *
     * @param file
     * @return
     * @throws IOException
     */
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping(value = COVID_SELFIE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RiderCovidSelfie> uploadCovidSelfieFile(
            @PathVariable(name = "id", required = true) @NotEmpty final String id,
            @RequestPart("file") MultipartFile file,
            @RequestParam(name = "uploadedTime", required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime uploadedTime)
            throws InvalidImageExtensionException, FileConversionException {
        log.info("Uploading Covid Selfie for Rider id - {} ", id);
        RiderCovidSelfie uploadedDocument = covidSelfieService.uploadCovidSelfie(file, id, uploadedTime);

        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedDocument);

    }

    @GetMapping(value = COVID_SELFIE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<RiderCovidSelfie>> getUploadedFile(
            @PathVariable(name = "id", required = true) @NotEmpty final String id,
            @RequestParam(name = "from", required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(name = "to", required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Pageable paging = PageRequest.of(page, size);

        log.debug("Fetching Covid Selfie for Rider id - {} from time   - {} to time", id, from, to);
        Page<RiderCovidSelfie> uploadedDocuments = covidSelfieService.getAllCovidSelfie(id, from, to, paging);

        return ResponseEntity.status(HttpStatus.OK).body(uploadedDocuments);
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping(value = DOWNLOAD_COVID_SELFIE, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<RiderCovidSelfieData> getDownloadFile(
            @PathVariable(name = "id", required = true) @NotEmpty final String id,
            @RequestParam(name = "from", required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(name = "to", required = true)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        log.debug("Rider id - {} from time   - {} to time", id, from, to);
        Pageable paging = PageRequest.of(page, size, Sort.by("uploadedTime").descending());
        RiderCovidSelfieData resources = covidSelfieService.downloadCovidSelfie(id, from, to,paging);

        return ResponseEntity.status(HttpStatus.OK).body(resources);

    }
}
