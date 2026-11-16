package com.example.addressbook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.example.addressbook.dto.ImportResult;
import com.example.addressbook.entity.Contact;
import com.example.addressbook.entity.ContactMethod;
import com.example.addressbook.entity.MethodType;
import com.example.addressbook.excel.ExcelService;
import com.example.addressbook.repository.ContactRepository;

/** Unit tests for the import bookkeeping in {@link ContactService} (dedupe + row messages). */
@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

	@Mock
	private ContactRepository repository;

	@Mock
	private ExcelService excelService;

	@InjectMocks
	private ContactService contactService;

	private Contact contact(String name, String phone) {
		Contact contact = new Contact();
		contact.setName(name);
		contact.addMethod(new ContactMethod(MethodType.PHONE, "手机", phone));
		return contact;
	}

	@Test
	void importSkipsRowsThatDuplicateAnExistingContact() throws Exception {
		Contact existing = contact("林晓", "13800138000");
		Contact duplicate = contact("林晓", "138-0013-8000");
		Contact fresh = contact("陈子墨", "13959201122");

		List<String> messages = new ArrayList<>();
		given(repository.findAll()).willReturn(List.of(existing));
		given(excelService.parse(any())).willReturn(new ExcelService.ParseResult(
				List.of(new ExcelService.ParsedContact(duplicate, 3),
						new ExcelService.ParsedContact(fresh, 4)),
				2, 0, messages));

		MockMultipartFile file = new MockMultipartFile("file", "addressbook.xlsx",
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "x".getBytes());

		ImportResult result = contactService.importExcel(file);

		assertThat(result.totalRows()).isEqualTo(2);
		assertThat(result.imported()).isEqualTo(1);
		assertThat(result.skipped()).isEqualTo(1);
		assertThat(result.messages()).anyMatch(message -> message.startsWith("第 3 行：") && message.contains("重复"));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<Contact>> captor = ArgumentCaptor.forClass(List.class);
		verify(repository).saveAll(captor.capture());
		assertThat(captor.getValue()).extracting(Contact::getName).containsExactly("陈子墨");
	}

	@Test
	void importDropsDuplicatesInsideTheSameFile() throws Exception {
		given(repository.findAll()).willReturn(List.of());
		given(excelService.parse(any())).willReturn(new ExcelService.ParseResult(
				List.of(new ExcelService.ParsedContact(contact("王雅琪", "15060012233"), 2),
						new ExcelService.ParsedContact(contact("王雅琪", "150 6001 2233"), 3)),
				2, 0, new ArrayList<>()));

		MockMultipartFile file = new MockMultipartFile("file", "addressbook.xlsx", "application/octet-stream",
				"x".getBytes());

		ImportResult result = contactService.importExcel(file);

		assertThat(result.imported()).isEqualTo(1);
		assertThat(result.skipped()).isEqualTo(1);
	}
}
