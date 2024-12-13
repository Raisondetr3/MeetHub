package ru.itmo.cs.unit.service;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import ru.itmo.cs.entity.*;
import ru.itmo.cs.exception.ResourceNotFoundException;
import ru.itmo.cs.repository.LocationRepository;
import ru.itmo.cs.service.LocationService;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @InjectMocks
    private LocationService locationService;

    @Mock
    private LocationRepository locationRepository;

    private Location location;

    @BeforeEach
    void setUp() {
        location = new Location();
        location.setId(1);
        location.setCountry("Russia");
        location.setRegion("Saint Petersburg");
        location.setCity("SPB");
        location.setAddress("Nevsky Prospect, 1");
    }

    @Test
    @DisplayName("Успешное создание местоположения")
    void shouldCreateLocationSuccessfully() {
        // Arrange
        when(locationRepository.save(location)).thenReturn(location);

        // Act
        Location result = locationService.createLocation(location);

        // Assert
        assertNotNull(result);
        assertEquals(location, result);
        verify(locationRepository).save(location);
    }

    @Test
    @DisplayName("Успешное получение местоположения по ID")
    void shouldGetLocationByIdSuccessfully() {
        // Arrange
        when(locationRepository.findById(1)).thenReturn(Optional.of(location));

        // Act
        Location result = locationService.getLocationById(1);

        // Assert
        assertNotNull(result);
        assertEquals(location, result);
        verify(locationRepository).findById(1);
    }

    @Test
    @DisplayName("Ошибка при получении местоположения с несуществующим ID")
    void shouldThrowExceptionForInvalidLocationId() {
        // Arrange
        when(locationRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> locationService.getLocationById(1),
            "Ожидалось исключение при отсутствии местоположения"
        );

        assertEquals("Location not found with ID: 1", exception.getMessage());
        verify(locationRepository).findById(1);
    }

    @Test
    @DisplayName("Успешное получение местоположений по стране")
    void shouldGetLocationsByCountrySuccessfully() {
        // Arrange
        when(locationRepository.findByCountry("Russia")).thenReturn(List.of(location));

        // Act
        List<Location> result = locationService.getLocationsByCountry("Russia");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(location, result.get(0));
        verify(locationRepository).findByCountry("Russia");
    }

    @Test
    @DisplayName("Успешное получение местоположений по городу")
    void shouldGetLocationsByCitySuccessfully() {
        // Arrange
        when(locationRepository.findByCity("SPB")).thenReturn(List.of(location));

        // Act
        List<Location> result = locationService.getLocationsByCity("SPB");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(location, result.get(0));
        verify(locationRepository).findByCity("SPB");
    }

    @Test
    @DisplayName("Успешное обновление местоположения")
    void shouldUpdateLocationSuccessfully() {
        // Arrange
        Location updatedDetails = new Location();
        updatedDetails.setCountry("Russia");
        updatedDetails.setRegion("Moscow Region");
        updatedDetails.setCity("Moscow");
        updatedDetails.setAddress("Tverskaya, 5");

        when(locationRepository.findById(1)).thenReturn(Optional.of(location));
        when(locationRepository.save(location)).thenReturn(location);

        // Act
        Location result = locationService.updateLocation(1, updatedDetails);

        // Assert
        assertNotNull(result);
        assertEquals("Moscow", result.getCity());
        assertEquals("Moscow Region", result.getRegion());
        assertEquals("Tverskaya, 5", result.getAddress());
        verify(locationRepository).findById(1);
        verify(locationRepository).save(location);
    }

    @Test
    @DisplayName("Ошибка при обновлении несуществующего местоположения")
    void shouldThrowExceptionWhenUpdatingInvalidLocation() {
        // Arrange
        Location updatedDetails = new Location();
        updatedDetails.setCountry("Russia");
        updatedDetails.setRegion("Moscow Region");
        updatedDetails.setCity("Moscow");
        updatedDetails.setAddress("Tverskaya, 5");

        when(locationRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> locationService.updateLocation(1, updatedDetails),
            "Ожидалось исключение при обновлении несуществующего местоположения"
        );

        assertEquals("Location not found with ID: 1", exception.getMessage());
        verify(locationRepository).findById(1);
    }

    @Test
    @DisplayName("Успешное удаление местоположения")
    void shouldDeleteLocationSuccessfully() {
        // Arrange
        when(locationRepository.existsById(1)).thenReturn(true);

        // Act
        locationService.deleteLocation(1);

        // Assert
        verify(locationRepository).existsById(1);
        verify(locationRepository).deleteById(1);
    }

    @Test
    @DisplayName("Ошибка при удалении несуществующего местоположения")
    void shouldThrowExceptionWhenDeletingInvalidLocation() {
        // Arrange
        when(locationRepository.existsById(1)).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> locationService.deleteLocation(1),
            "Ожидалось исключение при удалении несуществующего местоположения"
        );

        assertEquals("Location not found with ID: 1", exception.getMessage());
        verify(locationRepository).existsById(1);
    }
}

