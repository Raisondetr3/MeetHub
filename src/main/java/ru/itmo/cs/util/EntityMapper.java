package ru.itmo.cs.util;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.itmo.cs.dto.UserCreateDto;
import ru.itmo.cs.dto.UserDto;
import ru.itmo.cs.entity.User;

/**
 * Утилита для преобразования между сущностями и DTO.
 */
@Component
public class EntityMapper {

    private final PasswordEncoder passwordEncoder;

    public EntityMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Преобразует сущность User в DTO.
     *
     * @param user сущность User
     * @return UserDto
     */
    public UserDto toUserDto(User user) {
        return new UserDto(
            user.getId(),
            user.getUsername(),
            user.getEmail()
        );
    }

    /**
     * Преобразует DTO для создания пользователя в сущность User.
     *
     * @param dto DTO для создания пользователя
     * @return сущность User
     */
    public User toUserEntity(UserCreateDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        return user;
    }
}
