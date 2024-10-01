package com.seleniumtests;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be used in tests so that video is capture during test, helping debugging
 */
@Retention(RetentionPolicy.RUNTIME)
@Target ({ElementType.TYPE, ElementType.METHOD})
public @interface CaptureVideo {
	public boolean enabled() default true;
}