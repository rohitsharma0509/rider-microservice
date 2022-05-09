package com.scb.rider.service.factory;

import com.scb.rider.constants.DocumentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class RiderDocumentFactory {

    @Autowired
    private ApplicationContext context;

    public UploadService getInstance(DocumentType documentType) {
        if(DocumentType.BACKGROUND_VERIFICATION_FORM.equals(documentType)) {
            return context.getBean(BackgroundVerificationUploadService.class);
        }
        return null;
    }
}
