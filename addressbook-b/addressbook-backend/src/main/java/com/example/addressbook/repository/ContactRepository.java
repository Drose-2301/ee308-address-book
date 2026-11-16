package com.example.addressbook.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.addressbook.entity.Contact;

public interface ContactRepository extends JpaRepository<Contact, Long> {

	/**
	 * One query powers the whole list view: keyword search over name / company / position /
	 * any contact method value, an optional "bookmarked only" filter, bookmarks first.
	 */
	@Query("""
			select distinct c from Contact c left join c.methods m
			where (:keyword = '' or lower(c.name) like :keyword
			       or lower(coalesce(c.company, '')) like :keyword
			       or lower(coalesce(c.position, '')) like :keyword
			       or lower(coalesce(m.value, '')) like :keyword)
			  and (:favoriteOnly = false or c.favorite = true)
			order by c.favorite desc, c.name asc
			""")
	List<Contact> search(@Param("keyword") String keyword, @Param("favoriteOnly") boolean favoriteOnly);

	long countByFavoriteTrue();
}
