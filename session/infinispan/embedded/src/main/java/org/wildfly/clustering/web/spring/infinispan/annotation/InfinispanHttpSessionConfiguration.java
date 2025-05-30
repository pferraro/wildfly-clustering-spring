/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.web.spring.infinispan.annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.session.config.annotation.web.http.SpringHttpSessionConfiguration;
import org.wildfly.clustering.web.spring.SessionMarshallerFactory;
import org.wildfly.clustering.web.spring.SessionPersistenceGranularity;

/**
 * @author Paul Ferraro
 * @deprecated Use {@link org.wildfly.clustering.spring.session.infinispan.embedded.config.InfinispanHttpSessionConfiguration} instead.
 */
@Deprecated(forRemoval = true)
@Configuration(proxyBeanMethods = false)
@Import(SpringHttpSessionConfiguration.class)
public class InfinispanHttpSessionConfiguration extends org.wildfly.clustering.spring.session.infinispan.embedded.config.InfinispanHttpSessionConfiguration {

	@Autowired(required = false)
	public void setMarshallerFactory(SessionMarshallerFactory marshallerFactory) {
		this.setMarshaller(marshallerFactory);
	}

	@Autowired(required = false)
	public void setGranularity(SessionPersistenceGranularity granularity) {
		this.setPersistenceStrategy(granularity.get());
	}

	@Override
	public void setImportMetadata(AnnotationMetadata metadata) {
		AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(EnableInfinispanHttpSession.class.getName()));
		AnnotationAttributes manager = attributes.getAnnotation("manager");
		this.setMaxActiveSessions(manager.getNumber("maxActiveSessions").intValue());
		this.setMarshallerFactory(manager.getEnum("marshallerFactory"));
		SessionPersistenceGranularity granularity = manager.getEnum("granularity");
		this.setGranularity(granularity);
		this.accept(attributes);
	}
}
