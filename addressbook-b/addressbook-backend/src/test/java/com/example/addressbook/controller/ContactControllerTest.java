package com.example.addressbook.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.addressbook.dto.ContactDto;
import com.example.addressbook.dto.MethodDto;
import com.example.addressbook.service.ContactService;

/**
 * Web-layer tests for {@link ContactController}: the service is mocked, so these tests only
 * check routing, status codes and the JSON contract the Vue front end relies on.
 */
@WebMvcTest(ContactController.class)
class ContactControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ContactService contactService;

	private ContactDto sample() {
		return new ContactDto(1L, "林晓", "福大科技", "产品经理", "福州", "备注", true,
				Instant.parse("2026-11-16T00:00:00Z"), Instant.parse("2026-11-16T00:00:00Z"),
				List.of(new MethodDto(1L, "PHONE", "手机", "13800138000"),
						new MethodDto(2L, "EMAIL", "工作邮箱", "linxiao@fzu-tech.com")));
	}

	@Test
	void listReturnsContactsWithMethods() throws Exception {
		given(contactService.list(eq(""), eq(false))).willReturn(List.of(sample()));

		mockMvc.perform(get("/api/contacts"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("林晓"))
				.andExpect(jsonPath("$[0].favorite").value(true))
				.andExpect(jsonPath("$[0].methods[0].type").value("PHONE"))
				.andExpect(jsonPath("$[0].methods[1].value").value("linxiao@fzu-tech.com"));
	}

	@Test
	void listPassesSearchAndFavoriteFilters() throws Exception {
		given(contactService.list(eq("福"), eq(true))).willReturn(List.of());

		mockMvc.perform(get("/api/contacts").param("keyword", "福").param("favorite", "true"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}

	@Test
	void createReturns201WithLocationOfNewContact() throws Exception {
		given(contactService.create(any())).willReturn(sample());

		String body = """
				{"name":"林晓","company":"福大科技","favorite":true,
				 "methods":[{"type":"PHONE","label":"手机","value":"13800138000"}]}
				""";
		mockMvc.perform(post("/api/contacts")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("林晓"));
	}

	@Test
	void createRejectsBlankName() throws Exception {
		given(contactService.create(any()))
				.willThrow(new com.example.addressbook.exception.BadRequestException("姓名不能为空"));

		mockMvc.perform(post("/api/contacts")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"  \"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("姓名不能为空"));
	}

	@Test
	void favoriteFlagIsToggledThroughPatch() throws Exception {
		given(contactService.setFavorite(1L, false)).willReturn(sample());

		mockMvc.perform(patch("/api/contacts/1/favorite")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"favorite\":false}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1));
	}

	@Test
	void deleteReturns204() throws Exception {
		willDoNothing().given(contactService).delete(1L);

		mockMvc.perform(delete("/api/contacts/1"))
				.andExpect(status().isNoContent());
	}

	@Test
	void statsReturnsCounts() throws Exception {
		given(contactService.stats()).willReturn(new com.example.addressbook.dto.StatsDto(5, 2));

		mockMvc.perform(get("/api/contacts/stats"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(5))
				.andExpect(jsonPath("$.favorites").value(2));
	}
}
