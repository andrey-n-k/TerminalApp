/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.cache.db.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for indelicate reference field.<br/>
 * Note: this field must be used with {@link FieldName} annotation
 *
 * @author Swift
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Reference {

	/**
	 * @return type of reference Note: "Many to many" are not supported
	 */
	ReferenceType type() default ReferenceType.MANY_TO_ONE;

	/**
	 * @return class of relation. Class must be annotated by {@link TableName}
	 */
	Class referenceClass();
}