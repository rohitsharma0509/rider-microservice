package com.scb.rider.model.document;

import com.scb.rider.constants.DocumentType;
import com.scb.rider.model.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@Builder
@Document
@CompoundIndex(name="riderProfileId_documentType", def = "{'riderProfileId': 1, 'documentType': 1}", unique = true)
public class RiderUploadedDocument extends BaseEntity {

    @Id
    private String id;

    @NotNull
    private String imageUrl;

    private String imageExternalUrl;
    
    private String riderProfileId;

    private DocumentType documentType;

    private String updatedBy;

    private List<String> documentUrls;
}
