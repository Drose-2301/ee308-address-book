package com.example.addressbook.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * A single address book entry. It owns its contact methods (one-to-many, cascade all),
 * so removing a contact also removes every phone number / e-mail attached to it.
 */
@Entity
@Table(name = "contacts")
public class Contact {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(length = 100)
	private String company;

	@Column(length = 100)
	private String position;

	@Column(length = 300)
	private String address;

	@Column(length = 1000)
	private String notes;

	/** "Bookmark contact" feature: favourited contacts float to the top of the list. */
	@Column(nullable = false)
	private boolean favorite = false;

	@Column(nullable = false)
	private Instant createdAt = Instant.now();

	@Column(nullable = false)
	private Instant updatedAt = Instant.now();

	@OneToMany(mappedBy = "contact", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id ASC")
	private List<ContactMethod> methods = new ArrayList<>();

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = Instant.now();
	}

	public void addMethod(ContactMethod method) {
		method.setContact(this);
		this.methods.add(method);
	}

	public void clearMethods() {
		for (ContactMethod method : this.methods) {
			method.setContact(null);
		}
		this.methods.clear();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public boolean isFavorite() {
		return favorite;
	}

	public void setFavorite(boolean favorite) {
		this.favorite = favorite;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public List<ContactMethod> getMethods() {
		return methods;
	}

	public void setMethods(List<ContactMethod> methods) {
		this.methods = methods;
	}
}
