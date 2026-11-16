package com.example.addressbook.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tiny landing endpoint so opening http://localhost:8080 in a browser shows something useful
 * instead of a 404 page. It is not used by the UI, which talks to /api/** directly.
 */
@RestController
public class InfoController {

	@GetMapping("/")
	public Map<String, Object> index() {
		return Map.of(
				"application", "EE308 address book backend",
				"version", "1.0.0",
				"frontend", "http://localhost:5173",
				"endpoints", List.of(
						"GET    /api/contacts?keyword=&favorite=",
						"GET    /api/contacts/{id}",
						"POST   /api/contacts",
						"PUT    /api/contacts/{id}",
						"PATCH  /api/contacts/{id}/favorite",
						"DELETE /api/contacts/{id}",
						"GET    /api/contacts/stats",
						"GET    /api/contacts/export",
						"POST   /api/contacts/import"));
	}
}
