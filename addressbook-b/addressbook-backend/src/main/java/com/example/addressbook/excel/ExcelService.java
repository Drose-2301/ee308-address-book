package com.example.addressbook.excel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.example.addressbook.dto.ContactDto;
import com.example.addressbook.dto.MethodDto;
import com.example.addressbook.entity.Contact;
import com.example.addressbook.entity.ContactMethod;
import com.example.addressbook.entity.MethodType;
import com.example.addressbook.exception.BadRequestException;

/**
 * Excel import / export.
 *
 * <p>Layout: one row per contact, one column per kind of information. Several values of the
 * same kind (two phone numbers, for example) live in the same cell, separated by "；".
 * That keeps the sheet readable while still supporting "multiple contact methods".</p>
 */
@Service
public class ExcelService {

	public static final String[] HEADERS = {
			"姓名", "公司", "职位", "书签", "地址", "备注", "电话", "邮箱", "微信", "QQ", "其他联系方式"
	};

	private static final int MAX_ROWS = 5000;

	private static final String[] FAVORITE_TRUE = { "是", "y", "yes", "true", "1", "书签", "★" };

	private static final String[] SPLITTER = { "；", ";", "、", "\n", "\r" };

	/** Header aliases accepted on import, so hand-made sheets still work. */
	private static final Map<String, List<String>> ALIASES = new LinkedHashMap<>();

	static {
		ALIASES.put("name", List.of("姓名", "名字", "名称", "联系人", "name"));
		ALIASES.put("company", List.of("公司", "公司名称", "单位", "company"));
		ALIASES.put("position", List.of("职位", "职务", "岗位", "position", "title"));
		ALIASES.put("favorite", List.of("书签", "收藏", "书签联系人", "favorite", "bookmark"));
		ALIASES.put("address", List.of("地址", "住址", "通讯地址", "address"));
		ALIASES.put("notes", List.of("备注", "说明", "notes", "remark"));
		ALIASES.put("phone", List.of("电话", "手机", "手机号", "联系电话", "phone", "mobile", "tel"));
		ALIASES.put("email", List.of("邮箱", "邮件", "电子邮箱", "email", "mail"));
		ALIASES.put("wechat", List.of("微信", "wechat", "weixin"));
		ALIASES.put("qq", List.of("qq"));
		ALIASES.put("other", List.of("其他", "其他联系方式", "other"));
	}

	// ------------------------------------------------------------------ export

	public byte[] export(List<ContactDto> contacts) {
		try (XSSFWorkbook workbook = new XSSFWorkbook();
				ByteArrayOutputStream out = new ByteArrayOutputStream()) {

			Sheet sheet = workbook.createSheet("通讯录");
			CellStyle headerStyle = headerStyle(workbook);
			CellStyle bodyStyle = bodyStyle(workbook);

			Row header = sheet.createRow(0);
			header.setHeightInPoints(24);
			for (int i = 0; i < HEADERS.length; i++) {
				Cell cell = header.createCell(i);
				cell.setCellValue(HEADERS[i]);
				cell.setCellStyle(headerStyle);
			}

			int rowIndex = 1;
			for (ContactDto contact : contacts) {
				Row row = sheet.createRow(rowIndex++);
				row.setHeightInPoints(20);
				List<String> values = new ArrayList<>();
				values.add(contact.name());
				values.add(contact.company());
				values.add(contact.position());
				values.add(contact.favorite() ? "是" : "否");
				values.add(contact.address());
				values.add(contact.notes());
				values.add(join(contact, MethodType.PHONE));
				values.add(join(contact, MethodType.EMAIL));
				values.add(join(contact, MethodType.WECHAT));
				values.add(join(contact, MethodType.QQ));
				values.add(join(contact, MethodType.OTHER));

				for (int i = 0; i < values.size(); i++) {
					Cell cell = row.createCell(i);
					cell.setCellValue(values.get(i) == null ? "" : values.get(i));
					cell.setCellStyle(bodyStyle);
				}
			}

			int[] widths = { 14, 20, 16, 8, 34, 30, 22, 26, 16, 14, 26 };
			for (int i = 0; i < widths.length; i++) {
				sheet.setColumnWidth(i, widths[i] * 256);
			}
			sheet.createFreezePane(0, 1);

			workbook.write(out);
			return out.toByteArray();
		}
		catch (IOException e) {
			throw new IllegalStateException("生成 Excel 文件失败", e);
		}
	}

	private String join(ContactDto contact, MethodType type) {
		List<String> values = contact.methods().stream()
				.filter(m -> type.name().equals(m.type()))
				.map(MethodDto::value)
				.filter(v -> v != null && !v.isBlank())
				.toList();
		return String.join("；", values);
	}

