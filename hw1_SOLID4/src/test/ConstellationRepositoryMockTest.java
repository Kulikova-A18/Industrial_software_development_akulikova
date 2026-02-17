package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Мок-тесты для ConstellationRepository")
class ConstellationRepositoryMockTest {

    @Mock
    private ConstellationRepository mockRepository;

    private SatelliteConstellation constellation1;
    private SatelliteConstellation constellation2;
    private Map<String, SatelliteConstellation> mockConstellations;

    private static final String CONSTELLATION_NAME_1 = "Орбита-1";
    private static final String CONSTELLATION_NAME_2 = "Орбита-2";
    private static final String NON_EXISTENT_NAME = "Несуществующая";

    @BeforeEach
    void setUp() {
        constellation1 = new SatelliteConstellation(CONSTELLATION_NAME_1);
        constellation2 = new SatelliteConstellation(CONSTELLATION_NAME_2);

        mockConstellations = new HashMap<>();
        mockConstellations.put(CONSTELLATION_NAME_1, constellation1);
        mockConstellations.put(CONSTELLATION_NAME_2, constellation2);
    }

    @Test
    @DisplayName("Должен вызвать метод save() с правильным параметром")
    void shouldCallSaveWithCorrectParameter() {
        // Arrange
        doNothing().when(mockRepository).save(constellation1);

        // Act
        mockRepository.save(constellation1);

        // Assert
        verify(mockRepository, times(1)).save(constellation1);
        verify(mockRepository, never()).save(constellation2);
    }

    @Test
    @DisplayName("Должен вызвать метод save() дважды при сохранении двух группировок")
    void shouldCallSaveTwice() {
        // Arrange
        doNothing().when(mockRepository).save(any(SatelliteConstellation.class));

        // Act
        mockRepository.save(constellation1);
        mockRepository.save(constellation2);

        // Assert
        verify(mockRepository, times(2)).save(any(SatelliteConstellation.class));
        verify(mockRepository, times(1)).save(constellation1);
        verify(mockRepository, times(1)).save(constellation2);
    }

    @Test
    @DisplayName("Должен вернуть подготовленную группировку при поиске по имени")
    void shouldReturnPreparedConstellationWhenFound() {
        // Arrange
        when(mockRepository.findByName(CONSTELLATION_NAME_1)).thenReturn(constellation1);
        when(mockRepository.findByName(CONSTELLATION_NAME_2)).thenReturn(constellation2);

        // Act
        SatelliteConstellation result1 = mockRepository.findByName(CONSTELLATION_NAME_1);
        SatelliteConstellation result2 = mockRepository.findByName(CONSTELLATION_NAME_2);

        // Assert
        assertEquals(constellation1, result1);
        assertEquals(constellation2, result2);
        assertEquals(CONSTELLATION_NAME_1, result1.getConstellationName());
        assertEquals(CONSTELLATION_NAME_2, result2.getConstellationName());

        verify(mockRepository, times(1)).findByName(CONSTELLATION_NAME_1);
        verify(mockRepository, times(1)).findByName(CONSTELLATION_NAME_2);
    }

    @Test
    @DisplayName("Должен вернуть null для несуществующей группировки")
    void shouldReturnNullForNonExistent() {
        // Arrange
        when(mockRepository.findByName(NON_EXISTENT_NAME)).thenReturn(null);

        // Act
        SatelliteConstellation result = mockRepository.findByName(NON_EXISTENT_NAME);

        // Assert
        assertNull(result);
        verify(mockRepository, times(1)).findByName(NON_EXISTENT_NAME);
    }

    @Test
    @DisplayName("Должен вернуть true для существующей группировки")
    void shouldReturnTrueForExisting() {
        // Arrange
        when(mockRepository.existsByName(CONSTELLATION_NAME_1)).thenReturn(true);
        when(mockRepository.existsByName(CONSTELLATION_NAME_2)).thenReturn(true);

        // Act
        boolean result1 = mockRepository.existsByName(CONSTELLATION_NAME_1);
        boolean result2 = mockRepository.existsByName(CONSTELLATION_NAME_2);

        // Assert
        assertTrue(result1);
        assertTrue(result2);

        verify(mockRepository, times(1)).existsByName(CONSTELLATION_NAME_1);
        verify(mockRepository, times(1)).existsByName(CONSTELLATION_NAME_2);
    }

    @Test
    @DisplayName("Должен вернуть false для несуществующей группировки")
    void shouldReturnFalseForNonExistent() {
        // Arrange
        when(mockRepository.existsByName(NON_EXISTENT_NAME)).thenReturn(false);

        // Act
        boolean result = mockRepository.existsByName(NON_EXISTENT_NAME);

        // Assert
        assertFalse(result);
        verify(mockRepository, times(1)).existsByName(NON_EXISTENT_NAME);
    }

    @Test
    @DisplayName("Должен вернуть подготовленную карту всех группировок")
    void shouldReturnPreparedMap() {
        // Arrange
        when(mockRepository.getConstellations()).thenReturn(mockConstellations);

        // Act
        Map<String, SatelliteConstellation> result = mockRepository.getConstellations();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.containsKey(CONSTELLATION_NAME_1));
        assertTrue(result.containsKey(CONSTELLATION_NAME_2));

        verify(mockRepository, times(1)).getConstellations();
    }

    @Test
    @DisplayName("Должен вернуть пустую карту для пустого репозитория")
    void shouldReturnEmptyMap() {
        // Arrange
        when(mockRepository.getConstellations()).thenReturn(new HashMap<>());

        // Act
        Map<String, SatelliteConstellation> result = mockRepository.getConstellations();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mockRepository, times(1)).getConstellations();
    }

    @Test
    @DisplayName("Должен проверить последовательность вызовов методов")
    void shouldVerifyMethodCallSequence() {
        // Arrange
        when(mockRepository.findByName(CONSTELLATION_NAME_1)).thenReturn(constellation1);
        when(mockRepository.existsByName(CONSTELLATION_NAME_1)).thenReturn(true);

        // Act
        boolean exists = mockRepository.existsByName(CONSTELLATION_NAME_1);
        SatelliteConstellation found = mockRepository.findByName(CONSTELLATION_NAME_1);

        // Assert
        InOrder inOrder = inOrder(mockRepository);
        inOrder.verify(mockRepository).existsByName(CONSTELLATION_NAME_1);
        inOrder.verify(mockRepository).findByName(CONSTELLATION_NAME_1);
    }

    @Test
    @DisplayName("Должен проверить, что методы вызываются с правильными параметрами")
    void shouldVerifyMethodsCalledWithCorrectParameters() {
        // Act
        mockRepository.save(constellation1);
        mockRepository.findByName(CONSTELLATION_NAME_1);
        mockRepository.existsByName(CONSTELLATION_NAME_1);

        // Assert
        verify(mockRepository).save(constellation1);
        verify(mockRepository).findByName(CONSTELLATION_NAME_1);
        verify(mockRepository).existsByName(CONSTELLATION_NAME_1);

        verify(mockRepository, never()).save(constellation2);
        verify(mockRepository, never()).findByName(CONSTELLATION_NAME_2);
        verify(mockRepository, never()).existsByName(CONSTELLATION_NAME_2);
    }
}