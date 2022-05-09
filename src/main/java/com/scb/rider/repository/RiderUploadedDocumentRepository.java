package com.scb.rider.repository;

import com.scb.rider.constants.DocumentType;
import com.scb.rider.model.document.RiderUploadedDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RiderUploadedDocumentRepository extends MongoRepository<RiderUploadedDocument, String> {

    Optional<RiderUploadedDocument> findByRiderProfileIdAndDocumentType(String profileId, DocumentType documentType);
    Long deleteByRiderProfileIdAndDocumentType(String profileId, DocumentType documentType);

    Long deleteByRiderProfileId(String profileId);

}

