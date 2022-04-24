package com.celonis.demo.annotations;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Cacheable(unless = "true")
public @interface ReadOnlyCacheable {
    @AliasFor(annotation = Cacheable.class)
    String[] value() default {};

    @AliasFor(annotation = Cacheable.class)
    String[] cacheNames() default {};

    @AliasFor(annotation = Cacheable.class)
    String key() default "";

    @AliasFor(annotation = Cacheable.class)
    String keyGenerator() default "";

    @AliasFor(annotation = Cacheable.class)
    String cacheManager() default "";

    @AliasFor(annotation = Cacheable.class)
    String cacheResolver() default "";

    @AliasFor(annotation = Cacheable.class)
    String condition() default "";

    //force this annotation to be readonly
    //String unless() default "";

    @AliasFor(annotation = Cacheable.class)
    boolean sync() default false;
}