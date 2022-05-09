package com.scb.rider.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.enumeration.RiderJobStatus;

@Repository
public class RiderJobDetailsCustomRepositoryImpl implements RiderJobDetailsCustomRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<RiderJobDetails> findRiderJobReconciliationDetails(LocalDateTime startDate, LocalDateTime endDate) {

        final Query query = new Query();

        final List<Criteria> foodDeliveredCriteria = new ArrayList<>();
        final List<Criteria> orderCancelledCriteria = new ArrayList<>();
        if (startDate != null) {
            foodDeliveredCriteria.add(Criteria.where("foodDeliveredTime").gte(startDate));
            orderCancelledCriteria.add(Criteria.where("orderCancelledByOperationTime").gte(startDate));
        }
        if (endDate != null) {
            foodDeliveredCriteria.add(Criteria.where("foodDeliveredTime").lt(endDate));
            orderCancelledCriteria.add(Criteria.where("orderCancelledByOperationTime").lt(endDate));
        }
        foodDeliveredCriteria.add(Criteria.where("jobStatus").is(RiderJobStatus.FOOD_DELIVERED.name()));
		orderCancelledCriteria.add(Criteria.where("jobStatus").is(RiderJobStatus.ORDER_CANCELLED_BY_OPERATOR.name()));

        foodDeliveredCriteria.add(Criteria.where("profileId").ne(null));
        orderCancelledCriteria.add(Criteria.where("profileId").ne(null));

        Criteria firstCriteria = new Criteria().andOperator(foodDeliveredCriteria.toArray(new Criteria[foodDeliveredCriteria.size()]));
        Criteria secondCriteria = new Criteria().andOperator(orderCancelledCriteria.toArray(new Criteria[orderCancelledCriteria.size()]));
        Criteria finalCriteria = new Criteria().orOperator(firstCriteria, secondCriteria);

        query.addCriteria(finalCriteria);
        return mongoTemplate.find(query, RiderJobDetails.class);
    }
    
    @Override
    public RiderJobDetails findRunningJobIdForRider(String profileId)
    {
    	List<String> riderStaus = RiderJobStatus.getNotActiveRiderJobStatuses();
    	final Query query = new Query();
        query.addCriteria(Criteria.where("profileId").is(profileId).andOperator(
        		Criteria.where("jobStatus").nin(riderStaus)))
        .with(Sort.by(Sort.Direction.DESC, "createdDate"))
        .limit(1);
		
       return mongoTemplate.findOne(query, RiderJobDetails.class);
    }
    
    
    
    
}
