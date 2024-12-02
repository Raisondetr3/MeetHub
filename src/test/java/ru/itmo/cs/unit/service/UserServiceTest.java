package ru.itmo.cs.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.itmo.cs.dto.UserCreateDto;
import ru.itmo.cs.dto.UserDto;
import ru.itmo.cs.entity.User;
import ru.itmo.cs.repository.UserRepository;
import ru.itmo.cs.service.UserService;
import ru.itmo.cs.util.EntityMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserService userService;
    private UserRepository userRepository;
    private EntityMapper entityMapper;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        entityMapper = Mockito.mock(EntityMapper.class);
        userService = new UserService(userRepository, entityMapper, mock(PasswordEncoder.class));
    }

    @Test
    @DisplayName("Успешная регистрация нового пользователя")
    void shouldRegisterUserSuccessfully() {
        UserCreateDto createDto = new UserCreateDto("testUser", "test@example.com", "password");
        User user = new User(null, createDto.getUsername(), createDto.getEmail(), "encodedPassword");
        User savedUser = new User(1, createDto.getUsername(), createDto.getEmail(), "encodedPassword");
        UserDto expectedDto = new UserDto(1, createDto.getUsername(), createDto.getEmail());

        when(entityMapper.toUserEntity(eq(createDto), any())).thenReturn(user);
        when(userRepository.save(user)).thenReturn(savedUser);
        when(entityMapper.toUserDto(savedUser)).thenReturn(expectedDto);

        UserDto result = userService.registerUser(createDto);

        assertNotNull(result, "Результат не должен быть null");
        assertEquals(expectedDto, result, "Возвращенный DTO должен соответствовать ожидаемому");
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
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> userService.registerUser(createDto),
                "Должно быть выброшено исключение при попытке регистрации существующего пользователя"
        );
        assertEquals("Пользователь с таким именем уже существует", exception.getMessage());
    }

    @Test
    void shouldFindUserById() {
        User user = new User(1, "testUser", "test@example.com", "password");
        UserDto expectedDto = new UserDto(user.getId(), user.getUsername(), user.getEmail());

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(entityMapper.toUserDto(user)).thenReturn(expectedDto);

        Optional<UserDto> result = userService.findById(1);

        assertTrue(result.isPresent(), "Пользователь должен быть найден");
        assertEquals(expectedDto, result.get(), "Возвращенный DTO должен соответствовать ожидаемому");
        verify(userRepository).findById(1);
    }

    @Test
    @DisplayName("Возврат пустого результата, если пользователь не найден по ID")
    void shouldReturnEmptyWhenUserNotFoundById() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        Optional<UserDto> result = userService.findById(1);

        assertFalse(result.isPresent(), "Пользователь не должен быть найден");
        verify(userRepository).findById(1);
    }

    @Test
    void shouldLoadUserByUsernameSuccessfully() {
        User user = new User(1, "testUser", "test@example.com", "password");
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        UserDetails result = userService.loadUserByUsername("testUser");

        assertNotNull(result, "UserDetails не должен быть null");
        assertEquals(user.getUsername(), result.getUsername(), "Имя пользователя должно совпадать");
        verify(userRepository).findByUsername("testUser");
    }

    @Test
    @DisplayName("Ошибка при загрузке пользователя по несуществующему имени")
    void shouldThrowExceptionWhenUserNotFoundByUsername() {
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("nonExistentUser"),
                "Должно быть выброшено исключение, если пользователь не найден"
        );
        assertEquals("Пользователь с именем nonExistentUser не найден", exception.getMessage());
        verify(userRepository).findByUsername("nonExistentUser");
    }

    @Test
    void shouldCheckIfUserExistsByUsername() {
        String username = "testUser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(new User()));

        boolean exists = userService.existsByUsername(username);

        assertTrue(exists, "Пользователь должен существовать");
        verify(userRepository).findByUsername(username);
    }

    @Test
    void shouldReturnFalseIfUserDoesNotExistByUsername() {
        String username = "nonExistentUser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        boolean exists = userService.existsByUsername(username);

        assertFalse(exists, "Пользователь не должен существовать");
        verify(userRepository).findByUsername(username);
    }
}