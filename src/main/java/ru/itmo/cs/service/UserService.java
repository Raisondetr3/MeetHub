package ru.itmo.cs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.itmo.cs.dto.AuthResponseDto;
import ru.itmo.cs.dto.LoginRequestDto;
import ru.itmo.cs.dto.UserCreateDto;
import ru.itmo.cs.dto.UserDto;
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
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final EntityMapper entityMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Пользователь с именем " + username + " не найден"));
    }

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
     */
    public AuthResponseDto login(LoginRequestDto loginRequestDto) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequestDto.getUsername(),
                loginRequestDto.getPassword()
            )
        );

        UserDto user = findByUsername(loginRequestDto.getUsername());

        String token = jwtService.generateToken(user.getUsername());
        long expirationTime = System.currentTimeMillis() + jwtService.getJwtExpiration();

        return new AuthResponseDto(token, expirationTime, user);
    }

    /**
     * Ищет пользователя по ID.
     *
     * @param id ID пользователя
     * @return DTO найденного пользователя
     * @throws UserNotFoundException если пользователь не найден
     */
    public UserDto findById(Integer id) {
        return userRepository.findById(id)
            .map(entityMapper::toUserDto)
            .orElseThrow(() -> new UserNotFoundException("Пользователь с ID " + id + " не найден"));
    }

    /**
     * Ищет пользователя по имени.
     *
     * @param username имя пользователя
     * @return DTO найденного пользователя
     * @throws UserNotFoundException если пользователь не найден
     */
    public UserDto findByUsername(String username) {
        return userRepository.findByUsername(username)
            .map(entityMapper::toUserDto)
            .orElseThrow(() -> new UserNotFoundException("Пользователь с именем " + username + " не найден"));
    }
}



