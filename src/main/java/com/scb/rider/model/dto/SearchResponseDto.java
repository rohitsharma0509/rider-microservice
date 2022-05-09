package com.scb.rider.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.scb.rider.model.document.RiderRemarksDetails;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchResponseDto {

  private Integer totalPages;

  private Long totalCount;
  
  private Integer currentPage;

  private List<RiderSearchProfileDto> riderDetails;
  
  private List<RiderRemarksDetails> riderRemarks;


  public static SearchResponseDto of(List<RiderSearchProfileDto> riderProfiles, int totalPages, long l, int currentPageNumber) {
    SearchResponseDto searchResponseDto = SearchResponseDto.builder().riderDetails(riderProfiles)
        .totalPages(totalPages).totalCount(l).currentPage(currentPageNumber).build();
    return searchResponseDto;
  }
  
  
	public static SearchResponseDto ofRemarks(List<RiderRemarksDetails> riderRemarks, int totalPages, long l,
			int currentPageNumber) {
		return SearchResponseDto.builder().riderRemarks(riderRemarks).totalPages(totalPages).totalCount(l)
				.currentPage(currentPageNumber).build();

	}
  
  
}
