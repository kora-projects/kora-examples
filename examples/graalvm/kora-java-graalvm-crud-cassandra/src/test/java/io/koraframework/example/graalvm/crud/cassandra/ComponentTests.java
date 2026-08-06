package io.koraframework.example.graalvm.crud.cassandra;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

import java.util.Collections;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import io.koraframework.example.graalvm.crud.cassandra.repository.PetRepository;
import io.koraframework.example.graalvm.crud.cassandra.service.PetCache;
import io.koraframework.example.graalvm.crud.cassandra.service.PetService;
import io.koraframework.example.graalvm.crud.openapi.server.model.CategoryCreateTO;
import io.koraframework.example.graalvm.crud.openapi.server.model.PetCreateTO;
import io.koraframework.example.graalvm.crud.openapi.server.model.PetUpdateTO;
import io.koraframework.test.extension.junit5.KoraAppTest;
import io.koraframework.test.extension.junit5.KoraAppTestConfigModifier;
import io.koraframework.test.extension.junit5.KoraConfigModification;
import io.koraframework.test.extension.junit5.TestComponent;

@KoraAppTest(Application.class)
class ComponentTests implements KoraAppTestConfigModifier {

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
                     type = FIXED_WINDOW
                     countBased.windowSize = 2
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
                     attempts = 0
                   }
                 }
                 """);
    }

    @Test
    void updatePetWithNewCategoryCreated() {
        // given
        mockCache();
        mockRepository();

        var added = petService.add(new PetCreateTO("dog", new CategoryCreateTO("dog")));
        assertNotEquals(0L, added.id());
        assertEquals("dog", added.category());

        // when
        Mockito.when(petRepository.findById(anyLong())).thenReturn(added);
        var updated = petService.update(added.id(),
                new PetUpdateTO(PetUpdateTO.StatusEnum.PENDING, "cat", new CategoryCreateTO("cat")));

        // then
        assertNotNull(updated);
        assertEquals(added.id(), updated.id());
        assertEquals("cat", updated.category());
        Mockito.verify(petRepository).insert(any());
        Mockito.verify(petRepository).update(any());
    }

    @Test
    void updatePetWithSameCategory() {
        // given
        mockCache();
        mockRepository();

        var added = petService.add(new PetCreateTO("dog", new CategoryCreateTO("dog")));
        assertNotEquals(0L, added.id());

        // when
        Mockito.when(petRepository.findById(anyLong())).thenReturn(added);
        var updated = petService.update(added.id(),
                new PetUpdateTO(PetUpdateTO.StatusEnum.PENDING, "cat", new CategoryCreateTO("dog")));

        // then
        assertNotNull(updated);
        assertEquals(added.id(), updated.id());
        assertEquals("dog", updated.category());
        Mockito.verify(petRepository).insert(any());
        Mockito.verify(petRepository).update(any());
    }

    private void mockCache() {
        Mockito.when(petCache.get(anyLong())).thenReturn(null);
        Mockito.when(petCache.put(anyLong(), any())).then(invocation -> invocation.getArguments()[1]);
        Mockito.when(petCache.get(anyCollection())).thenReturn(Collections.emptyMap());
    }

    private void mockRepository() {
        Mockito.when(petRepository.findById(anyLong())).thenReturn(null);
    }
}
