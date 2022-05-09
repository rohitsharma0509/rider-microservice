package com.scb.rider.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.dto.RiderIdsOnTrainingOrActiveJobCount;

@Repository
public interface RiderJobDetailsRepository extends MongoRepository<RiderJobDetails, String>,
        RiderJobDetailsCustomRepository {

    Optional<RiderJobDetails> findByJobId(String jobId);

    Optional<RiderJobDetails> findByJobIdAndProfileId(String jobId, String profileId);

    List<RiderJobDetails> findByProfileIdAndJobStatusNotIn(String profileId, List<String> status);

    Long deleteByProfileId(String profileId);
    
    @Aggregation(pipeline = {"{$match:{'jobStatus':{$in:?0}}}","{$group: {_id: {'riders':'$_'},uniqueCount: {$addToSet: '$profileId'}}}", "{$project:{_id:'$_',riderIds:'$uniqueCount'}}"})
    AggregationResults<RiderIdsOnTrainingOrActiveJobCount> getRiderIdgroupByActiveJob(List<String> activeRiderJobStatuses);

    List<RiderJobDetails>  findTop1ByProfileIdOrderByJobAcceptedTime(String id);
}
