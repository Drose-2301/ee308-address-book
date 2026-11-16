package com.example.addressbook.dto;

/** One contact method as it travels over HTTP. */
public record MethodDto(
		Long id,
		String type,
		String label,
		String value) {
}
