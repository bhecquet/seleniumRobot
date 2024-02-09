package com.seleniumtests.uipage.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Annotation that say this method or page should be handled in the provided context
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({TYPE})
public @interface Context {
    public String name();

}
