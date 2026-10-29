package com.shop.member.dto;

public record MemberRegisterRequest(
        String email,
        String password,
        String name,
        String picture
) {
}
