/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.spring.context.infinispan.remote.config.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.wildfly.clustering.spring.context.infinispan.remote.HotRodConfiguration;

/**
 * Configures the HotRod client used by a session repository.
 * @author Paul Ferraro
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface HotRod {
	/**
	 * The HotRod URI.
	 * @return a HotRod URI.
	 */
	String uri();

	/**
	 * The Infinispan server configuration template from which to create a remote cache.
	 * @return an Infinispan server template
	 */
	String template() default "";

	/**
	 * The Infinispan server configuration template from which to create a remote cache.
	 * @return an Infinispan server template
	 */
	String configuration() default HotRodConfiguration.DEFAULT_CONFIGURATION;

	/**
	 * The HotRod client properties.
	 * @return an array of HotRod client properties.
	 */
	Property[] properties() default {};
}
