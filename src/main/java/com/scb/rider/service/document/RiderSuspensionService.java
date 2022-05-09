package com.scb.rider.service.document;

import com.scb.rider.model.document.RiderSuspendHistory;
import com.scb.rider.model.dto.PaginatedRiderSuspensionHistoryList;
import com.scb.rider.model.dto.RiderProfileSuspensionHistoryDto;
import com.scb.rider.model.dto.RiderSearchProfileDto;
import com.scb.rider.model.dto.SearchResponseDto;
import com.scb.rider.repository.RiderSuspendHistoryRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public class RiderSuspensionService {

    @Autowired
    private RiderSuspendHistoryRepository riderSuspendHistoryRepository;

    public PaginatedRiderSuspensionHistoryList getSuspensionHistoryList(String riderId, Pageable pageable) {
        LocalDateTime createdDateAfter = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).minusMonths(6);
        Page<RiderSuspendHistory> historiesPaginated = riderSuspendHistoryRepository.findByRiderIdAndCreatedDateAfter(riderId, createdDateAfter, pageable);
        List<RiderProfileSuspensionHistoryDto> histories = historiesPaginated.getContent().stream()
                .map(RiderProfileSuspensionHistoryDto::of)
                .collect(Collectors.toList());

        if (ObjectUtils.isEmpty(historiesPaginated) || (!historiesPaginated.hasContent() && historiesPaginated.getTotalElements() < 1)) {
            log.error("Record not found for riderId " + riderId);
            return PaginatedRiderSuspensionHistoryList.builder()
                    .currentPage("0")
                    .totalCount("0")
                    .totalPages("0")
                    .histories(histories)
                    .build();
        }

        return PaginatedRiderSuspensionHistoryList.builder()
                .currentPage(String.format("%d", historiesPaginated.getNumber() + 1))
                .totalCount(String.format("%d", historiesPaginated.getTotalElements()))
                .totalPages(String.format("%d", historiesPaginated.getTotalPages()))
                .histories(histories)
                .build();
    }

}
