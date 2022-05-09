package com.scb.rider.events;

import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.service.SequenceGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
public class RiderProfileEventListener extends AbstractMongoEventListener<RiderProfile> {
    @Value("${rider.profile.database.sequence.prefix}")
    private String prefix;

    public static final String SEQUENCE_NAME = "rider_profile_sequence";

    private SequenceGeneratorService sequenceGenerator;

    @Autowired
    public RiderProfileEventListener(SequenceGeneratorService sequenceGenerator) {
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public void onBeforeConvert(BeforeConvertEvent<RiderProfile> event) {
        if (event.getSource().getRiderId() == null) {
            event.getSource().setRiderId(prefix +
                    sequenceGenerator.generateSequence(SEQUENCE_NAME));
        }
    }
}
