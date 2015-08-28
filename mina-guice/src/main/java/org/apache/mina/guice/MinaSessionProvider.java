package org.apache.mina.guice;

import org.apache.mina.core.session.IoSession;

import com.google.inject.Provider;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Provides the session object kept in an instance of {@link ThreadLocal}.  This makes it possible
 * to inject the current {@link IoSession}.
 * 
 * @author "Patrick Twohig" patrick@namazustudios.com
 *
 */
class MinaSessionProvider implements Provider<IoSession> {

	private static final ThreadLocal<IoSession> session = new ThreadLocal<IoSession>();

	@Override
	public IoSession get() {
		return MinaSessionProvider.getSession();
	}

	static IoSession getSession() {

		final IoSession session = MinaSessionProvider.session.get();
		if (session == null) throw new IllegalStateException("Out of session scope.");

		return session;

	}

    static void setSession(final IoSession session) {

		if (session == null) throw new IllegalArgumentException("Session cannot be null.");

		final IoSession tmp = MinaSessionProvider.session.get();
		if (tmp != null && !tmp.equals(session)) throw new IllegalStateException("Already in scope.");

		MinaSessionProvider.session.set(session);

	}

	static void freeSession(final IoSession session) {

		if (session == null) throw new IllegalArgumentException("Session cannot be null.");

		final IoSession tmp = MinaSessionProvider.session.get();
		if (tmp != null && !tmp.equals(session)) throw new IllegalStateException("Already in a different session's scope.");

		MinaSessionProvider.session.set(null);

	}

}
