package com.scb.rider.service.document;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.scb.rider.exception.InvalidInputException;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.document.RiderSelectedTrainingAppointment;
import com.scb.rider.model.dto.RiderIdsOnTrainingOrActiveJobCount;
import com.scb.rider.model.dto.RiderSearchProfileDto;
import com.scb.rider.model.dto.SearchResponseDto;
import com.scb.rider.model.enumeration.RiderJobStatus;
import com.scb.rider.repository.RiderJobDetailsRepository;
import com.scb.rider.repository.RiderSearchRepository;
import com.scb.rider.repository.RiderTrainingAppointmentRepository;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@SuppressWarnings("squid:S2259")
public class RiderSearchService {


  private static final String ENROLLMENT_DATE = "enrollmentDate";

  private static final String MANDATORY_LAST_NAME = "mandatoryLastName";

  private static final String MANDATORY_FIRST_NAME = "mandatoryFirstName";

  private static final String FIRST_NAME = "firstName";

  private static final String RIDER_ON_ACTIVE_JOB = "riderOnActiveJob";

  private static final String RIDER_ON_TRAINING_TODAY = "riderOnTrainingToday";

  private static final String ALL_RIDERS = "allRiders";

  private static final String SUSPENDED = "suspended";

  private static final String UN_AUTHORIZED = "unAuthorized";

  private static final String AUTHORIZED = "authorized";

  private static final String LAST_NAME = "lastName";

  private static final String STATUS = "status";

  private static final String PHONENUMBER = "phoneNumber";

  private static final String RIDER_ID = "riderId";

  private static final String NAME = "name";

  private static final String IS_READY_FOR_AUTHORIZATION = "isReadyForAuthorization";

  private static final String APPOINTMENT_ID = "appointmentId";
  
  private static final String READY_TH = "\u0E1E\u0E23\u0E49\u0E2D\u0E21";
  
  private static final String NOT_READY_TH = "\u0E22\u0E31\u0E07\u0E44\u0E21\u0E48\u0E1E\u0E23\u0E49\u0E2D\u0E21";
  
  private static final String READY = "ready";
  
  private static final String NOT_READY = "not ready yet";
  
  private static final String TIERNAME = "tierName";
  
  private static final String APPROVAL_DATE_TIME = "approvalDateTime";
  
  private static final String PREFERRED_ZONE_NAME = "preferredZoneName";

  @Value("${rider.profile.database.sequence.prefix}")
  private String prefix;

  
  @Autowired
  private RiderSearchRepository riderSearchRepository;

  @Autowired
  private RiderTrainingAppointmentRepository riderTrainingAppointmentRepository;

  @Autowired
  private RiderJobDetailsRepository riderJobDetailsRepository;

  public SearchResponseDto getRiderProfileBySearchTermWithFilterQuery(String query,
      List<String> filterquery, Pageable pageable) {

    query = query.trim();

    Page<RiderProfile> pageRiderProfile = null;
    List<RiderProfile> riderProfiles;

    if (pageable.getSort().isSorted()) {
      List<Order> orders = getSortedOrderList(pageable);
      pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
    }


    if (StringUtils.isNotEmpty(query)) {
      log.info("Search By Query text");
      pageRiderProfile = getRiderByFreeTextSearch(query, pageable);
    } else {
      log.info("Search By Filter query", filterquery);
      Map<String, String> filtersQuery = new HashMap<String, String>();
      if (!CollectionUtils.isEmpty(filterquery)) {
        filterquery.stream().forEach(filter -> {
          String filterValue[] = filter.split(":");
          if (filterValue.length >= 2) {
            filtersQuery.put(filterValue[0], filterValue[1]);
          }
        });
        pageRiderProfile = getRidersByStatusAndColumnLevelQuery(filtersQuery, query, pageable);
      }
    }

    if (ObjectUtils.isEmpty(pageRiderProfile) || !pageRiderProfile.hasContent()) {
      List<RiderSearchProfileDto> emptyRiderSearchProfileDto = new ArrayList<>();
      log.error("Record not found for Query " + query);
      return SearchResponseDto.of(emptyRiderSearchProfileDto, 0, 0, 0);
    }

    riderProfiles = pageRiderProfile.getContent();
    log.debug(String.format("Query Searched - %s", query));


    return SearchResponseDto.of(RiderSearchProfileDto.of(riderProfiles),
        pageRiderProfile.getTotalPages(), pageRiderProfile.getTotalElements(),
        pageRiderProfile.getNumber() + 1);
  }


