package io.koraframework.migration;

import static org.openrewrite.java.Assertions.java;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

/**
 * Before/after coverage for the declarative recipes in
 * {@code META-INF/rewrite/kora-1-to-2.yml}.
 *
 * <p>Kora itself is not on the test classpath, so the 1.x types the recipes match on are
 * declared as stubs. That is enough for type attribution and keeps the tests independent
 * of any published Kora artifact.
 */
class Kora1To2Test implements RewriteTest {

    private static final String[] KORA_1_X_STUBS = {
            """
            package ru.tinkoff.kora.common;
            public @interface Component {}
            """,
            """
            package ru.tinkoff.kora.common;
            public @interface KoraApp {}
            """,
            """
            package ru.tinkoff.kora.common;
            public @interface Tag { Class<?>[] value(); }
            """,
            """
            package ru.tinkoff.kora.json.module;
            public interface JsonModule {}
            """,
            """
            package ru.tinkoff.kora.http.server.undertow;
            public interface UndertowHttpServerModule {}
            """,
            """
            package jakarta.annotation;
            public @interface Nullable {}
            """,
            // 2.0 type, present only so the idempotency test has attributed types
            """
            package io.koraframework.common.annotation;
            public @interface Component {}
            """,
    };

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("io.koraframework.migration.Kora1To2")
                .parser(JavaParser.fromJavaVersion().dependsOn(KORA_1_X_STUBS));
    }

    @Test
    void relocatesPackagesAndMovesDiAnnotations() {
        rewriteRun(
                java(
                        """
                        import ru.tinkoff.kora.common.Component;

                        @Component
                        public class PetService {}
                        """,
                        """
                        import io.koraframework.common.annotation.Component;

                        @Component
                        public class PetService {}
                        """
                )
        );
    }

    @Test
    void movesApplicationAndTagAnnotationsTogether() {
        rewriteRun(
                java(
                        """
                        import ru.tinkoff.kora.common.KoraApp;
                        import ru.tinkoff.kora.common.Tag;

                        @KoraApp
                        public interface Application {
                            @Tag(Application.class)
                            String name();
                        }
                        """,
                        """
                        import io.koraframework.common.annotation.KoraApp;
                        import io.koraframework.common.annotation.Tag;

                        @KoraApp
                        public interface Application {
                            @Tag(Application.class)
                            String name();
                        }
                        """
                )
        );
    }

    @Test
    void renamesTypesThatChangedModule() {
        rewriteRun(
                java(
                        """
                        import ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule;
                        import ru.tinkoff.kora.json.module.JsonModule;

                        public interface Application extends UndertowHttpServerModule, JsonModule {}
                        """,
                        """
                        import io.koraframework.http.server.undertow.UndertowPublicHttpServerModule;
                        import io.koraframework.json.common.JsonModule;

                        public interface Application extends UndertowPublicHttpServerModule, JsonModule {}
                        """
                )
        );
    }

    @Test
    void replacesJakartaNullableWithJSpecify() {
        rewriteRun(
                java(
                        """
                        import jakarta.annotation.Nullable;

                        public class PetService {
                            @Nullable
                            String name() {
                                return null;
                            }
                        }
                        """,
                        """
                        import org.jspecify.annotations.Nullable;

                        public class PetService {
                            @Nullable
                            String name() {
                                return null;
                            }
                        }
                        """
                )
        );
    }

    /**
     * The recipe has to be safe to run twice: the migration script and the recipe overlap,
     * and a project half migrated by hand is the normal case rather than the exception.
     */
    @Test
    void leavesAlreadyMigratedCodeAlone() {
        rewriteRun(
                java(
                        """
                        import io.koraframework.common.annotation.Component;

                        @Component
                        public class PetService {}
                        """
                )
        );
    }
}
