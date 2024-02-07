/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.clustering.spring.session;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Optional;
import java.util.function.Consumer;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionActivationListener;
import jakarta.servlet.http.HttpSessionEvent;

import org.wildfly.clustering.session.ImmutableSession;
import org.wildfly.clustering.session.container.ContainerFacadeProvider;

/**
 * @author Paul Ferraro
 */
public enum JakartaServletFacadeProvider implements ContainerFacadeProvider<HttpSession, ServletContext, HttpSessionActivationListener> {
	INSTANCE;

	@Override
	public HttpSession asSession(ImmutableSession session, ServletContext context) {
		return new AbstractHttpSession() {
			@Override
			public String getId() {
				return session.getId();
			}

			@Override
			public ServletContext getServletContext() {
				return context;
			}

			@Override
			public boolean isNew() {
				return session.getMetaData().isNew();
			}

			@Override
			public long getCreationTime() {
				return session.getMetaData().getCreationTime().toEpochMilli();
			}

			@Override
			public long getLastAccessedTime() {
				return session.getMetaData().getLastAccessStartTime().toEpochMilli();
			}

			@Override
			public int getMaxInactiveInterval() {
				return (int) session.getMetaData().getTimeout().getSeconds();
			}

			@Override
			public Enumeration<String> getAttributeNames() {
				return Collections.enumeration(session.getAttributes().getAttributeNames());
			}

			@Override
			public Object getAttribute(String name) {
				return session.getAttributes().getAttribute(name);
			}

			@Override
			public void setAttribute(String name, Object value) {
				// Ignore
			}

			@Override
			public void removeAttribute(String name) {
				// Ignore
			}

			@Override
			public void invalidate() {
				// Ignore
			}

			@Override
			public void setMaxInactiveInterval(int interval) {
				// Ignore
			}
		};
	}

	@Override
	public Optional<HttpSessionActivationListener> asSessionActivationListener(Object attribute) {
		return Optional.ofNullable(attribute).filter(HttpSessionActivationListener.class::isInstance).map(HttpSessionActivationListener.class::cast);
	}

	@Override
	public Consumer<HttpSession> prePassivateNotifier(HttpSessionActivationListener listener) {
		return new Consumer<>() {
			@Override
			public void accept(HttpSession session) {
				listener.sessionWillPassivate(new HttpSessionEvent(session));
			}
		};
	}

	@Override
	public Consumer<HttpSession> postActivateNotifier(HttpSessionActivationListener listener) {
		return new Consumer<>() {
			@Override
			public void accept(HttpSession session) {
				listener.sessionDidActivate(new HttpSessionEvent(session));
			}
		};
	}

	@Override
	public HttpSessionActivationListener asSessionActivationListener(Consumer<HttpSession> prePassivate, Consumer<HttpSession> postActivate) {
		return new HttpSessionActivationListener() {
			@Override
			public void sessionWillPassivate(HttpSessionEvent event) {
				prePassivate.accept(event.getSession());
			}

			@Override
			public void sessionDidActivate(HttpSessionEvent event) {
				postActivate.accept(event.getSession());
			}
		};
	}
}
