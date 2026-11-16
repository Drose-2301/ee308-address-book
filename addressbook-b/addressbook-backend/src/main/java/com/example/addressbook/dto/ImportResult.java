package com.example.addressbook.dto;

import java.util.List;

/** Summary returned after an Excel import run. */
public record ImportResult(
		int totalRows,
		int imported,
		int skipped,
		List<String> messages) {
}
