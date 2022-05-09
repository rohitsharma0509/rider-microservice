package com.scb.rider.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.scb.rider.constants.DocumentType;
import com.scb.rider.model.document.RiderUploadedDocument;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RiderUploadedDocumentResponse {
    private String id;

    private String imageUrl;

    private String riderProfileId;

    private DocumentType documentType;
    
    private String  imageExternalUrl;

    private List<String> documentUrls;

    public static RiderUploadedDocumentResponse of(RiderUploadedDocument document) {

        return RiderUploadedDocumentResponse.builder()
                .id(document.getId())
                .imageUrl(document.getImageUrl())
                .riderProfileId(document.getRiderProfileId())
                .documentType(document.getDocumentType())
                .imageExternalUrl(document.getImageExternalUrl())
                .documentUrls(document.getDocumentUrls())
                .build();
    }
}
