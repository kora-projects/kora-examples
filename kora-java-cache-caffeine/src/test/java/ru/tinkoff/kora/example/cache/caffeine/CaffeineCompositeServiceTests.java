package ru.tinkoff.kora.example.cache.caffeine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@KoraAppTest(Application.class)
class CaffeineCompositeServiceTests {

    @TestComponent
    private CompositeService service;
    @TestComponent
    private CompositeCache cache;

    @BeforeEach
    void cleanup() {
        cache.invalidateAll();
    }

    @Test
    void get() {
        // when
        var origin = service.get("1", "1");

        // then
        var cached = service.get("1", "1");
        assertEquals(origin, cached);
    }

    @Test
    void getMapping() {
        // given
        var context = new CompositeService.UserContext("1", "2");

        // when
        var origin = service.getMapping(context);

        // then
        var cached = service.getMapping(context);
        assertEquals(origin, cached);
    }

    @Test
    void put() {
        // when
        var origin = service.put(BigDecimal.ONE, "12345", "1", "1");

        // then
        var cached = service.get("1", "1");
        assertEquals(origin, cached);
    }

    @Test
    void delete() {
        // given
        var origin = service.put(BigDecimal.ONE, "12345", "1", "1");
        var cached1 = service.get("1", "1");
        assertEquals(origin, cached1);

        // when
        service.delete("1", "1");

        // then
        var newValue = service.get("1", "1");
        assertNotEquals(origin, newValue);
    }

    @Test
    void deleteAll() {
        // given
        var origin1 = service.put(BigDecimal.ONE, "12345", "1", "1");
        var cached1 = service.get("1", "1");
        assertEquals(origin1, cached1);

        var origin2 = service.put(BigDecimal.ONE, "12345", "2", "2");
        var cached2 = service.get("2", "2");
        assertEquals(origin2, cached2);

        // when
        service.deleteAll();

        // then
        var newValue1 = service.get("1", "1");
        assertNotEquals(origin1, newValue1);

        var newValue2 = service.get("2", "2");
        assertNotEquals(origin2, newValue2);
    }
}