  private Page<RiderProfile> getRidersByStatusAndColumnLevelQuery(Map<String, String> filtersQuery,
      String query, Pageable pageable) {

    Page<RiderProfile> pageRiderProfile = null;
    String viewStatus = filtersQuery.getOrDefault("viewby", "");

    log.debug(String.format("ViewStatus  - %s", viewStatus));

    if (!StringUtils.isEmpty(viewStatus)) {
      switch (viewStatus) {
        case AUTHORIZED:
        case UN_AUTHORIZED:
        case SUSPENDED:
          pageRiderProfile =
              getRiderByStatusAndColumnLevelQuery(filtersQuery, viewStatus, pageable);
          break;
        case ALL_RIDERS:
          pageRiderProfile = getRiderByStatusAndColumnLevelQuery(filtersQuery, "", pageable);
          break;
        case RIDER_ON_TRAINING_TODAY:
        case RIDER_ON_ACTIVE_JOB:
          pageRiderProfile = getRiderOnTrainingTodayOrOnActiveJobByColumnLevelQuery(filtersQuery,
              viewStatus, pageable);
          break;
        case APPOINTMENT_ID:
          pageRiderProfile = getRiderByAppointmentIdByColumnLevelQuery(filtersQuery, viewStatus, pageable);
          break;
        default:
          pageRiderProfile = getRiderByFreeTextSearch(query, pageable);
          break;
      }

    }

    return pageRiderProfile;
  }

  public Page<RiderProfile> getRiderByAppointmentIdByColumnLevelQuery(
      Map<String, String> filtersQuery, String viewStatus, Pageable pageable) {

    String appointmentId = extractValue(filtersQuery, APPOINTMENT_ID);
    String name = extractValue(filtersQuery, NAME);
    String riderId = extractValue(filtersQuery, RIDER_ID);
    String phoneNumber = extractValue(filtersQuery, PHONENUMBER);
    String status = extractValue(filtersQuery, STATUS);
    String tierName = extractValue(filtersQuery, TIERNAME);
    String firstName = getFirstNameAndLastName(name, FIRST_NAME); 
    String lastName = getFirstNameAndLastName(name, LAST_NAME); 
    String mandatoryFirstName = getFirstNameAndLastName(name, MANDATORY_FIRST_NAME); 
    String mandatoryLastName = getFirstNameAndLastName(name, MANDATORY_LAST_NAME);
    String preferredZoneName = extractValue(filtersQuery, PREFERRED_ZONE_NAME);
    String enrollmentDate = extractValue(filtersQuery, ENROLLMENT_DATE);
    
    AggregationResults<RiderIdsOnTrainingOrActiveJobCount> riderIdsResult = null;
    List<RiderSelectedTrainingAppointment> list = new ArrayList<>();

    if (!StringUtils.isEmpty(viewStatus) && viewStatus.equals(APPOINTMENT_ID)) {
      list = riderTrainingAppointmentRepository.findByAppointmentId(appointmentId);
    }

     

    List<String> riderIds = new ArrayList<>();

    list.forEach(rider -> riderIds.add(rider.getRiderId()));

    log.debug(
        String.format("Rider Ids size for Training Slot - %s", Integer.toString(riderIds.size())));

    return riderSearchRepository.getRidersByRiderIdsAndQueryOnFields(firstName, lastName, riderId,
        phoneNumber, status, riderIds, mandatoryFirstName, mandatoryLastName, tierName, preferredZoneName, 
        enrollmentDate, pageable);
  }

