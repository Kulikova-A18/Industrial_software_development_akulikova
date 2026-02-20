package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Юнит-тесты для ConstellationRepository")
class ConstellationRepositoryUnitTest {

    private ConstellationRepository repository;
    private SatelliteConstellation constellation1;
    private SatelliteConstellation constellation2;
    private SatelliteConstellation constellation3;

    private static final String CONSTELLATION_NAME_1 = "Орбита-1";
    private static final String CONSTELLATION_NAME_2 = "Орбита-2";
    private static final String CONSTELLATION_NAME_3 = "Геостационар-1";
    private static final String NON_EXISTENT_NAME = "Несуществующая-группировка";
    private static final String VERY_LONG_NAME = "Очень-длинное-название-группировки-спутников-для-тестирования-граничных-случаев-1234567890";

    @BeforeEach
    void setUp() {
        repository = new ConstellationRepository();
        constellation1 = new SatelliteConstellation(CONSTELLATION_NAME_1);
        constellation2 = new SatelliteConstellation(CONSTELLATION_NAME_2);
        constellation3 = new SatelliteConstellation(CONSTELLATION_NAME_3);
    }

    @Nested
    @DisplayName("Тесты для метода save()")
    class SaveTests {

        @Test
        @DisplayName("Должен сохранить группировку в репозиторий")
        void shouldSaveConstellation() {
            // Act
            repository.save(constellation1);

            // Assert
            assertTrue(repository.existsByName(CONSTELLATION_NAME_1));
            assertEquals(1, repository.getConstellations().size());
            assertNotNull(repository.findByName(CONSTELLATION_NAME_1));
            assertEquals(constellation1, repository.findByName(CONSTELLATION_NAME_1));
        }

        @Test
        @DisplayName("Должен сохранить несколько группировок")
        void shouldSaveMultipleConstellations() {
            // Act
            repository.save(constellation1);
            repository.save(constellation2);
            repository.save(constellation3);

            // Assert
            assertEquals(3, repository.getConstellations().size());
            assertTrue(repository.existsByName(CONSTELLATION_NAME_1));
            assertTrue(repository.existsByName(CONSTELLATION_NAME_2));
            assertTrue(repository.existsByName(CONSTELLATION_NAME_3));
        }

        @Test
        @DisplayName("Должен перезаписать существующую группировку при сохранении с тем же именем")
        void shouldOverwriteExistingConstellation() {
            // Arrange
            repository.save(constellation1);
            SatelliteConstellation newConstellation = new SatelliteConstellation(CONSTELLATION_NAME_1);

            // Act
            repository.save(newConstellation);

            // Assert
            assertEquals(1, repository.getConstellations().size());
            assertEquals(newConstellation, repository.findByName(CONSTELLATION_NAME_1));
            assertNotEquals(constellation1, repository.findByName(CONSTELLATION_NAME_1));
        }

        @ParameterizedTest
        @ValueSource(strings = { "A", "AB", "ABC", VERY_LONG_NAME })
        @DisplayName("Должен сохранить группировку с различными именами")
        void shouldSaveConstellationWithDifferentNames(String name) {
            // Arrange
            SatelliteConstellation constellation = new SatelliteConstellation(name);

            // Act
            repository.save(constellation);

            // Assert
            assertTrue(repository.existsByName(name));
            assertEquals(constellation, repository.findByName(name));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Должен сохранить группировку с null или пустым именем")
        void shouldSaveConstellationWithNullOrEmptyName(String name) {
            // Arrange
            SatelliteConstellation constellation = new SatelliteConstellation(name);

            // Act
            repository.save(constellation);

            // Assert
            assertTrue(repository.existsByName(name));
            assertEquals(constellation, repository.findByName(name));
        }
    }

    @Nested
    @DisplayName("Тесты для метода findByName()")
    class FindByNameTests {

        @Test
        @DisplayName("Должен найти группировку по имени")
        void shouldFindConstellationByName() {
            // Arrange
            repository.save(constellation1);
            repository.save(constellation2);

            // Act
            SatelliteConstellation found = repository.findByName(CONSTELLATION_NAME_2);

            // Assert
            assertNotNull(found);
            assertEquals(CONSTELLATION_NAME_2, found.getConstellationName());
        }

