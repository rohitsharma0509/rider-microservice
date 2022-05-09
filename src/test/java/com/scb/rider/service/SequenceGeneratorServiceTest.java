package com.scb.rider.service;

import com.scb.rider.model.document.DatabaseSequence;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class SequenceGeneratorServiceTest {

    @Mock
    private MongoOperations mongoOperations;

    @InjectMocks
    private SequenceGeneratorService service;

    @Test
    public void generateSequenceTest() {
        DatabaseSequence databaseSequence = new DatabaseSequence("abc_sequence", 10000);

        when(mongoOperations.findAndModify(any(), any(),
                any(), (Class<Object>) any())).thenReturn(databaseSequence);

        Long sequence = service.generateSequence("abc_sequence");

        assertNotNull(sequence);
    }

    @Test
    public void generateSequenceDatabaseIsNullTest() {
        DatabaseSequence databaseSequence = new DatabaseSequence("abc_sequence", 10000);

        when(mongoOperations.findAndModify(any(), any(),
                any(), (Class<Object>) any())).thenReturn(null);

        when(mongoOperations.save(any())).thenReturn(databaseSequence);

        Long sequence = service.generateSequence("abc_sequence");

        assertNotNull(sequence);
    }

}
