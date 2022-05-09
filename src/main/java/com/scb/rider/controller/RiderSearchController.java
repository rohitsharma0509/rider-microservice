package com.scb.rider.controller;

import com.scb.rider.model.dto.RiderProfileDto;
import com.scb.rider.model.dto.SearchResponseDto;
import com.scb.rider.service.document.RiderSearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
@RequestMapping(value = "/ridersearch", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(value = "Rider Search Endpoints")
public class RiderSearchController {

  @Autowired
  private RiderSearchService riderSearchService;

  @ApiOperation(nickname = "get-search-rider-by-id-name-status-phoneNumber",
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
      value = "Gets Rider profiles", response = RiderProfileDto.class)
  @GetMapping
  public ResponseEntity<SearchResponseDto> getRiderProfileBySearchTerm(
      @ApiParam(value = "q", example = "5fc35ef7af8a144ac42a0a54/John",
          required = true) @RequestParam(name = "q", required = false, defaultValue = "") String query,
      @ApiParam(value = "filterquery", example = "viewby:authorized",
          required = false) @RequestParam(name = "filterquery", required = false) List<String> filterquery,
      @PageableDefault(page = 0, size = 5) @SortDefault.SortDefaults(@SortDefault(sort = "updatedDate",
          direction = Sort.Direction.DESC)) Pageable pageable) {
    log.info(String.format("Query Searched - %s", query));
    return ResponseEntity.ok(this.riderSearchService.getRiderProfileBySearchTermWithFilterQuery(query, filterquery, pageable ));
  }
}
