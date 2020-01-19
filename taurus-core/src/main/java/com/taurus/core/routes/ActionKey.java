package com.taurus.core.routes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ActionKey is used to configure actionKey for method of controller.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ActionKey {
	String value();
	
	int validate() default 0;
	
	/**参数{"a","1","b","2"}*/
	String[] params() default {};
}

