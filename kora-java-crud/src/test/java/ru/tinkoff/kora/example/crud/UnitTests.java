package ru.tinkoff.kora.example.crud;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import ru.tinkoff.kora.example.crud.openapi.http.server.model.CategoryCreateTO;
import ru.tinkoff.kora.example.crud.openapi.http.server.model.PetCreateTO;
import ru.tinkoff.kora.example.crud.openapi.http.server.model.PetUpdateTO;
import ru.tinkoff.kora.example.crud.repository.CategoryRepository;
import ru.tinkoff.kora.example.crud.repository.PetRepository;
import ru.tinkoff.kora.example.crud.service.PetCache;
import ru.tinkoff.kora.example.crud.service.PetService;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@KoraAppTest(Application.class)
class UnitTests implements KoraAppTestConfigModifier {

    @Mock
    @TestComponent
    private PetCache petCache;
    @Mock
    @TestComponent
    private PetRepository petRepository;
    @Mock
    @TestComponent
    private CategoryRepository categoryRepository;

    @TestComponent
    private PetService petService;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofString("""
                resilient {
                   circuitbreaker.pet {
                     slidingWindowSize = 2
                     minimumRequiredCalls = 2
                     failureRateThreshold = 100
                     permittedCallsInHalfOpenState = 1
                     waitDurationInOpenState = 15s
                   }
                   timeout.pet {
                     duration = 5000ms
                   }
                   retry.pet {
                     delay = 100ms
                     attempts = 2
                   }
                 }
                 """);
    }

    @Test
    void updatePetWithNewCategoryCreated() {
        // given
        mockCache();
        mockRepository(Map.of("dog", 1L, "cat", 2L));

        var added = petService.add(new PetCreateTO("dog", new CategoryCreateTO("dog")));
        assertEquals(1, added.id());
        assertEquals(1, added.category().id());

        // when
        Mockito.when(petRepository.findById(anyLong())).thenReturn(Optional.of(added));
        var updated = petService.update(added.id(),
                new PetUpdateTO(PetUpdateTO.StatusEnum.PENDING, "cat", new CategoryCreateTO("cat")));
        assertTrue(updated.isPresent());
        assertEquals(1, updated.get().id());
        assertEquals(2, updated.get().category().id());

        // then
        Mockito.verify(petRepository).insert(any());
        Mockito.verify(categoryRepository, Mockito.times(2)).insert(any());
    }

    @Test
    void updatePetWithSameCategory() {
        // given
        mockCache();
        mockRepository(Map.of("dog", 1L));

        var added = petService.add(new PetCreateTO("dog", new CategoryCreateTO("dog")));
        assertEquals(1, added.id());
        assertEquals(1, added.category().id());

        // when
        Mockito.when(petRepository.findById(anyLong())).thenReturn(Optional.of(added));
        Mockito.when(categoryRepository.findByName(any())).thenReturn(Optional.of(added.category()));
        var updated = petService.update(added.id(),
                new PetUpdateTO(PetUpdateTO.StatusEnum.PENDING, "cat", new CategoryCreateTO("dog")));
        assertTrue(updated.isPresent());
        assertNotEquals(0, updated.get().id());
        assertNotEquals(0, updated.get().category().id());

        // then
        Mockito.verify(petRepository).insert(any());
        Mockito.verify(categoryRepository).insert(any());
    }

    private void mockCache() {
        Mockito.when(petCache.get(anyLong())).thenReturn(null);
        Mockito.when(petCache.put(anyLong(), any())).then(invocation -> invocation.getArguments()[1]);
        Mockito.when(petCache.get(anyCollection())).thenReturn(Collections.emptyMap());
    }

    private void mockRepository(Map<String, Long> categoryNameToId) {
        categoryNameToId.forEach((k, v) -> Mockito.when(categoryRepository.insert(k)).thenReturn(v));
        Mockito.when(categoryRepository.findByName(any())).thenReturn(Optional.empty());
        Mockito.when(petRepository.insert(any())).thenReturn(1L);
        Mockito.when(petRepository.findById(anyLong())).thenReturn(Optional.empty());
    }
}
