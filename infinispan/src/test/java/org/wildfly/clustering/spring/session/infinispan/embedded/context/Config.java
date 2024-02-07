/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.spring.session.infinispan.embedded.context;

import org.wildfly.clustering.spring.session.SessionMarshallerFactory;
import org.wildfly.clustering.spring.session.SessionPersistenceGranularity;
import org.wildfly.clustering.spring.session.annotation.SessionManager;
import org.wildfly.clustering.spring.session.infinispan.embedded.annotation.EnableInfinispanHttpSession;
import org.wildfly.clustering.spring.session.infinispan.embedded.annotation.Infinispan;

/**
 * Test configuration for session manager.
 * @author Paul Ferraro
 */
@EnableInfinispanHttpSession(config = @Infinispan, manager = @SessionManager(marshallerFactory = SessionMarshallerFactory.PROTOSTREAM, granularity = SessionPersistenceGranularity.ATTRIBUTE, maxActiveSessions = 100))
public class Config {
}
