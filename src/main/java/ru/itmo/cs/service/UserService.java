package ru.itmo.cs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.itmo.cs.dto.auth.AuthResponseDto;
import ru.itmo.cs.dto.auth.LoginRequestDto;
import ru.itmo.cs.dto.auth.UserCreateDto;
import ru.itmo.cs.dto.auth.UserDto;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.exception.UserAlreadyExistsException;
import ru.itmo.cs.exception.UserNotFoundException;
import ru.itmo.cs.repository.UserRepository;
import ru.itmo.cs.util.EntityMapper;

/**
 * Сервис для работы с пользователями.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EntityMapper entityMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Регистрация нового пользователя.
     *
     * @param dto DTO для создания пользователя
     * @return DTO с токеном, временем истечения и данными пользователя
     * @throws UserAlreadyExistsException если пользователь с таким именем уже существует
     */
    public AuthResponseDto register(UserCreateDto dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
        }

        User user = entityMapper.toUserEntity(dto, passwordEncoder);
        User savedUser = userRepository.save(user);
        UserDto registeredUser = entityMapper.toUserDto(savedUser);

        String token = jwtService.generateToken(registeredUser.getUsername());
        long expirationTime = System.currentTimeMillis() + jwtService.getJwtExpiration();

        return new AuthResponseDto(token, expirationTime, registeredUser);
    }

    /**
     * Вход в систему.
     *
     * @param loginRequestDto DTO для входа
     * @return DTO с токеном, временем истечения и данными пользователя
     * @throws BadCredentialsException если пароль не совпадает
     */
    public AuthResponseDto login(LoginRequestDto loginRequestDto) {
        UserDetails userDetails = userRepository.findByUsername(loginRequestDto.getUsername())
            .orElseThrow(() -> new BadCredentialsException("Неверное имя пользователя или пароль"));

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), userDetails.getPassword())) {
            throw new BadCredentialsException("Неверное имя пользователя или пароль");
        }

        UserDto user = findByUsername(loginRequestDto.getUsername());

        String token = jwtService.generateToken(user.getUsername());
        long expirationTime = System.currentTimeMillis() + jwtService.getJwtExpiration();

        return new AuthResponseDto(token, expirationTime, user);
    }

    public UserDto findById(Integer id) {
        return userRepository.findById(id)
            .map(entityMapper::toUserDto)
            .orElseThrow(() -> new UserNotFoundException("Пользователь с ID " + id + " не найден"));
    }

    public UserDto findByUsername(String username) {
        return userRepository.findByUsername(username)
            .map(entityMapper::toUserDto)
            .orElseThrow(() -> new UserNotFoundException("Пользователь с именем " + username + " не найден"));
    }
}

