package com.example.dashboard.di;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD, FIELD, PARAMETER, TYPE})
public @interface EngineType {
    String value() default "";

    // Classe interna corrigida
    class Literal extends AnnotationLiteral<EngineType> implements EngineType {
        private final String value;

        public Literal(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }
    }
}