        @Test
        @DisplayName("Должен вернуть null для несуществующего имени")
        void shouldReturnNullForNonExistentName() {
            // Arrange
            repository.save(constellation1);

            // Act
            SatelliteConstellation found = repository.findByName(NON_EXISTENT_NAME);

            // Assert
            assertNull(found);
        }

        @Test
        @DisplayName("Должен учитывать регистр при поиске")
        void shouldBeCaseSensitive() {
            // Arrange
            String lowerCaseName = "орбита-1";
            String upperCaseName = "ОРБИТА-1";
            SatelliteConstellation lowerCaseConstellation = new SatelliteConstellation(lowerCaseName);
            repository.save(lowerCaseConstellation);

            // Act & Assert
            assertNotNull(repository.findByName(lowerCaseName));
            assertNull(repository.findByName(upperCaseName));
        }

        @Test
        @DisplayName("Должен найти группировку в пустом репозитории")
        void shouldReturnNullForEmptyRepository() {
            // Act
            SatelliteConstellation found = repository.findByName(CONSTELLATION_NAME_1);

            // Assert
            assertNull(found);
        }
    }

    @Nested
    @DisplayName("Тесты для метода existsByName()")
    class ExistsByNameTests {

        @Test
        @DisplayName("Должен вернуть true для существующей группировки")
        void shouldReturnTrueForExistingConstellation() {
            // Arrange
            repository.save(constellation1);

            // Act & Assert
            assertTrue(repository.existsByName(CONSTELLATION_NAME_1));
        }

        @Test
        @DisplayName("Должен вернуть false для несуществующей группировки")
        void shouldReturnFalseForNonExistentConstellation() {
            // Arrange
            repository.save(constellation1);

            // Act & Assert
            assertFalse(repository.existsByName(NON_EXISTENT_NAME));
        }

        @Test
        @DisplayName("Должен вернуть false для пустого репозитория")
        void shouldReturnFalseForEmptyRepository() {
            // Act & Assert
            assertFalse(repository.existsByName(CONSTELLATION_NAME_1));
        }

        @ParameterizedTest
        @ValueSource(strings = { "", " ", "  ", "\t", "\n" })
        @DisplayName("Должен обрабатывать пробельные символы в имени")
        void shouldHandleWhitespaceNames(String name) {
            // Arrange
            SatelliteConstellation constellation = new SatelliteConstellation(name);
            repository.save(constellation);

            // Act & Assert
            assertTrue(repository.existsByName(name));
        }
    }

    @Nested
    @DisplayName("Тесты для метода getConstellations()")
    class GetConstellationsTests {

        @Test
        @DisplayName("Должен вернуть пустую карту для пустого репозитория")
        void shouldReturnEmptyMapForEmptyRepository() {
            // Act
            Map<String, SatelliteConstellation> result = repository.getConstellations();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
            assertEquals(0, result.size());
        }

        @Test
        @DisplayName("Должен вернуть все сохраненные группировки")
        void shouldReturnAllSavedConstellations() {
            // Arrange
            repository.save(constellation1);
            repository.save(constellation2);

            // Act
            Map<String, SatelliteConstellation> result = repository.getConstellations();

            // Assert
            assertEquals(2, result.size());
            assertTrue(result.containsKey(CONSTELLATION_NAME_1));
            assertTrue(result.containsKey(CONSTELLATION_NAME_2));
            assertEquals(constellation1, result.get(CONSTELLATION_NAME_1));
            assertEquals(constellation2, result.get(CONSTELLATION_NAME_2));
        }

        @Test
        @DisplayName("Должен вернуть копию карты, а не ссылку на оригинал")
        void shouldReturnCopyOfMapNotOriginal() {
            // Arrange
            repository.save(constellation1);

            // Act
            Map<String, SatelliteConstellation> result = repository.getConstellations();
            result.clear(); // Очищаем полученную карту

            // Assert
            assertEquals(1, repository.getConstellations().size()); // Оригинал не должен измениться
            assertTrue(repository.existsByName(CONSTELLATION_NAME_1));
        }

