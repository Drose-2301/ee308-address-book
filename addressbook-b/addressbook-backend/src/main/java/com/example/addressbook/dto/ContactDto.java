package com.example.addressbook.dto;

import java.time.Instant;
import java.util.List;

/** A contact plus every contact method attached to it. */
public record ContactDto(
		Long id,
		String name,
		String company,
		String position,
		String address,
		String notes,
		boolean favorite,
		Instant createdAt,
		Instant updatedAt,
		List<MethodDto> methods) {
}
