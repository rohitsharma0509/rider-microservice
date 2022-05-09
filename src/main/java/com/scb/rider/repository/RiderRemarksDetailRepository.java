package com.scb.rider.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.scb.rider.model.document.RiderRemarksDetails;

public interface RiderRemarksDetailRepository extends MongoRepository<RiderRemarksDetails, String> {

	final String FILTER_BY_ALL_REMARKS_AND_QUERY =
		      "{$and:[{'riderId': ?3},"
		      + "{'$and':[{'dateSearch': { $regex: /.*?0.*/, $options: 'i'}},"
		      + "{'timeSearch': { $regex: /.*?1.*/, $options: 'i'}}, "
		      + "{'remark': { $regex: /.*?2.*/, $options: 'i'}}]}]}";
	
	@Query(FILTER_BY_ALL_REMARKS_AND_QUERY)
	 Page<RiderRemarksDetails> getAllRemarks(String date, String time, String remark,String riderId,Pageable pageable);

	Page<RiderRemarksDetails> findByRiderId(String riderId, Pageable pageable);

   
}