  public String getReadyForAuthorizedString(Map<String, String> filtersQuery) {
    String isReadyToAuthorized = extractValue(filtersQuery, IS_READY_FOR_AUTHORIZATION);
    if(StringUtils.isNotBlank(isReadyToAuthorized)) {
      if(READY.contains(isReadyToAuthorized.toLowerCase()) || READY_TH.contains(isReadyToAuthorized)) {
        isReadyToAuthorized = Boolean.TRUE.toString();
      } else if(NOT_READY.contains(isReadyToAuthorized.toLowerCase()) || NOT_READY_TH.contains(isReadyToAuthorized)) {
        isReadyToAuthorized = Boolean.FALSE.toString();
      }
    }
    return isReadyToAuthorized;
  }

  public String getFirstNameAndLastName(String name, String columnName) {
    String firstName = name, lastName = name, mandatoryFirstName = "", mandatoryLastName = "";
    if (name.split(" ").length > 1) {
      String nameList[] = name.split(" ");
      firstName = nameList[0];
      mandatoryFirstName = nameList[0];
      lastName = Arrays.stream(nameList).skip(1).collect(Collectors.joining(" "));
      mandatoryLastName = lastName;
    }
    switch (columnName) {
      case FIRST_NAME:
        return firstName;
      case LAST_NAME:
        return lastName;
      case MANDATORY_FIRST_NAME:
        return mandatoryFirstName;
      case MANDATORY_LAST_NAME:
        return mandatoryLastName;
    }
    return "";
  }

  public Page<RiderProfile> getRiderOnTrainingTodayOrOnActiveJobByColumnLevelQuery(
      Map<String, String> filtersQuery, String viewStatus, Pageable pageable) {

    String name = extractValue(filtersQuery, NAME);
    String riderId = extractValue(filtersQuery, RIDER_ID);
    String phoneNumber = extractValue(filtersQuery, PHONENUMBER);
    String status = extractValue(filtersQuery, STATUS);
    String date = extractValue(filtersQuery, "date");
    String tierName = extractValue(filtersQuery, TIERNAME);
    String firstName = getFirstNameAndLastName(name, FIRST_NAME); 
    String lastName = getFirstNameAndLastName(name, LAST_NAME); 
    String mandatoryFirstName = getFirstNameAndLastName(name, MANDATORY_FIRST_NAME); 
    String mandatoryLastName = getFirstNameAndLastName(name, MANDATORY_LAST_NAME);
    String preferredZoneName = extractValue(filtersQuery, PREFERRED_ZONE_NAME);
    String enrollmentDate = extractValue(filtersQuery, ENROLLMENT_DATE);
    
    LocalDate parsedDate = getFormattedDate(date);

    AggregationResults<RiderIdsOnTrainingOrActiveJobCount> riderIdsResult = null;

    riderIdsResult = !StringUtils.isEmpty(viewStatus) && viewStatus.equals(RIDER_ON_TRAINING_TODAY)
        ? (riderTrainingAppointmentRepository.getRiderIdgroupByTodaysDate(parsedDate))
        : (riderJobDetailsRepository
            .getRiderIdgroupByActiveJob(RiderJobStatus.getActiveRiderJobStatuses()));

    List<String> riderIds = new ArrayList<String>();

    riderIdsResult.forEach(System.out::println);

    riderIdsResult.getMappedResults().stream()
        .forEach(riderObj -> riderIds.addAll(riderObj.getRiderIds()));

    log.debug(String.format("Rider Ids size for Training or Active Filter - %s",
        Integer.toString(riderIds.size())));


    return riderSearchRepository.getRidersByRiderIdsAndQueryOnFields(firstName, lastName, riderId,
        phoneNumber, status, riderIds, mandatoryFirstName, mandatoryLastName, tierName, preferredZoneName, 
        enrollmentDate, pageable);
  }

