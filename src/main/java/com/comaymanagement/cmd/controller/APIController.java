package com.comaymanagement.cmd.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.poi.util.IOUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.comaymanagement.cmd.entity.ResponseObject;
import com.comaymanagement.cmd.service.APIService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class APIController {
	@Autowired
	ServletContext context;

	@PostMapping("/upload-images")
	@ResponseBody
	public ResponseEntity<Object> uploadFile(MultipartHttpServletRequest request) {
		String base64Result = "";
//		// Lay r ds ten file
		MultiValueMap<String, MultipartFile> form = request.getMultiFileMap();
		List<MultipartFile> files = form.get("image");
		StringBuilder pathSaveFile = new StringBuilder(System.getProperty("user.dir"));
		Map<String, String> result = new LinkedHashMap<>();
		for (MultipartFile mpf : files) {
			if (mpf.getOriginalFilename().equals("")) {
				continue;
			}
//				B1: lay ra duong dan se luu file
			pathSaveFile.append("/image/");

//				pathSaveFile = "/com/comaymanagement/cmd/image/";
			// Get extension
			String[] extensions = mpf.getOriginalFilename().split("\\.");
			StringBuilder ext = new StringBuilder(".").append(extensions[extensions.length - 1]);
			String name = String.format("%s_%s", new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date().getTime()),
					RandomStringUtils.randomAlphanumeric(5) + ext);
//				B2: Tao file
			File file = new File(pathSaveFile + name);
//				File file = new File(pathSaveFile + mpf.getOriginalFilename());
			System.out.println("Path save file: " + pathSaveFile);
//			B3: dung ham trong thu vien commmon de save
			try {
				mpf.transferTo(file);
				base64Result = APIService.convertToBase64(name);
				result.put("name", name);
				result.put("base64Data", APIService.convertToBase64(base64Result));
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", "", result));
			} catch (IOException e) {
				System.err.println(e.getMessage());
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseObject("OK", e.getMessage(), mpf.getOriginalFilename()));
			}
		}
		return ResponseEntity.status(HttpStatus.OK).body(new ResponseObject("OK", "No image to save", ""));

	}

	@GetMapping(value = "/get-image/{name}", produces = MediaType.IMAGE_JPEG_VALUE)
    public @ResponseBody byte[] getImageWithMediaType(
    		@PathVariable String name
    		) throws IOException {
		StringBuilder baseURL = new StringBuilder(System.getProperty("user.dir")).append("/image/");
//		File image = new File(baseURL + name.trim());
		final InputStream in = new BufferedInputStream(new FileInputStream(baseURL + name.trim())); 
//        final InputStream in = getClass().getResourceAsStream(baseURL + name.trim());
        return IOUtils.toByteArray(in);
    }
	
}
