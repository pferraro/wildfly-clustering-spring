/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.spring.context.infinispan.remote.cache;

import java.util.function.BiFunction;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheContainer;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.Cache;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.wildfly.clustering.marshalling.ByteBufferMarshalledKeyFactory;
import org.wildfly.clustering.marshalling.ByteBufferMarshalledValueFactory;
import org.wildfly.clustering.marshalling.ByteBufferMarshaller;
import org.wildfly.clustering.marshalling.MarshalledValueFactory;
import org.wildfly.clustering.spring.context.cache.AbstractCacheManager;

/**
 * @author Paul Ferraro
 *
 */
public class HotRodCacheManager extends AbstractCacheManager {

	private final RemoteCacheContainer container;

	public HotRodCacheManager(RemoteCacheContainer container, BiFunction<Environment, ResourceLoader, ByteBufferMarshaller> marshallerFactory) {
		super(container, marshallerFactory);
		this.container = container;
	}

	@Override
	public @Nullable Cache getCache(String name) {
		RemoteCacheContainer container = this.container;
		ByteBufferMarshaller marshaller = this.getMarshaller();
		MarshalledValueFactory<ByteBufferMarshaller> keyFactory = new ByteBufferMarshalledKeyFactory(marshaller);
		MarshalledValueFactory<ByteBufferMarshaller> valueFactory = new ByteBufferMarshalledValueFactory(marshaller);
		return new HotRodCache<>(new HotRodCache.Configuration<ByteBufferMarshaller>() {
			@Override
			public MarshalledValueFactory<ByteBufferMarshaller> getKeyMarshalledValueFactory() {
				return keyFactory;
			}

			@Override
			public MarshalledValueFactory<ByteBufferMarshaller> getValueMarshalledValueFactory() {
				return valueFactory;
			}

			@Override
			public <K, V> RemoteCache<K, V> getCache() {
				return container.getCache(name);
			}
		});
	}
}
