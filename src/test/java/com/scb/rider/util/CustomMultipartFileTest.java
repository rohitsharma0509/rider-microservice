package com.scb.rider.util;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amazonaws.services.s3.model.transform.XmlResponsesSaxParser.ListAllMyBucketsHandler;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
class CustomMultipartFileTest {

	
	@Test
	public void testCopyProperties() throws IllegalStateException, IOException {

		byte[] imageByte = Base64.getDecoder().decode(
				"iVBORw0KGgoAAAANSUhEUgAAAvUAAAE4CAYAAADb3Ax6");

		CustomMultipartFile csm = new CustomMultipartFile(imageByte, "test");

		csm.transferTo(new File("test.jpg"));

		csm.clearOutStreams();
		csm.getBytes();
		csm.getInputStream();
		csm.getName();
		csm.getOriginalFilename();
		csm.getContentType();
		csm.isEmpty();
		csm.getSize();
		File myObj = new File("test.jpg"); 
		myObj.delete();

	}

}