	private CellStyle headerStyle(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setBold(true);
		font.setColor(IndexedColors.WHITE.getIndex());
		font.setFontHeightInPoints((short) 11);
		style.setFont(font);
		style.setFillForegroundColor(IndexedColors.INDIGO.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setBorderBottom(BorderStyle.THIN);
		return style;
	}

	private CellStyle bodyStyle(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setBorderBottom(BorderStyle.HAIR);
		style.setBorderRight(BorderStyle.HAIR);
		style.setWrapText(false);
		return style;
	}

	// ------------------------------------------------------------------ import

	/**
	 * Parses a workbook into contacts. Rows without a name are skipped and reported back to the
	 * caller, so one broken line does not reject the whole file.
	 */
	public ParseResult parse(InputStream inputStream) {
		try (Workbook workbook = WorkbookFactory.create(inputStream)) {
			if (workbook.getNumberOfSheets() == 0) {
				throw new BadRequestException("Excel 文件里没有工作表");
			}
			Sheet sheet = workbook.getSheetAt(0);
			Row headerRow = sheet.getRow(sheet.getFirstRowNum());
			if (headerRow == null) {
				throw new BadRequestException("Excel 文件是空的");
			}

			Map<String, Integer> columns = mapColumns(headerRow);
			if (!columns.containsKey("name")) {
				throw new BadRequestException("未找到“姓名”列，请参考导出的模板："
						+ String.join("、", HEADERS));
			}

			DataFormatter formatter = new DataFormatter();
			List<Contact> contacts = new ArrayList<>();
			List<String> messages = new ArrayList<>();
			int totalRows = 0;
			int skipped = 0;

			int lastRow = Math.min(sheet.getLastRowNum(), MAX_ROWS);
			for (int r = sheet.getFirstRowNum() + 1; r <= lastRow; r++) {
				Row row = sheet.getRow(r);
				if (row == null || isBlank(row, formatter)) {
					continue;
				}
				totalRows++;
				String name = cellText(row, columns.get("name"), formatter);
				if (name.isBlank()) {
					skipped++;
					messages.add("第 " + (r + 1) + " 行：姓名为空，已跳过");
					continue;
				}

				Contact contact = new Contact();
				contact.setName(name);
				contact.setCompany(cellText(row, columns.get("company"), formatter));
				contact.setPosition(cellText(row, columns.get("position"), formatter));
				contact.setAddress(cellText(row, columns.get("address"), formatter));
				contact.setNotes(cellText(row, columns.get("notes"), formatter));
				contact.setFavorite(isFavorite(cellText(row, columns.get("favorite"), formatter)));

				addMethods(contact, row, columns.get("phone"), MethodType.PHONE, formatter);
				addMethods(contact, row, columns.get("email"), MethodType.EMAIL, formatter);
				addMethods(contact, row, columns.get("wechat"), MethodType.WECHAT, formatter);
				addMethods(contact, row, columns.get("qq"), MethodType.QQ, formatter);
				addMethods(contact, row, columns.get("other"), MethodType.OTHER, formatter);
				contacts.add(contact);
			}

			if (contacts.isEmpty() && skipped == 0) {
				throw new BadRequestException("Excel 文件里没有可导入的数据行");
			}
			return new ParseResult(contacts, totalRows, skipped, messages);
		}
		catch (BadRequestException e) {
			throw e;
		}
		catch (Exception e) {
			throw new BadRequestException("无法解析 Excel 文件：" + e.getMessage());
		}
	}

	private Map<String, Integer> mapColumns(Row headerRow) {
		Map<String, Integer> columns = new LinkedHashMap<>();
		DataFormatter formatter = new DataFormatter();
		for (int i = headerRow.getFirstCellNum(); i < headerRow.getLastCellNum(); i++) {
			String raw = formatter.formatCellValue(headerRow.getCell(i));
			String normalized = normalize(raw);
			if (normalized.isEmpty()) {
				continue;
			}
			for (Map.Entry<String, List<String>> entry : ALIASES.entrySet()) {
				if (columns.containsKey(entry.getKey())) {
					continue;
				}
				for (String alias : entry.getValue()) {
					if (normalized.equals(normalize(alias))) {
						columns.put(entry.getKey(), i);
						break;
					}
				}
			}
		}
		return columns;
	}

	private void addMethods(Contact contact, Row row, Integer column, MethodType type,
			DataFormatter formatter) {
		String raw = cellText(row, column, formatter);
		if (raw.isBlank()) {
			return;
		}
		Set<String> seen = new LinkedHashSet<>(Arrays.asList(raw.split(String.join("|", SPLITTER))));
		for (String value : seen) {
			String trimmed = value.trim();
			if (!trimmed.isEmpty()) {
				contact.addMethod(new ContactMethod(type, null, trimmed));
			}
		}
	}

	private boolean isBlank(Row row, DataFormatter formatter) {
		for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
			if (!formatter.formatCellValue(row.getCell(i)).trim().isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private String cellText(Row row, Integer column, DataFormatter formatter) {
		if (column == null) {
			return "";
		}
		Cell cell = row.getCell(column);
		if (cell == null) {
			return "";
		}
		return formatter.formatCellValue(cell).trim();
	}

	private boolean isFavorite(String text) {
		String normalized = normalize(text);
		if (normalized.isEmpty()) {
			return false;
		}
		for (String candidate : FAVORITE_TRUE) {
			if (normalized.equals(normalize(candidate))) {
				return true;
			}
		}
		return false;
	}

	private String normalize(String text) {
		if (text == null) {
			return "";
		}
		return text.toLowerCase(Locale.ROOT).replaceAll("[\\s*_\\-]", "").trim();
	}

	/** Raw output of a parse run, turned into an {@code ImportResult} by the service layer. */
	public record ParseResult(List<Contact> contacts, int totalRows, int skipped, List<String> messages) {
	}
}