        @Test
        @DisplayName("Должен вернуть карту, которая не влияет на оригинал при модификации")
        void modificationsShouldNotAffectOriginal() {
            // Arrange
            repository.save(constellation1);
            Map<String, SatelliteConstellation> result = repository.getConstellations();

            // Act
            SatelliteConstellation newConstellation = new SatelliteConstellation("Новая");
            result.put("Новая", newConstellation);

            // Assert
            assertFalse(repository.existsByName("Новая"));
            assertEquals(1, repository.getConstellations().size());
        }
    }

    @Nested
    @DisplayName("Тесты для метода findAll()")
    class FindAllTests {

        @Test
        @DisplayName("Должен вернуть пустую карту для пустого репозитория")
        void shouldReturnEmptyMapForEmptyRepository() {
            // Act
            Map<String, SatelliteConstellation> result = repository.findAll();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Должен вернуть все группировки")
        void shouldReturnAllConstellations() {
            // Arrange
            repository.save(constellation1);
            repository.save(constellation2);

            // Act
            Map<String, SatelliteConstellation> result = repository.findAll();

            // Assert
            assertEquals(2, result.size());
            assertTrue(result.containsKey(CONSTELLATION_NAME_1));
            assertTrue(result.containsKey(CONSTELLATION_NAME_2));
        }
    }

    @Nested
    @DisplayName("Интеграционные сценарии с несколькими операциями")
    class MultiOperationScenarios {

        @Test
        @DisplayName("Должен корректно обрабатывать последовательность операций")
        void shouldHandleSequenceOfOperations() {
            // 1. Сохраняем первую группировку
            repository.save(constellation1);
            assertEquals(1, repository.getConstellations().size());
            assertTrue(repository.existsByName(CONSTELLATION_NAME_1));

            // 2. Сохраняем вторую группировку
            repository.save(constellation2);
            assertEquals(2, repository.getConstellations().size());
            assertTrue(repository.existsByName(CONSTELLATION_NAME_2));

            // 3. Ищем несуществующую
            assertNull(repository.findByName(NON_EXISTENT_NAME));

            // 4. Перезаписываем первую группировку
            SatelliteConstellation updatedConstellation = new SatelliteConstellation(CONSTELLATION_NAME_1);
            repository.save(updatedConstellation);
            assertEquals(2, repository.getConstellations().size());
            assertEquals(updatedConstellation, repository.findByName(CONSTELLATION_NAME_1));
            assertNotEquals(constellation1, repository.findByName(CONSTELLATION_NAME_1));

            // 5. Проверяем все группировки
            Map<String, SatelliteConstellation> all = repository.findAll();
            assertEquals(2, all.size());
            assertTrue(all.containsKey(CONSTELLATION_NAME_1));
            assertTrue(all.containsKey(CONSTELLATION_NAME_2));
        }

        @Test
        @DisplayName("Должен работать с группировками, содержащими спутники")
        void shouldWorkWithConstellationsContainingSatellites() {
            // Arrange
            SatelliteConstellation constellation = new SatelliteConstellation("Активная");
            CommunicationSatellite comSat = new CommunicationSatellite("ComSat-1", 500.0);
            ImagingSatellite imgSat = new ImagingSatellite("ImgSat-1", 2.5);

            constellation.addSatellite(comSat);
            constellation.addSatellite(imgSat);

            // Act
            repository.save(constellation);

            // Assert
            SatelliteConstellation found = repository.findByName("Активная");
            assertNotNull(found);
            assertEquals(2, found.getSatellites().size());

            // Проверяем, что спутники сохранились корректно
            Satellite firstSat = found.getSatellites().get(0);
            Satellite secondSat = found.getSatellites().get(1);

            assertTrue(firstSat instanceof CommunicationSatellite);
            assertTrue(secondSat instanceof ImagingSatellite);
            assertEquals("ComSat-1", firstSat.getName());
            assertEquals("ImgSat-1", secondSat.getName());
        }
    }
}