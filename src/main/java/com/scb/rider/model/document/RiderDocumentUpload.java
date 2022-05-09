package com.scb.rider.model.document;

import java.util.Map;
import org.springframework.data.mongodb.core.mapping.Document;
import com.scb.rider.constants.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
public class RiderDocumentUpload {

  private Map<DocumentType, Boolean> documentUploadedFlag;
  private String  enrollmentDate;
  
}
