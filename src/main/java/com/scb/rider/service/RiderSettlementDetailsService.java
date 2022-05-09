package com.scb.rider.service;

import com.google.common.collect.Lists;
import com.scb.rider.client.JobServiceFeignClient;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.JobSettlementDetails;
import com.scb.rider.model.dto.RiderSettlementDetails;
import com.scb.rider.repository.RiderJobDetailsRepository;
import com.scb.rider.repository.RiderProfileRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Log4j2
public class RiderSettlementDetailsService {

    private static final Integer BATCH_SIZE= 800;
    @Autowired
    private RiderJobDetailsRepository riderJobDetailsRepository;

    @Autowired
    private RiderProfileRepository riderProfileRepository;

    @Autowired
    private JobServiceFeignClient jobServiceFeignClient;

    public List<RiderSettlementDetails> getRiderSettlementDetails(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Fetching data in between start time : {} and end time {}", startTime, endTime);
        List<RiderJobDetails> riderJobDetailsList = riderJobDetailsRepository
                .findRiderJobReconciliationDetails(startTime, endTime);

        List<String> riderIdList = new ArrayList<>();
        List<String> jobIdList = new ArrayList<>();
        riderJobDetailsList.stream().forEach(riderJob -> {
            if (!riderIdList.contains(riderJob.getProfileId())) {
                riderIdList.add(riderJob.getProfileId());
            }
            if (!jobIdList.contains(riderJob.getJobId())) {
                jobIdList.add(riderJob.getJobId());
            }
        });
        List<CompletableFuture<List<JobSettlementDetails>>> completableFutures = new ArrayList<>();
        List<List<String>> subSets = Lists.partition(jobIdList, BATCH_SIZE);
        subSets.stream().forEach(batchIds -> completableFutures.add(callJobService(batchIds)));

        CompletableFuture<List<List<JobSettlementDetails>>> allCompletableFuture = CompletableFuture
                .allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()])).thenApply(future ->
                        completableFutures.stream()
                                .map(CompletableFuture::join)
                                .collect(Collectors.toList())
                );
        List<JobSettlementDetails> jobSettlementDetailsList = new ArrayList<>();
        try {
            List<List<JobSettlementDetails>> listOfLists = allCompletableFuture.get();
            jobSettlementDetailsList = listOfLists.stream().flatMap(List::stream).collect(Collectors.toList());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        Map<String, JobSettlementDetails> jobListMap = jobSettlementDetailsList
                .stream()
                .collect(Collectors.toMap(JobSettlementDetails::getJobId, job -> job));

        List<RiderProfile> riderProfiles = riderProfileRepository.findByIdIn(riderIdList);

        Map<String, RiderProfile> riderListMap = riderProfiles
                .stream()
                .collect(Collectors.toMap(RiderProfile::getId, rider -> rider));

        return createRiderReconciliationList(riderJobDetailsList, riderListMap, jobListMap);
    }

    private List<RiderSettlementDetails> createRiderReconciliationList(List<RiderJobDetails> riderJobDetailsList,
                                                                       Map<String, RiderProfile> riderListMap,
                                                                       Map<String, JobSettlementDetails> jobListMap) {

        return riderJobDetailsList.stream()
                .map(riderJob -> RiderSettlementDetails.of(riderJob, riderListMap.get(riderJob.getProfileId()),
                        jobListMap.get(riderJob.getJobId())))
                .collect(Collectors.toList());

    }

    public CompletableFuture<List<JobSettlementDetails>> callJobService(List<String> jobIdList) {
        log.info("Fetching Job Details for  size {}", jobIdList.size());
        return CompletableFuture.supplyAsync(()->{
            log.info("Calling Job Service Start");
            return jobServiceFeignClient.getJobDetails(jobIdList);
        });
    }
}
