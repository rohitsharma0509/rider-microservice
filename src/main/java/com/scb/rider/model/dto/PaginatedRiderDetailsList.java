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
public class PaginatedRiderDetailsList {
    private int totalPages;
    private int size;
    private int currentPageNumber;
    private long totalElements;
    private List<RiderDetails> riders;
}
