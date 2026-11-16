package com.example.addressbook.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.addressbook.dto.ContactDto;
import com.example.addressbook.dto.ContactRequest;
import com.example.addressbook.dto.ImportResult;
import com.example.addressbook.dto.MethodDto;
import com.example.addressbook.dto.StatsDto;
import com.example.addressbook.entity.Contact;
import com.example.addressbook.entity.ContactMethod;
import com.example.addressbook.entity.MethodType;
import com.example.addressbook.exception.BadRequestException;
import com.example.addressbook.exception.NotFoundException;
import com.example.addressbook.excel.ExcelService;
import com.example.addressbook.repository.ContactRepository;

/** Business logic for the address book: CRUD, bookmarking, stats and Excel transfer. */
@Service
@Transactional
public class ContactService {

	private final ContactRepository repository;

	private final ExcelService excelService;

	public ContactService(ContactRepository repository, ExcelService excelService) {
		this.repository = repository;
		this.excelService = excelService;
	}

	@Transactional(readOnly = true)
	public List<ContactDto> list(String keyword, boolean favoriteOnly) {
		String normalized = keyword == null ? "" : keyword.trim().toLowerCase();
		String pattern = "%" + normalized + "%";
		return repository.search(pattern, favoriteOnly).stream().map(ContactService::toDto).toList();
	}

	@Transactional(readOnly = true)
	public ContactDto get(Long id) {
		return toDto(find(id));
	}

	public ContactDto create(ContactRequest request) {
		Contact contact = new Contact();
		apply(contact, request);
		return toDto(repository.save(contact));
	}

	public ContactDto update(Long id, ContactRequest request) {
		Contact contact = find(id);
		apply(contact, request);
		return toDto(repository.save(contact));
	}

	public ContactDto setFavorite(Long id, boolean favorite) {
		Contact contact = find(id);
		contact.setFavorite(favorite);
		return toDto(repository.save(contact));
	}

	public void delete(Long id) {
		if (!repository.existsById(id)) {
			throw new NotFoundException("联系人不存在：id=" + id);
		}
		repository.deleteById(id);
	}

	@Transactional(readOnly = true)
	public StatsDto stats() {
		return new StatsDto(repository.count(), repository.countByFavoriteTrue());
	}

	@Transactional(readOnly = true)
	public byte[] exportExcel() {
		return excelService.export(list("", false));
	}

	public ImportResult importExcel(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BadRequestException("请选择一个 Excel 文件");
		}
		ExcelService.ParseResult parsed;
		try {
			parsed = excelService.parse(file.getInputStream());
		}
		catch (java.io.IOException e) {
			throw new BadRequestException("读取上传文件失败：" + e.getMessage());
		}

		// 去重：以「姓名 + 首要联系方式」为键，库中已有的和本文件内已出现的都算重复。
		Set<String> knownKeys = new HashSet<>();
		repository.findAll().forEach(existing -> {
			String key = dedupeKey(existing);
			if (key != null) {
				knownKeys.add(key);
			}
		});

		List<Contact> toSave = new ArrayList<>();
		int duplicates = 0;
		for (ExcelService.ParsedContact row : parsed.contacts()) {
			String key = dedupeKey(row.contact());
			if (key != null && !knownKeys.add(key)) {
				duplicates++;
				parsed.messages().add("第 " + row.rowNumber() + " 行：与已有联系人重复（姓名 + 首要联系方式），已跳过");
				continue;
			}
			toSave.add(row.contact());
		}

		repository.saveAll(toSave);
		return new ImportResult(parsed.totalRows(), toSave.size(), parsed.skipped() + duplicates,
				parsed.messages());
	}

	/**
	 * Key used to detect duplicates: normalised name plus the primary phone number (falling back
	 * to the first contact method). Returns {@code null} when there is nothing to compare, so a
	 * contact with neither a phone nor any other value is always imported instead of being dropped.
	 */
	private String dedupeKey(Contact contact) {
		if (contact.getName() == null || contact.getName().isBlank()) {
			return null;
		}
		String value = contact.getMethods().stream()
				.filter(method -> method.getType() == MethodType.PHONE)
				.map(ContactMethod::getValue)
				.filter(Objects::nonNull)
				.map(String::trim)
				.filter(v -> !v.isEmpty())
				.findFirst()
				.orElseGet(() -> contact.getMethods().stream()
						.map(ContactMethod::getValue)
						.filter(Objects::nonNull)
						.map(String::trim)
						.filter(v -> !v.isEmpty())
						.findFirst()
						.orElse(""));
		if (value.isEmpty()) {
			return null;
		}
		return contact.getName().trim().toLowerCase(Locale.ROOT) + "|" + value.replaceAll("[\\s\\-()]", "");
	}

	// ------------------------------------------------------------------ helpers

	private Contact find(Long id) {
		return repository.findById(id)
				.orElseThrow(() -> new NotFoundException("联系人不存在：id=" + id));
	}

	/** Copies the request onto the entity, rebuilding the method list element by element. */
	private void apply(Contact contact, ContactRequest request) {
		if (request.name() == null || request.name().isBlank()) {
			throw new BadRequestException("姓名不能为空");
		}
		contact.setName(request.name().trim());
		contact.setCompany(trimToNull(request.company()));
		contact.setPosition(trimToNull(request.position()));
		contact.setAddress(trimToNull(request.address()));
		contact.setNotes(trimToNull(request.notes()));
		contact.setFavorite(Boolean.TRUE.equals(request.favorite()));

		contact.clearMethods();
		if (request.methods() != null) {
			for (ContactRequest.MethodRequest method : request.methods()) {
				if (method == null || method.value() == null || method.value().isBlank()) {
					continue;
				}
				contact.addMethod(new ContactMethod(parseType(method.type()),
						trimToNull(method.label()), method.value().trim()));
			}
		}
	}

	private MethodType parseType(String raw) {
		if (raw == null || raw.isBlank()) {
			return MethodType.OTHER;
		}
		try {
			return MethodType.valueOf(raw.trim().toUpperCase());
		}
		catch (IllegalArgumentException e) {
			return MethodType.OTHER;
		}
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	public static ContactDto toDto(Contact contact) {
		List<MethodDto> methods = contact.getMethods().stream()
				.map(m -> new MethodDto(m.getId(), m.getType().name(), m.getLabel(), m.getValue()))
				.toList();
		return new ContactDto(contact.getId(), contact.getName(), contact.getCompany(),
				contact.getPosition(), contact.getAddress(), contact.getNotes(),
				contact.isFavorite(), contact.getCreatedAt(), contact.getUpdatedAt(), methods);
	}
}
