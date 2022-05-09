package com.scb.rider.service.document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.model.document.RiderRemarksDetails;
import com.scb.rider.model.dto.SearchResponseDto;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.repository.RiderRemarksDetailRepository;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class RiderRemarksService {
	@Autowired
	private RiderProfileRepository riderProfileRepository;

	@Autowired
	private RiderRemarksDetailRepository detailRepository;

	private static final String DATE = "date";

	private static final String TIME = "time";

	private static final String REMARK = "remark";

	public RiderRemarksDetails saveRiderRemarksInfo(String riderId, RiderRemarksDetails riderRemarksDetails) {
		riderProfileRepository.findById(riderId)
				.orElseThrow(() -> new DataNotFoundException("Record not found for id " + riderId));

		riderRemarksDetails.setDateSearch(riderRemarksDetails.getDate().toString());
		riderRemarksDetails.setTimeSearch(riderRemarksDetails.getTime().toString());
		riderRemarksDetails.setRiderId(riderId);
		RiderRemarksDetails savedRiderRemarksDetails = detailRepository.save(riderRemarksDetails);

		log.info("Remark saved for riderId-{}", riderId);
		return savedRiderRemarksDetails;
	}

	public void deleteRiderRemarksInfo(@NotEmpty String messageId) {

		detailRepository.findById(messageId)
				.orElseThrow(() -> new DataNotFoundException("Record not found for id " + messageId));

		detailRepository.deleteById(messageId);

		log.info("Remark deleted for messageId-{}", messageId);

	}

	public SearchResponseDto getRemarksBySearchTermWithFilter(String riderId, List<String> filterquery,
			Pageable pageable) {

		log.info("Sorting List After Modification-->" + pageable.getSort());

		Page<RiderRemarksDetails> pageRiderRemarksDetails = null;

		log.info("Search By Filter query for remark", filterquery);

		Map<String, String> filtersQuery = new HashMap<>();
		if (!CollectionUtils.isEmpty(filterquery)) {
			filterquery.stream().forEach(filter -> {
				String filterValue[] = filter.split(":");
				if (filterValue.length >= 2) {
					if ((filterValue[0].equals(TIME)) && filterValue.length >= 3) {
						filtersQuery.put(filterValue[0], String.format("%s:%s", filterValue[1], filterValue[2]));
					} else {
						filtersQuery.put(filterValue[0], filterValue[1]);
					}
				}
			});
			pageRiderRemarksDetails = getRemarksByColumnLevelQuery(riderId, filtersQuery, pageable);
		} else {
			pageRiderRemarksDetails = getAllRemarksByRiderId(riderId, pageable);
		}
		if (ObjectUtils.isEmpty(pageRiderRemarksDetails) || !pageRiderRemarksDetails.hasContent()) {
			List<RiderRemarksDetails> remarkList = new ArrayList<RiderRemarksDetails>();
			return SearchResponseDto.ofRemarks(remarkList, 0, 0, 0);
		}

		List<RiderRemarksDetails> remarkList = pageRiderRemarksDetails.getContent();

		return SearchResponseDto.ofRemarks(remarkList, pageRiderRemarksDetails.getTotalPages(),
				pageRiderRemarksDetails.getTotalElements(), pageRiderRemarksDetails.getNumber() + 1);

	}

	private Page<RiderRemarksDetails> getAllRemarksByRiderId(String riderId, Pageable pageable) {
		log.info("getting all remarks by riderId-{}", riderId);

		return detailRepository.findByRiderId(riderId, pageable);

	}

	private Page<RiderRemarksDetails> getRemarksByColumnLevelQuery(String riderId, Map<String, String> filtersQuery,
			Pageable pageable) {
		String date = extractValue(filtersQuery, DATE);
		String time = extractValue(filtersQuery, TIME);
		String remark = extractValue(filtersQuery, REMARK);
		log.info("date-{},time-{},remark-{}", date, time, remark);

		return detailRepository.getAllRemarks(date, time, remark, riderId, pageable);
	}

	private String extractValue(Map<String, String> filtersQuery, String key) {

		return filtersQuery.getOrDefault(key, StringUtils.EMPTY);
	}

}
