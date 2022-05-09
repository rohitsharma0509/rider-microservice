package com.scb.rider.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

public class CustomMultipartFile implements MultipartFile {

private final byte[] fileContent;

private String fileName;

private String contentType;

private File file;

private String destPath = System.getProperty("java.io.tmpdir");

private FileOutputStream fileOutputStream;

public CustomMultipartFile(byte[] fileData, String name) {
    this.fileContent = fileData;
    this.fileName = name;
    file = new File(destPath + fileName);

}

@Override
public void transferTo(File dest) throws IOException, IllegalStateException {
    fileOutputStream = new FileOutputStream(dest);
    fileOutputStream.write(fileContent);
}

public void clearOutStreams() throws IOException {
if (null != fileOutputStream) {
        fileOutputStream.flush();
        fileOutputStream.close();
        file.deleteOnExit();
    }
}

@Override
public byte[] getBytes() throws IOException {
    return fileContent;
}

@Override
public InputStream getInputStream() throws IOException {
    return new ByteArrayInputStream(fileContent);
}

@Override
public String getName() {
	return fileName;
}

@Override
public String getOriginalFilename() {
	return fileName;
}

@Override
public String getContentType() {
	return null;
}

@Override
public boolean isEmpty() {
    return fileContent == null || fileContent.length == 0;
}

@Override
public long getSize() {
	return fileContent.length;

}
}