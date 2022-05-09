package com.scb.rider.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import com.scb.rider.model.document.RiderProfile;

public interface RiderSearchRepository extends MongoRepository<RiderProfile, String> {

  final String filterByStatusAndQueryOnFields =
          "{$and:[{'$or':[ {'firstName': { $regex: /.*?0.*/, $options: 'i'} }, {'lastName': { $regex: /.*?1.*/, $options: 'i'}}]}," +
                  "{'$and':[ {'riderId': { $regex: /.*?2.*/, $options: 'i'}}, {'phoneNumber': { $regex: /.*?3.*/, $options: 'i'}}," +
                  "{'status': { $regex: /^?4.*/, $options: 'i'}}] }," +
                  "{status: { $regex: /^?5.*/, $options: 'i'}},{firstName: { $regex: /^?6.*/, $options: 'i'}},{lastName: { $regex: /^?7.*/, $options: 'i'}},{isReadyForAuthorization:  { $regex: /^?8.*/, $options: 'i'}},{'$or':[ {'approvalDateTime': {$exists:false}}, {'approvalDateTime': { $regex: /.*?9.*/, $options: 'i'} }]}," +
                  "{'$or':[ {'riderPreferredZones.preferredZoneName': {$exists:false}}, {'riderPreferredZones.preferredZoneName': { $regex: /.*?10.*/, $options: 'i'} }]}, "
                  + "{tierName:  { $regex: /^?11.*/, $options: 'i'}}, {'riderDocumentUpload.enrollmentDate':  { $regex: /^?12.*/, $options: 'i'}}]}";

  final String filterByRiderIdsAndQueryOnFields =
          "{$and:[{'$or':[{'firstName': { $regex: /.*?0.*/, $options: 'i'} },{'lastName': { $regex: /.*?1.*/, $options: 'i'}}]},{'$and':[{'riderId': { $regex: /.*?2.*/, $options: 'i'}}, {'phoneNumber': { $regex: /.*?3.*/, $options: 'i'}},{'status': { $regex: /^?4.*/, $options: 'i'}},{firstName: { $regex: /^?6.*/, $options: 'i'}}, {lastName: { $regex: /^?7.*/, $options: 'i'}},{tierName: { $regex: /^?8.*/, $options: 'i'}}] },{id: {$in:?5}},"
          + "{'$or':[ {'riderPreferredZones.preferredZoneName': {$exists:false}}, {'riderPreferredZones.preferredZoneName': { $regex: /.*?9.*/, $options: 'i'} }]},"
          + "{'riderDocumentUpload.enrollmentDate':  { $regex: /^?10.*/, $options: 'i'}}]}";
  
  
  @Query("{$and:[{'$or':[ {'riderId': { $regex: /.*?0.*/, $options: 'i'}}, {'firstName': { $regex: /.*?1.*/, $options: 'i'} }, {'lastName': { $regex: /.*?2.*/, $options: 'i'}}, {'phoneNumber': { $regex: /.*?0.*/, $options: 'i'}}, {'status': { $regex: /^?0.*/, $options: 'i'}} ]},{'firstName': { $regex: /.*?3.*/, $options: 'i'} }, {'lastName': { $regex: /.*?4.*/, $options: 'i'}}]}")
  Page<RiderProfile> findRiderByTerm(String query, String firstName, String LastName,
      String mandatoryFirstName, String mandatoryLastName, Pageable pageable);

  @Query("{$and:[{'$or':[ {?0: { $regex: /.*?1.*/, $options: 'i'}}, {'firstName': { $regex: /.*?0.*/, $options: 'i'} }, {'lastName': { $regex: /.*?0.*/, $options: 'i'}}, {'phoneNumber': { $regex: /.*?0.*/, $options: 'i'}}, {'status': { $regex: /^?0.*/, $options: 'i'}} ] },{firstName: { $regex: /^?2.*/, $options: 'i'}},{lastName: { $regex: /^?3.*/, $options: 'i'}}]}")
  Page<RiderProfile> findRiderByTermViewByAndDateFilter(String column, String query,
      String mandatoryFirstName, String mandatoryLastName, Pageable pageable);

  @Query(filterByStatusAndQueryOnFields)
  Page<RiderProfile> getRidersByStatusAndQueryOnFields(String firstName, String lastName,
      String riderId, String phoneNumber, String status, String viewBy, String mandatoryFirstName,
      String mandatoryLastName, String isReadyForAuthorization, String approvalDateTime, String preferredZoneName, String tierName, 
      String enrollmentDate, Pageable pageable);

  @Query(filterByRiderIdsAndQueryOnFields)
  Page<RiderProfile> getRidersByRiderIdsAndQueryOnFields(String firstName, String lastName,
      String riderId, String phoneNumber, String status, List<String> riderIds,
      String mandatoryFirstName, String mandatoryLastName, String tierName, String preferredZoneName,
      String enrollmentDate, Pageable pageable);

  Optional<RiderProfile> findByIdAndPhoneNumber(String id, String phoneNumber);
  
  Optional<RiderProfile> findByRiderIdAndPhoneNumber(String id, String phoneNumber);
}