  private LocalDate getFormattedDate(String date) {

    LocalDate localDate = LocalDate.now(ZoneId.of("Asia/Bangkok"));
    log.debug(String.format("Training Date Filter - %s", date));
    try {
      localDate = !StringUtils.isEmpty(date) ? LocalDate.parse(date) : localDate;
    } catch (DateTimeParseException ex) {
      throw new InvalidInputException("Invalid Date Format");
    }
    return localDate;
  }

  private Page<RiderProfile> getRiderByFreeTextSearch(String query, Pageable pageable) {

    String firstName = getFirstNameAndLastName(query, FIRST_NAME); 
    String lastName = getFirstNameAndLastName(query, LAST_NAME); 
    String mandatoryFirstName = getFirstNameAndLastName(query, MANDATORY_FIRST_NAME); 
    String mandatoryLastName = getFirstNameAndLastName(query, MANDATORY_LAST_NAME);
   
    return riderSearchRepository.findRiderByTerm(query, firstName, lastName, mandatoryFirstName,
        mandatoryLastName, pageable);

  }

  private Page<RiderProfile> getRiderByStatusAndColumnLevelQuery(Map<String, String> filtersQuery,
      String viewStatus, Pageable pageable) {

    String name = extractValue(filtersQuery, NAME);
    String riderId = extractValue(filtersQuery, RIDER_ID);
    String phoneNumber = extractValue(filtersQuery, PHONENUMBER);
    String status = extractValue(filtersQuery, STATUS);
    String tierName = extractValue(filtersQuery, TIERNAME);
    String isReadyToAuthorized = getReadyForAuthorizedString(filtersQuery);
    String approvalDateTime = extractValue(filtersQuery, APPROVAL_DATE_TIME);
    String preferredZoneName = extractValue(filtersQuery, PREFERRED_ZONE_NAME);

    String firstName = getFirstNameAndLastName(name, FIRST_NAME); 
    String lastName = getFirstNameAndLastName(name, LAST_NAME); 
    String mandatoryFirstName = getFirstNameAndLastName(name, MANDATORY_FIRST_NAME); 
    String mandatoryLastName = getFirstNameAndLastName(name, MANDATORY_LAST_NAME);
    String enrollmentDate = extractValue(filtersQuery, ENROLLMENT_DATE);


    return riderSearchRepository.getRidersByStatusAndQueryOnFields(firstName, lastName, riderId,
            phoneNumber, status, viewStatus, mandatoryFirstName, mandatoryLastName,
            isReadyToAuthorized, approvalDateTime, preferredZoneName, tierName, enrollmentDate, pageable);
  }

  public List<Order> getSortedOrderList(Pageable pageable) {
    List<Order> orders = new ArrayList<Order>();

    pageable.getSort().forEach(sortOrder -> {
      String propertyField = sortOrder.getProperty();
      if (propertyField.equals(NAME)) {
        orders.add(getSortedField(propertyField, sortOrder.getDirection()));
        propertyField = LAST_NAME;
      }
      orders.add(getSortedField(propertyField, sortOrder.getDirection()));
    });
    return orders;
  }

  private String extractValue(Map<String, String> filtersQuery, String key) {

    return filtersQuery.getOrDefault(key, "");
  }

  private Order getSortedField(String fieldName, Direction Direction) {
    Map<String, String> fields = new HashMap<>();
    fields.put(NAME, FIRST_NAME);
    String dbField = fields.getOrDefault(fieldName, fieldName);
    return Direction == Sort.Direction.ASC ? new Order(Sort.Direction.ASC, dbField)
        : new Order(Sort.Direction.DESC, dbField);
  }
  
  public Optional<RiderProfile> findRiderProfileByRiderIdAndPhoneNumber(String id, String phoneNumber){
    if(id.startsWith(prefix)) {
      return riderSearchRepository.findByRiderIdAndPhoneNumber(id, phoneNumber); 
    }
    return riderSearchRepository.findByIdAndPhoneNumber(id, phoneNumber);
  }
}
