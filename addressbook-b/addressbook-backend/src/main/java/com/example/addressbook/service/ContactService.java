package com.example.addressbook.service;

import java.util.List;

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
		repository.saveAll(parsed.contacts());
		return new ImportResult(parsed.totalRows(), parsed.contacts().size(), parsed.skipped(),
				parsed.messages());
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
