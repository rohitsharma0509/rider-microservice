package com.scb.rider.model.document;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.scb.rider.view.View;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@Document
@NoArgsConstructor
@AllArgsConstructor
public class RiderRemarksDetails {
    @Id
    @JsonView(value = {View.RiderRemark.RESPONSE.class})
    private String id;
    
    @JsonView(value = {View.RiderRemark.RESPONSE.class})
    @Indexed
    private String riderId;
    
    @JsonView(value = {View.RiderRemark.REQUEST.class, View.RiderRemark.RESPONSE.class})
    @NotBlank(message = "{api.rider.profile.blank.msg}")
	@JsonFormat(pattern="yyyy-MM-dd", timezone = JsonFormat.DEFAULT_LOCALE)
    private LocalDate date;
    
    @JsonView(value = {View.RiderRemark.REQUEST.class, View.RiderRemark.RESPONSE.class})
    @NotBlank(message = "{api.rider.profile.blank.msg}")
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="HH:mm",timezone = JsonFormat.DEFAULT_LOCALE)
    private LocalTime time;
    
    @JsonIgnore
    private String timeSearch; // for search
    @JsonIgnore
    private String dateSearch; // for search
    
    
    @JsonView(value = {View.RiderRemark.REQUEST.class, View.RiderRemark.RESPONSE.class})
    @NotBlank(message = "{api.rider.profile.blank.msg}")
    @Size(max = 500, message = "{api.rider.profile.length.msg}")
    private String remark;

}
