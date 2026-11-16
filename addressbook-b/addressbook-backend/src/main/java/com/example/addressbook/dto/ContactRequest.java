package com.example.addressbook.dto;

/** Incoming payload for creating or updating a contact. */
public record ContactRequest(
		String name,
		String company,
		String position,
		String address,
		String notes,
		Boolean favorite,
		java.util.List<MethodRequest> methods) {

	public record MethodRequest(String type, String label, String value) {
	}
}
