package ru.itmo.cs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.itmo.cs.dto.UserCreateDto;
import ru.itmo.cs.dto.UserDto;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.repository.UserRepository;
import ru.itmo.cs.util.EntityMapper;

import java.util.Optional;

/**
 * Сервис для работы с пользователями.
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final EntityMapper entityMapper;
    private final PasswordEncoder passwordEncoder;


    /**
     * Загружает пользователя по имени для аутентификации.
     *
     * @param username имя пользователя
     * @return UserDetails
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Пользователь с именем " + username + " не найден"));
    }

    /**
     * Регистрирует нового пользователя.
     *
     * @param dto DTO для создания пользователя
     * @return DTO созданного пользователя
     */
    public UserDto registerUser(UserCreateDto dto) {
        User user = entityMapper.toUserEntity(dto, passwordEncoder);
        User savedUser = userRepository.save(user);
        return entityMapper.toUserDto(savedUser);
    }

    /**
     * Ищет пользователя по его ID.
     *
     * @param id ID пользователя
     * @return DTO найденного пользователя
     */
    public Optional<UserDto> findById(Integer id) {
        return userRepository.findById(id)
            .map(entityMapper::toUserDto);
    }

    /**
     * Проверяет, существует ли пользователь с указанным именем.
     *
     * @param username имя пользователя
     * @return true, если пользователь существует
     */
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
}
