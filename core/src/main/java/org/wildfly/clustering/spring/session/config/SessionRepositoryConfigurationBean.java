/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.spring.session.config;

import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import jakarta.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.session.IndexResolver;
import org.springframework.session.PrincipalNameIndexResolver;
import org.springframework.session.Session;
import org.springframework.web.context.ServletContextAware;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.session.SessionAttributePersistenceStrategy;
import org.wildfly.clustering.spring.session.SessionMarshallerFactory;
import org.wildfly.clustering.spring.session.SessionRepositoryConfiguration;

/**
 * Spring configuration bean for a distributable session repository.
 * @author Paul Ferraro
 */
public class SessionRepositoryConfigurationBean implements SessionRepositoryConfiguration, ServletContextAware, ApplicationEventPublisherAware {

	public static final String DEFAULT_SPRING_SECURITY_INDEX_ID = "SPRING_SECURITY_CONTEXT";
	public static final String DEFAULT_STRING_SECURITY_INDEX_NAME = "org.springframework.session.FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME";
	public static final Map<String, String> DEFAULT_SPRING_SECURITY_INDEXES = Map.of(DEFAULT_SPRING_SECURITY_INDEX_ID, DEFAULT_STRING_SECURITY_INDEX_NAME);
	public static final IndexResolver<Session> DEFAULT_SPRING_SECURITY_INDEX_RESOLVER = new PrincipalNameIndexResolver<>();

	private OptionalInt maxActiveSessions = OptionalInt.empty();
	private SessionAttributePersistenceStrategy persistenceStrategy = SessionAttributePersistenceStrategy.COARSE;
	private Function<ClassLoader, ByteBufferMarshaller> marshallerFactory = SessionMarshallerFactory.JBOSS;
	private Supplier<String> identifierFactory = () -> UUID.randomUUID().toString();
	private ApplicationEventPublisher publisher;
	private ServletContext context;
	private Map<String, String> indexes;
	private IndexResolver<Session> indexResolver;

	protected SessionRepositoryConfigurationBean(Map<String, String> indexes, IndexResolver<Session> indexResolver) {
		this.indexes = indexes;
		this.indexResolver = indexResolver;
	}

	@Override
	public OptionalInt getMaxActiveSessions() {
		return this.maxActiveSessions;
	}

	@Override
	public SessionAttributePersistenceStrategy getPersistenceStrategy() {
		return this.persistenceStrategy;
	}

	@Override
	public Function<ClassLoader, ByteBufferMarshaller> getMarshallerFactory() {
		return this.marshallerFactory;
	}

	@Override
	public Supplier<String> getIdentifierFactory() {
		return this.identifierFactory;
	}

	@Override
	public ApplicationEventPublisher getEventPublisher() {
		return this.publisher;
	}

	@Override
	public ServletContext getServletContext() {
		return this.context;
	}

	@Override
	public Map<String, String> getIndexes() {
		return this.indexes;
	}

	@Override
	public IndexResolver<Session> getIndexResolver() {
		return this.indexResolver;
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	@Override
	public void setServletContext(ServletContext context) {
		this.context = context;
	}

	@Autowired(required = false)
	public void setGranularity(Supplier<SessionAttributePersistenceStrategy> granularity) {
		this.persistenceStrategy = granularity.get();
	}

	@Autowired(required = false)
	public void setPersistenceStrategy(SessionAttributePersistenceStrategy persistenceStrategy) {
		this.persistenceStrategy = persistenceStrategy;
	}

	@Autowired(required = false)
	public void setMarshallerFactory(Function<ClassLoader, ByteBufferMarshaller> marshallerFactory) {
		this.marshallerFactory = marshallerFactory;
	}

	@Autowired(required = false)
	public void setMaxActiveSessions(int maxActiveSessions) {
		this.maxActiveSessions = (maxActiveSessions >= 0) ? OptionalInt.of(maxActiveSessions) : OptionalInt.empty();
	}

	@Autowired(required = false)
	public void setIndexes(Map<String, String> indexes) {
		this.indexes = indexes;
	}

	@Autowired(required = false)
	public void setIndexResolver(IndexResolver<Session> resolver) {
		this.indexResolver = resolver;
	}
}