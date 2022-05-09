package com.scb.rider.model.pdpa;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Content {

	private String contentSubType;
	private String title;
	private String body;
	private String metaData;
}
