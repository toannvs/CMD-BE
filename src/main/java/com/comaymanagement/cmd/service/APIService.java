package com.comaymanagement.cmd.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.stereotype.Service;

import com.comaymanagement.cmd.constant.CMDConstrant;

@Service
public class APIService {
	public static String convertToBase64(String fileName) throws IOException {
		StringBuilder baseURL = new StringBuilder(CMDConstrant.IMAGE_FOLDER).append("/image/");
		byte[] data = null;
		try {
			InputStream in = new FileInputStream(baseURL + fileName);
			System.out.println("file size (bytes)=" + in.available());
			data = new byte[in.available()];
			in.read(data);
			in.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return "";
		}
		// Base64 encoded by byte arrays with a string of Base64 encoded
		return new String(Objects.requireNonNull(Base64.encodeBase64(data)));
	}
}
