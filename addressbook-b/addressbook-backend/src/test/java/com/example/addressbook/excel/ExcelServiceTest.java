package com.example.addressbook.excel;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.addressbook.dto.ContactDto;
import com.example.addressbook.dto.MethodDto;
import com.example.addressbook.entity.Contact;

/** Round-trip test: export an address book to .xlsx and read it back. */
class ExcelServiceTest {

	private final ExcelService excelService = new ExcelService();

	@Test
	void exportThenImportKeepsContactsAndMethods() {
		ContactDto contact = new ContactDto(1L, "林晓", "福大科技", "产品经理", "福州", "备注",
				true, Instant.now(), Instant.now(),
				List.of(new MethodDto(1L, "PHONE", "手机", "13800138000"),
						new MethodDto(2L, "PHONE", "座机", "0591-88886666"),
						new MethodDto(3L, "EMAIL", "工作邮箱", "linxiao@fzu-tech.com")));

		byte[] workbook = excelService.export(List.of(contact));
		assertThat(workbook).isNotEmpty();

		ExcelService.ParseResult parsed = excelService.parse(new ByteArrayInputStream(workbook));
		assertThat(parsed.totalRows()).isEqualTo(1);
		assertThat(parsed.skipped()).isZero();

		Contact imported = parsed.contacts().get(0).contact();
		assertThat(parsed.contacts().get(0).rowNumber()).isEqualTo(2);
		assertThat(imported.getName()).isEqualTo("林晓");
		assertThat(imported.isFavorite()).isTrue();
		assertThat(imported.getMethods()).hasSize(3);
		assertThat(imported.getMethods().stream().filter(m -> m.getType().name().equals("PHONE")))
				.hasSize(2);
	}

	@Test
	void contactWithoutMethodsRoundTrips() {
		ContactDto valid = new ContactDto(1L, "陈子墨", "", "", "", "", false,
				Instant.now(), Instant.now(), List.of());
		byte[] workbook = excelService.export(List.of(valid));

		ExcelService.ParseResult parsed = excelService.parse(new ByteArrayInputStream(workbook));
		assertThat(parsed.contacts()).hasSize(1);
		assertThat(parsed.contacts().get(0).contact().getMethods()).isEmpty();
	}
}
