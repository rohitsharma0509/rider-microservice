package com.scb.rider.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.scb.rider.model.document.RiderPreferredZones;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import lombok.*;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString@Api(value = "PreferredZone")
@ApiModel(value = "PreferredZone")
public class RiderPreferredZoneDto {
	@NotBlank(message = "{api.rider.profile.blank.msg}")
	@Size(max = 40, message = "{api.rider.profile.length.msg}")
	private String riderProfileId;
	@NotBlank(message = "{api.rider.profile.blank.msg}")
	@Size(max = 40, message = "{api.rider.profile.length.msg}")
	private String preferredZoneId;
	@NotBlank(message = "{api.rider.profile.blank.msg}")
	@Size(max = 40, message = "{api.rider.profile.length.msg}")
	private String preferredZoneName;
	@JsonIgnore
	private String updatedBy;

	public static RiderPreferredZoneDto of(RiderPreferredZones zones) {
		RiderPreferredZoneDto zoneDto = RiderPreferredZoneDto.builder().build();
		BeanUtils.copyProperties(zones, zoneDto);
		return zoneDto;
	}

}
