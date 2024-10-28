package ru.tinkoff.kora.example.submodule.vet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

import java.util.Collections;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import ru.tinkoff.kora.example.submodule.vet.repository.VetRepository;
import ru.tinkoff.kora.example.submodule.vet.service.VetCache;
import ru.tinkoff.kora.example.submodule.vet.service.VetService;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@KoraAppTest(TestVetApplication.class)
class VetServiceTests implements KoraAppTestConfigModifier {

    @Mock
    @TestComponent
    private VetCache vetCache;
    @Mock
    @TestComponent
    private VetRepository vetRepository;

    @TestComponent
    private VetService vetService;

    @NotNull
    @Override
    public KoraConfigModification config() {
        return KoraConfigModification.ofString("""
                resilient {
                   circuitbreaker.vet {
                     slidingWindowSize = 2
                     minimumRequiredCalls = 2
                     failureRateThreshold = 100
                     permittedCallsInHalfOpenState = 1
                     waitDurationInOpenState = 15s
                   }
                   timeout.vet {
                     duration = 5000ms
                   }
                   retry.vet {
                     delay = 100ms
                     attempts = 2
                   }
                 }
                 """);
    }

    @Test
    void updateVetWithNewCategoryCreated() {
        // given
        mockCache();
        mockRepository();

        var added = vetService.add("dog", "dog");
        assertEquals(1, added.id());

        // when
        Mockito.when(vetRepository.findById(anyLong())).thenReturn(Optional.of(added));
        var updated = vetService.update(added.id(), "cat", "cat");
        assertTrue(updated.isPresent());
        assertEquals(1, updated.get().id());

        // then
        Mockito.verify(vetRepository).insert(any());
    }

    @Test
    void updateVetWithSameCategory() {
        // given
        mockCache();
        mockRepository();

        var added = vetService.add("dog", "dog");
        assertEquals(1, added.id());

        // when
        Mockito.when(vetRepository.findById(anyLong())).thenReturn(Optional.of(added));
        var updated = vetService.update(added.id(), "cat", "cat");
        assertTrue(updated.isPresent());
        assertNotEquals(0, updated.get().id());

        // then
        Mockito.verify(vetRepository).insert(any());
    }

    private void mockCache() {
        Mockito.when(vetCache.get(anyLong())).thenReturn(null);
        Mockito.when(vetCache.put(anyLong(), any())).then(invocation -> invocation.getArguments()[1]);
        Mockito.when(vetCache.get(anyCollection())).thenReturn(Collections.emptyMap());
    }

    private void mockRepository() {
        Mockito.when(vetRepository.insert(any())).thenReturn(1L);
        Mockito.when(vetRepository.findById(anyLong())).thenReturn(Optional.empty());
    }
}
