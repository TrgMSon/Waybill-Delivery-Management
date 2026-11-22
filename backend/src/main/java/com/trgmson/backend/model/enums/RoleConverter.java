package com.trgmson.backend.model.enums;

import com.trgmson.backend.exception.BadRequestException;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Role, String> {

    @Override
    public String convertToDatabaseColumn(Role role) {
        if (role == null) return null;
        return role.name().toLowerCase();
    }

    @Override
    public Role convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return null;
        return switch (dbData.trim().toUpperCase()) {
            case "DRIVER" -> Role.DRIVER;
            case "ADMIN" -> Role.ADMIN;
            default -> throw new BadRequestException("Vai trò tài khoản không hợp lệ: " + dbData);
        };
    }
}
