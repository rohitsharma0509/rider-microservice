package com.scb.rider.model.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.index.Indexed;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiderPreferredZones implements Serializable {
	
	private static final long serialVersionUID = -3172767058261216682L;

	@Indexed
	private String preferredZoneId;
	private String preferredZoneName;
	private String updatedBy;
}


