package com.scb.rider.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@Setter
@Builder
public class PaginatedRiderMannerScoreHistoryList {
    private String currentPage;
    private String totalCount;
    private String totalPages;
    private List<RiderProfileMannerScoreHistoryDto> histories;
}
