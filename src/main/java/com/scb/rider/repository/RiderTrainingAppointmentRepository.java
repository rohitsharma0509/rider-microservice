package com.scb.rider.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.scb.rider.model.enumeration.TrainingType;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.scb.rider.model.document.RiderSelectedTrainingAppointment;
import com.scb.rider.model.dto.RiderIdsOnTrainingOrActiveJobCount;
import com.scb.rider.model.dto.RiderStatusAggregateCountDocument;

public interface RiderTrainingAppointmentRepository extends MongoRepository<RiderSelectedTrainingAppointment, String> {
	
	List<RiderSelectedTrainingAppointment> findByRiderId(String riderId);

	Optional<RiderSelectedTrainingAppointment> findByRiderIdAndTrainingType(String riderId, TrainingType trainingType);

	List<RiderSelectedTrainingAppointment> findByRiderIdAndTrainingTypeIn(String riderId, List<String> trainingTypes);

	Long deleteByRiderId(String riderId);
	
	@Aggregation(pipeline = {"{$match:{'date':?0}}","{$group: {_id: {'riders':'$_'},uniqueCount:{$sum:1}}}", "{$project: {_id:'$riders','aggregateStatus':'ridertrainingtodaycount',riderCount:'$uniqueCount'}}"})
    AggregationResults<RiderStatusAggregateCountDocument> groupByTodaysDate(LocalDate now);
	
	@Aggregation(pipeline = {"{$match:{'date':?0}}","{$group: {_id: {'riders':'$_'},uniqueCount: {$addToSet: '$riderId'}}}", "{$project:{_id:'$_',riderIds:'$uniqueCount'}}"})
    AggregationResults<RiderIdsOnTrainingOrActiveJobCount> getRiderIdgroupByTodaysDate(LocalDate now);


	List<RiderSelectedTrainingAppointment> findByAppointmentId(String appointmentId);
	
}
