/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.spring.context.infinispan.embedded;

import java.util.OptionalInt;

import org.infinispan.Cache;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.StorageType;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.transaction.TransactionMode;
import org.infinispan.transaction.tm.EmbeddedTransactionManager;
import org.springframework.beans.factory.InitializingBean;
import org.wildfly.clustering.cache.infinispan.embedded.container.DataContainerConfigurationBuilder;
import org.wildfly.clustering.server.group.GroupCommandDispatcherFactory;
import org.wildfly.clustering.server.infinispan.dispatcher.CacheContainerCommandDispatcherFactory;
import org.wildfly.clustering.server.infinispan.dispatcher.ChannelEmbeddedCacheManagerCommandDispatcherFactoryConfiguration;
import org.wildfly.clustering.server.infinispan.dispatcher.EmbeddedCacheManagerCommandDispatcherFactory;
import org.wildfly.clustering.server.infinispan.dispatcher.LocalEmbeddedCacheManagerCommandDispatcherFactoryConfiguration;
import org.wildfly.clustering.server.jgroups.ChannelGroupMember;
import org.wildfly.clustering.session.SessionManager;
import org.wildfly.clustering.session.SessionManagerConfiguration;
import org.wildfly.clustering.session.SessionManagerFactory;
import org.wildfly.clustering.session.SessionManagerFactoryConfiguration;
import org.wildfly.clustering.session.infinispan.embedded.InfinispanSessionManagerFactory;
import org.wildfly.clustering.session.infinispan.embedded.InfinispanSessionManagerFactoryConfiguration;
import org.wildfly.clustering.session.infinispan.embedded.metadata.SessionMetaDataKey;
import org.wildfly.clustering.session.spec.SessionEventListenerSpecificationProvider;
import org.wildfly.clustering.session.spec.SessionSpecificationProvider;
import org.wildfly.clustering.spring.context.AutoDestroyBean;

/**
 * @author Paul Ferraro
 * @param <S> session type
 * @param <C> session manager context type
 * @param <L> session passivation listener type
 */
public class InfinispanSessionManagerFactoryBean<S, C, L> extends AutoDestroyBean implements SessionManagerFactory<C, Void>, InitializingBean {

	private final SessionManagerFactoryConfiguration<Void> configuration;
	private final SessionSpecificationProvider<S, C> sessionProvider;
	private final SessionEventListenerSpecificationProvider<S, L> listenerProvider;
	private final InfinispanConfiguration infinispan;
	private final ChannelEmbeddedCacheManagerCommandDispatcherFactoryConfiguration embeddedCacheManagerConfiguration;

	private SessionManagerFactory<C, Void> sessionManagerFactory;

	public InfinispanSessionManagerFactoryBean(SessionManagerFactoryConfiguration<Void> configuration, SessionSpecificationProvider<S, C> sessionProvider, SessionEventListenerSpecificationProvider<S, L> listenerProvider, InfinispanConfiguration infinispan, ChannelEmbeddedCacheManagerCommandDispatcherFactoryConfiguration embeddedCacheManagerConfiguration) {
		this.configuration = configuration;
		this.sessionProvider = sessionProvider;
		this.listenerProvider = listenerProvider;
		this.infinispan = infinispan;
		this.embeddedCacheManagerConfiguration = embeddedCacheManagerConfiguration;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		EmbeddedCacheManager container = this.embeddedCacheManagerConfiguration.getCacheContainer();
		String templateName = this.infinispan.getTemplateName();
		Configuration template = (templateName != null) ? container.getCacheConfiguration(templateName) : container.getDefaultCacheConfiguration();
		if (template == null) {
			throw new IllegalArgumentException(templateName);
		}
		ConfigurationBuilder builder = new ConfigurationBuilder().read(template).template(false);
		builder.encoding().mediaType(MediaType.APPLICATION_OBJECT_TYPE);

		if (template.invocationBatching().enabled()) {
			builder.transaction().transactionMode(TransactionMode.TRANSACTIONAL).transactionManagerLookup(EmbeddedTransactionManager::getInstance);
		}

		// Disable expiration
		builder.expiration().lifespan(-1).maxIdle(-1).disableReaper().wakeUpInterval(-1);

		OptionalInt maxActiveSessions = this.configuration.getMaxActiveSessions();
		EvictionStrategy eviction = maxActiveSessions.isPresent() ? EvictionStrategy.REMOVE : EvictionStrategy.MANUAL;
		builder.memory().storage(StorageType.HEAP)
				.whenFull(eviction)
				.maxCount(maxActiveSessions.orElse(-1))
				;
		if (eviction.isEnabled()) {
			// Only evict meta-data entries
			// We will cascade eviction to the remaining entries for a given session
			builder.addModule(DataContainerConfigurationBuilder.class).evictable(SessionMetaDataKey.class::isInstance);
		}

		String applicationName = this.configuration.getDeploymentName();
		container.defineConfiguration(applicationName, builder.build());
		this.accept(() -> container.undefineConfiguration(applicationName));

		GroupCommandDispatcherFactory<org.jgroups.Address, ChannelGroupMember> channelCommandDispatcherFactory = this.embeddedCacheManagerConfiguration.getCommandDispatcherFactory();
		CacheContainerCommandDispatcherFactory commandDispatcherFactory = (channelCommandDispatcherFactory != null) ? new EmbeddedCacheManagerCommandDispatcherFactory<>(new ChannelEmbeddedCacheManagerCommandDispatcherFactoryConfiguration() {
			@Override
			public GroupCommandDispatcherFactory<org.jgroups.Address, ChannelGroupMember> getCommandDispatcherFactory() {
				return channelCommandDispatcherFactory;
			}

			@Override
			public EmbeddedCacheManager getCacheContainer() {
				return container;
			}
		}) : new EmbeddedCacheManagerCommandDispatcherFactory<>(new LocalEmbeddedCacheManagerCommandDispatcherFactoryConfiguration() {
			@Override
			public EmbeddedCacheManager getCacheContainer() {
				return container;
			}
		});

		Cache<?, ?> cache = container.getCache(applicationName);
		cache.start();
		this.accept(cache::stop);

		InfinispanSessionManagerFactoryConfiguration infinispanConfiguration = new InfinispanSessionManagerFactoryConfiguration() {
			@SuppressWarnings("unchecked")
			@Override
			public <K, V> Cache<K, V> getCache() {
				return (Cache<K, V>) cache;
			}

			@Override
			public CacheContainerCommandDispatcherFactory getCommandDispatcherFactory() {
				return commandDispatcherFactory;
			}
		};

		this.sessionManagerFactory = new InfinispanSessionManagerFactory<>(this.configuration, this.sessionProvider, this.listenerProvider, infinispanConfiguration);
		this.accept(this::close);
	}

	@Override
	public void close() {
		this.sessionManagerFactory.close();
	}

	@Override
	public SessionManager<Void> createSessionManager(SessionManagerConfiguration<C> configuration) {
		return this.sessionManagerFactory.createSessionManager(configuration);
	}
}
