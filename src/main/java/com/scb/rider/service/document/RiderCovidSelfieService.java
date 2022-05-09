package com.scb.rider.service.document;

import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.exception.FileConversionException;
import com.scb.rider.exception.InvalidImageExtensionException;
import com.scb.rider.model.document.RiderCovidSelfie;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.RiderCovidSelfieData;
import com.scb.rider.model.dto.RiderCovidSelfieDataList;
import com.scb.rider.model.enumeration.RiderFileFormats;
import com.scb.rider.repository.RiderCovidSelfieRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.service.AmazonS3ImageService;
import com.scb.rider.util.FileUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
public class RiderCovidSelfieService {
    private static final String RIDER_SELFIE_FOLDER = "covid-selfie";

    @Autowired
    private RiderProfileRepository riderProfileRepository;
    @Autowired
    private RiderCovidSelfieRepository riderCovidSelfieRepository;
    @Autowired
    private AmazonS3ImageService s3ImageService;

    public RiderCovidSelfie uploadCovidSelfie(MultipartFile multipartFile, String riderId, LocalDateTime uploadedTime) throws InvalidImageExtensionException, FileConversionException {

        log.info("Validating file extension : {}", multipartFile.getName());
        RiderFileFormats.validateFileExtension(multipartFile);

        RiderProfile riderProfile = this.riderProfileRepository.findById(riderId)
                .orElseThrow(() -> new DataNotFoundException("Record not found for id " + riderId));

        String folder = riderId + "/" + uploadedTime.getDayOfMonth() + "-" + uploadedTime.getMonth() + "-" + uploadedTime.getYear();
        File file = null;
        String imageKey = "";
        try {
            file = FileUtils.convertMultipartToFile(multipartFile);
            log.info("Uploading File to S3 Bucket on folder: {}", folder);
            imageKey = s3ImageService.uploadFile(file, RIDER_SELFIE_FOLDER, folder);
            if (!Files.deleteIfExists(file.toPath())) {
                log.info("Unable to delete file: {}", file.getName());
            }
        } catch (IOException e) {
            throw new FileConversionException(e);
        }

        RiderCovidSelfie riderCovidSelfie = RiderCovidSelfie.builder()
                .riderId(riderProfile.getId())
                .mimeType(multipartFile.getContentType())
                .fileName(imageKey)
                .uploadedTime(uploadedTime).build();

        return riderCovidSelfieRepository.save(riderCovidSelfie);

    }

    public Page<RiderCovidSelfie> getAllCovidSelfie(String riderId, LocalDateTime from, LocalDateTime to, Pageable pageable) {

        return riderCovidSelfieRepository.findByRiderIdAndUploadedTimeBetween(riderId, from, to, pageable);

    }

    public RiderCovidSelfieData downloadCovidSelfie(String riderId, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        Page<RiderCovidSelfie> page = riderCovidSelfieRepository.findByRiderIdAndUploadedTimeBetween(riderId, from, to ,pageable);
        log.info("Fetched Covid Selfie Data for rider : {}", page.getTotalElements());
        List<RiderCovidSelfieDataList> riderCovidSelfieDataListList = new ArrayList<>();

        page.getContent().stream()
                .forEach(
                        selfiePic -> {
                            String folder = riderId + "/" + selfiePic.getUploadedTime().getDayOfMonth() + "-" + selfiePic.getUploadedTime().getMonth() + "-" + selfiePic.getUploadedTime().getYear();
                            byte data[] = new byte[0];
                            try {
                                data = s3ImageService.downloadFile(RIDER_SELFIE_FOLDER, folder, selfiePic.getFileName());
                            } catch (IOException e) {
                                e.printStackTrace();
                                log.info("Exception while downloading file from S3 {} , folder ",e.getMessage(), folder);
                            }
                            riderCovidSelfieDataListList.add(
                                    new RiderCovidSelfieDataList(selfiePic.getFileName(), selfiePic.getUploadedTime(), selfiePic.getMimeType(), data));
                        }
                );

        return new RiderCovidSelfieData(page.getTotalPages(),
                page.getTotalElements(), pageable.getPageNumber(), riderCovidSelfieDataListList);

    }

}
