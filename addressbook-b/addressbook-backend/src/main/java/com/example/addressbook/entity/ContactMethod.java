package com.example.addressbook.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * One piece of contact information (a phone number, an e-mail address, ...).
 * A contact owns a list of these, which is how "multiple contact methods" is modelled.
 */
@Entity
@Table(name = "contact_methods")
public class ContactMethod {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "contact_id", nullable = false)
	private Contact contact;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private MethodType type = MethodType.OTHER;

	/** Free-form label such as "手机" or "工作邮箱"; optional. */
	@Column(length = 40)
	private String label;

	@Column(nullable = false, length = 200)
	private String value = "";

	public ContactMethod() {
	}

	public ContactMethod(MethodType type, String label, String value) {
		this.type = type;
		this.label = label;
		this.value = value;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}

	public MethodType getType() {
		return type;
	}

	public void setType(MethodType type) {
		this.type = type;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
