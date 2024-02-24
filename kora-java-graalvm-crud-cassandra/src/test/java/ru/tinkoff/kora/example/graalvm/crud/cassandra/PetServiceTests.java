package ru.tinkoff.kora.example.graalvm.crud.cassandra;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

import java.util.Collections;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import ru.tinkoff.kora.database.common.UpdateCount;
import ru.tinkoff.kora.example.graalvm.crud.cassandra.repository.PetRepository;
import ru.tinkoff.kora.example.graalvm.crud.cassandra.service.PetCache;
import ru.tinkoff.kora.example.graalvm.crud.cassandra.service.PetService;
import ru.tinkoff.kora.example.graalvm.crud.openapi.server.model.CategoryCreateTO;
import ru.tinkoff.kora.example.graalvm.crud.openapi.server.model.PetCreateTO;
import ru.tinkoff.kora.example.graalvm.crud.openapi.server.model.PetUpdateTO;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier;
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@KoraAppTest(Application.class)
class PetServiceTests implements KoraAppTestConfigModifier {

    @Mock
    @TestComponent
    private PetCache petCache;
    @Mock
    @TestComponent
    private PetRepository petRepository;

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
        mockRepository();

        var added = petService.add(new PetCreateTO("dog", new CategoryCreateTO("dog"))).block();
        assertEquals(1, added.id());

        // when
        Mockito.when(petRepository.findById(anyLong())).thenReturn(Mono.just(added));
        var updated = petService.update(added.id(),
                new PetUpdateTO(PetUpdateTO.StatusEnum.PENDING, "cat", new CategoryCreateTO("cat"))).blockOptional();
        assertTrue(updated.isPresent());
        assertEquals(1, updated.get().id());

        // then
        Mockito.verify(petRepository).insert(any());
    }

    @Test
    void updatePetWithSameCategory() {
        // given
        mockCache();
        mockRepository();

        var added = petService.add(new PetCreateTO("dog", new CategoryCreateTO("dog"))).block();
        assertEquals(1, added.id());

        // when
        Mockito.when(petRepository.findById(anyLong())).thenReturn(Mono.just(added));
        var updated = petService.update(added.id(),
                new PetUpdateTO(PetUpdateTO.StatusEnum.PENDING, "cat", new CategoryCreateTO("dog"))).blockOptional();
        assertTrue(updated.isPresent());
        assertNotEquals(0, updated.get().id());

        // then
        Mockito.verify(petRepository).insert(any());
    }

    private void mockCache() {
        Mockito.when(petCache.get(anyLong())).thenReturn(null);
        Mockito.when(petCache.put(anyLong(), any())).then(invocation -> invocation.getArguments()[1]);
        Mockito.when(petCache.get(anyCollection())).thenReturn(Collections.emptyMap());
    }

    private void mockRepository() {
        Mockito.when(petRepository.insert(any())).thenReturn(Mono.just(new UpdateCount(1L)));
        Mockito.when(petRepository.findById(anyLong())).thenReturn(Mono.empty());
    }
}
