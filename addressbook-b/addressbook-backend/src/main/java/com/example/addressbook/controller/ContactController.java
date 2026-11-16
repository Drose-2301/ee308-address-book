package com.example.addressbook.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.addressbook.dto.ContactDto;
import com.example.addressbook.dto.ContactRequest;
import com.example.addressbook.dto.FavoriteRequest;
import com.example.addressbook.dto.ImportResult;
import com.example.addressbook.dto.StatsDto;
import com.example.addressbook.service.ContactService;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

	private final ContactService service;

	public ContactController(ContactService service) {
		this.service = service;
	}

	@GetMapping
	public List<ContactDto> list(@RequestParam(defaultValue = "") String keyword,
			@RequestParam(defaultValue = "false") boolean favorite) {
		return service.list(keyword, favorite);
	}

	@GetMapping("/stats")
	public StatsDto stats() {
		return service.stats();
	}

	@GetMapping("/{id}")
	public ContactDto get(@PathVariable Long id) {
		return service.get(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ContactDto create(@RequestBody ContactRequest request) {
		return service.create(request);
	}

	@PutMapping("/{id}")
	public ContactDto update(@PathVariable Long id, @RequestBody ContactRequest request) {
		return service.update(id, request);
	}

	@PatchMapping("/{id}/favorite")
	public ContactDto setFavorite(@PathVariable Long id, @RequestBody FavoriteRequest request) {
		return service.setFavorite(id, request.favorite());
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		service.delete(id);
	}

	/** Downloads the whole address book as an .xlsx workbook. */
	@GetMapping("/export")
	public ResponseEntity<byte[]> export() {
		byte[] body = service.exportExcel();
		String fileName = "通讯录_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx";
		String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"addressbook.xlsx\"; filename*=UTF-8''" + encoded)
				.contentType(MediaType.parseMediaType(
						"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(body);
	}

	/** Imports contacts from an .xlsx / .xls workbook and reports what happened row by row. */
	@PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ImportResult importFile(@RequestParam("file") MultipartFile file) {
		return service.importExcel(file);
	}
}
