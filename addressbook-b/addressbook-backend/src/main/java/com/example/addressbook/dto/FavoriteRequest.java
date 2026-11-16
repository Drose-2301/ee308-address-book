package com.example.addressbook.dto;

/** Body of PATCH /api/contacts/{id}/favorite. */
public record FavoriteRequest(boolean favorite) {
}
