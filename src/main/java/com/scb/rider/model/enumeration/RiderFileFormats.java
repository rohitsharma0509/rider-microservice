package com.scb.rider.model.enumeration;

import java.util.Arrays;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;
import com.scb.rider.exception.InvalidImageExtensionException;

public enum RiderFileFormats {
  pdf, png, jpg, jpeg;

  public static void validateFileExtension(MultipartFile[] files) throws InvalidImageExtensionException {
    for(MultipartFile file: files) {
      validateFileExtension(file);
    }
  }

  public static void validateFileExtension(MultipartFile multipartFile)
      throws InvalidImageExtensionException {
    
    String fileExtension = FilenameUtils.getExtension(multipartFile.getOriginalFilename());
    boolean isValidExtension = Arrays.stream(RiderFileFormats.values())
        .anyMatch(fileFormat -> fileFormat.name().equalsIgnoreCase(fileExtension));
    if (!isValidExtension) {
      throw new InvalidImageExtensionException("Invalid message");
    }
  }

}
