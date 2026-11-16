package com.example.addressbook.entity;

/**
 * The kinds of contact information a single contact can carry.
 * Stored as a string so the front end and the Excel exporter share the same vocabulary.
 */
public enum MethodType {
	PHONE,
	EMAIL,
	WECHAT,
	QQ,
	OTHER
}
