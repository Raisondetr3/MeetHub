package ru.itmo.cs.unit.service;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import ru.itmo.cs.dto.AuthResponseDto;
import ru.itmo.cs.dto.LoginRequestDto;
import ru.itmo.cs.dto.UserCreateDto;
import ru.itmo.cs.dto.UserDto;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.exception.UserAlreadyExistsException;
import ru.itmo.cs.exception.UserNotFoundException;
import ru.itmo.cs.repository.UserRepository;
import ru.itmo.cs.service.JwtService;
import ru.itmo.cs.service.UserService;
import ru.itmo.cs.util.EntityMapper;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Test
    @DisplayName("Успешная регистрация нового пользователя")
    void shouldRegisterUserSuccessfully() {
        // Arrange
        UserCreateDto createDto = new UserCreateDto("testUser", "test@example.com", "password");
        User user = new User(null, createDto.getUsername(), createDto.getEmail(), "encodedPassword");
        User savedUser = new User(1, createDto.getUsername(), createDto.getEmail(), "encodedPassword");
        UserDto expectedDto = new UserDto(1, createDto.getUsername(), createDto.getEmail());

        when(entityMapper.toUserEntity(eq(createDto), any())).thenReturn(user);
        when(userRepository.save(user)).thenReturn(savedUser);
        when(entityMapper.toUserDto(savedUser)).thenReturn(expectedDto);
        when(jwtService.generateToken(createDto.getUsername())).thenReturn("token");
        when(jwtService.getJwtExpiration()).thenReturn(3600000L);

        // Act
        AuthResponseDto result = userService.register(createDto);

        // Assert
        assertNotNull(result, "Результат не должен быть null");
        assertEquals("token", result.getToken(), "Токен должен быть корректным");
        assertEquals(expectedDto, result.getUser(), "Пользователь должен совпадать с ожидаемым");
        verify(userRepository).save(user);
        verify(entityMapper).toUserDto(savedUser);
    }

    @Test
    @DisplayName("Ошибка при регистрации пользователя с существующим именем")
    void shouldThrowExceptionWhenUserAlreadyExistsDuringRegistration() {
        // Arrange
        UserCreateDto createDto = new UserCreateDto("testUser", "test@example.com", "password");
        when(userRepository.findByUsername(createDto.getUsername())).thenReturn(Optional.of(new User()));

        // Act & Assert
        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.register(createDto),
                "Должно быть выброшено исключение при попытке регистрации существующего пользователя"
        );
        assertEquals("Пользователь с таким именем уже существует", exception.getMessage());
    }

    @Test
    @DisplayName("Успешный вход в систему")
    void shouldLoginSuccessfully() {
        // Arrange
        LoginRequestDto loginDto = new LoginRequestDto("testUser", "password");
        User user = new User(1, loginDto.getUsername(), "test@example.com", "encodedPassword");
        UserDto userDto = new UserDto(1, user.getUsername(), user.getEmail());

        when(authenticationManager.authenticate(any())).thenReturn(null); // Успешная аутентификация
        when(userRepository.findByUsername(loginDto.getUsername())).thenReturn(Optional.of(user));
        when(entityMapper.toUserDto(user)).thenReturn(userDto);
        when(jwtService.generateToken(loginDto.getUsername())).thenReturn("token");
        when(jwtService.getJwtExpiration()).thenReturn(3600000L);

        // Act
        AuthResponseDto result = userService.login(loginDto);

        // Assert
        assertNotNull(result, "Результат не должен быть null");
        assertEquals("token", result.getToken(), "Токен должен быть корректным");
        assertEquals(userDto, result.getUser(), "Пользователь должен совпадать с ожидаемым");
    }

    @Test
    @DisplayName("Ошибка при входе с неверными учетными данными")
    void shouldThrowExceptionWhenLoginFails() {
        // Arrange
        LoginRequestDto loginDto = new LoginRequestDto("testUser", "wrongPassword");
        BadCredentialsException badCredentialsException = new BadCredentialsException("Bad credentials");
        when(authenticationManager.authenticate(any())).thenThrow(badCredentialsException);

        // Act & Assert
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> userService.login(loginDto),
                "Должно быть выброшено исключение при ошибке аутентификации"
        );
        assertEquals("Bad credentials", exception.getMessage(), "Сообщение исключения должно совпадать");
    }


    @Test
    @DisplayName("Успешный поиск пользователя по ID")
    void shouldFindUserByIdSuccessfully() {
        // Arrange
        User user = new User(1, "testUser", "test@example.com", "password");
        UserDto expectedDto = new UserDto(1, "testUser", "test@example.com");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(entityMapper.toUserDto(user)).thenReturn(expectedDto);

        // Act
        UserDto result = userService.findById(1);

        // Assert
        assertNotNull(result, "Результат не должен быть null");
        assertEquals(expectedDto, result, "Пользователь должен совпадать с ожидаемым");
        verify(userRepository).findById(1);
    }

    @Test
    @DisplayName("Ошибка при поиске несуществующего пользователя по ID")
    void shouldThrowExceptionWhenUserNotFoundById() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.findById(1),
                "Должно быть выброшено исключение при отсутствии пользователя"
        );
        assertEquals("Пользователь с ID 1 не найден", exception.getMessage());
    }

    @Test
    @DisplayName("Успешный поиск пользователя по имени")
    void shouldFindUserByUsernameSuccessfully() {
        // Arrange
        User user = new User(1, "testUser", "test@example.com", "password");
        UserDto expectedDto = new UserDto(1, "testUser", "test@example.com");

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(entityMapper.toUserDto(user)).thenReturn(expectedDto);

        // Act
        UserDto result = userService.findByUsername("testUser");

        // Assert
        assertNotNull(result, "Результат не должен быть null");
        assertEquals(expectedDto, result, "Пользователь должен совпадать с ожидаемым");
        verify(userRepository).findByUsername("testUser");
    }

    @Test
    @DisplayName("Ошибка при поиске несуществующего пользователя по имени")
    void shouldThrowExceptionWhenUserNotFoundByUsername() {
        // Arrange
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.findByUsername("nonExistentUser"),
                "Должно быть выброшено исключение при отсутствии пользователя"
        );
        assertEquals("Пользователь с именем nonExistentUser не найден", exception.getMessage());
    }
}
