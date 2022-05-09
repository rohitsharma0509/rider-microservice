package com.scb.rider.service.document;

import com.scb.rider.model.document.RiderMannerScoreHistory;
import com.scb.rider.model.dto.PaginatedRiderMannerScoreHistoryList;
import com.scb.rider.model.dto.PaginatedRiderSuspensionHistoryList;
import com.scb.rider.model.dto.RiderProfileMannerScoreHistoryDto;
import com.scb.rider.repository.RiderMannerScoreHistoryRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public class RiderMannerScoreService {

    @Autowired
    private RiderMannerScoreHistoryRepository riderMannerScoreHistoryRepository;

    public PaginatedRiderMannerScoreHistoryList getMannerScoreHistoryList(String riderId, Pageable pageable) {
        LocalDateTime createdDateAfter = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).minusMonths(6);
        Page<RiderMannerScoreHistory> mannerScoreHistoryPage = riderMannerScoreHistoryRepository.findByRiderIdAndCreatedDateAfter(riderId, createdDateAfter, pageable);
        List<RiderProfileMannerScoreHistoryDto> histories = mannerScoreHistoryPage.getContent().stream()
                .map(RiderProfileMannerScoreHistoryDto::of)
                .collect(Collectors.toList());

        if (ObjectUtils.isEmpty(mannerScoreHistoryPage) || (!mannerScoreHistoryPage.hasContent() && mannerScoreHistoryPage.getTotalElements() < 1)) {
            log.error("Record not found for riderId " + riderId);
            return PaginatedRiderMannerScoreHistoryList.builder()
                    .currentPage("0")
                    .totalCount("0")
                    .totalPages("0")
                    .histories(histories)
                    .build();
        }

        return PaginatedRiderMannerScoreHistoryList.builder()
                .currentPage(String.format("%d", mannerScoreHistoryPage.getNumber() + 1))
                .totalCount(String.format("%d", mannerScoreHistoryPage.getTotalElements()))
                .totalPages(String.format("%d", mannerScoreHistoryPage.getTotalPages()))
                .histories(histories)
                .build();
    }

}
