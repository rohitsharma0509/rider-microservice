package com.scb.rider.repository;

import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.RiderStatusAggregateCountDocument;
import com.scb.rider.model.enumeration.AvailabilityStatus;
import com.scb.rider.model.enumeration.RiderStatus;
import org.apache.commons.lang3.Functions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RiderProfileRepository extends MongoRepository<RiderProfile, String> {
    Optional<RiderProfile> findByPhoneNumber(String phoneNumber);
    Optional<RiderProfile> findByAccountNumber(String accountNumber);
    RiderProfile findFirstByOrderByPhoneNumberAsc();
    List<RiderProfile> findByIdIn(List<String> riders);
    Page<RiderProfile> findByIdIn(List<String> riderIds, Pageable pageable);
    List<RiderProfile> findByRiderIdIn(List<String> riderIds);
    List<RiderProfile>  findByIdInAndAvailabilityStatus(List<String> riders, AvailabilityStatus status);
    List<RiderProfile>  findByAvailabilityStatus(AvailabilityStatus status);
    List<RiderProfile>  findByAvailabilityStatusAndStatus(AvailabilityStatus status, RiderStatus riderStatus);
    Page<RiderProfile> findByStatus(RiderStatus riderStatus, Pageable pageable);

    Optional<RiderProfile> findByIdInAndStatusNot(String id, RiderStatus status);
    
    @Aggregation(pipeline = {"{$group: {_id:'$status', uniqueCount:{$sum:1}}}", "{$project: {_id: '$_','aggregateStatus':'$_id','riderCount': '$uniqueCount'}}"})
    AggregationResults<RiderStatusAggregateCountDocument> groupByStatus();
    
    @Aggregation(pipeline = {"{$group: {_id: {'riders':'$_'},uniqueCount:{$sum:1}}}", "{$project: {_id:'$riders','aggregateStatus':'allriderscount',riderCount:'$uniqueCount'}}"})
    AggregationResults<RiderStatusAggregateCountDocument> groupByRiderId();
    
    @Aggregation(pipeline = {"{$match:{'availabilityStatus':?0}}", "{$count:'riderId'}", "{$project:{'aggregateStatus':'rideronactivejobcount','riderCount':'$riderId'}}"})
    AggregationResults<RiderStatusAggregateCountDocument> groupByRiderAvailabilityStatus(String availabilityStatus);

    Optional<RiderProfile> findByRiderId(String riderId);

    Optional<RiderProfile> findByNationalID(String nationalID);

    final String FIND_BY_ZONEID_AND_STATUS =
            "{$and:[{'status': { $regex: /.*?0.*/, $options: 'i'} }, {'riderPreferredZone.preferredZoneId': { $regex: /.*?1.*/, $options: 'i'}}]}";
    @Query(FIND_BY_ZONEID_AND_STATUS)
    List<RiderProfile> findAllByPreferredZoneIdAndStatus(AvailabilityStatus status, String zoneId);

    final String FIND_BY_ZONEID =
            "{'riderPreferredZones.preferredZoneId': { $regex: /.*?0.*/, $options: 'i'}}";
    @Query(FIND_BY_ZONEID)
    List<RiderProfile> findAllByPreferredZoneId(String zoneId);
    List<RiderProfile> findByEvBikeUserTrue();

    List<RiderProfile>  findByRiderIdInAndStatus(List<String> riders, RiderStatus status);

    Page<RiderProfile> findAllByStatusIn(List<RiderStatus> status, Pageable pageable);

    Page<RiderProfile> findAllByRiderPreferredZones_PreferredZoneIdAndStatusIn(String zoneId,List<RiderStatus> status, Pageable pageable);

    Page findAllByStatusInAndApprovalDateTimeIsNull(List<RiderStatus> status, Pageable pageable);

    Long countByStatusInAndApprovalDateTimeIsNull(List<RiderStatus> status);
}
