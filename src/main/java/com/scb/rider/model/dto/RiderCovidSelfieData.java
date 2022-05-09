package com.scb.rider.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RiderCovidSelfieData {

    private Integer totalPages;

    private Long totalCount;

    private Integer currentPage;

    private List<RiderCovidSelfieDataList> riderCovidSelfieDataLists;

}
