package org.apache.mina.guice;

import com.google.inject.OutOfScopeException;
import org.apache.mina.core.session.IoSession;

import com.google.inject.Provider;

/**
 * Provides the session object kept in an instance of {@link ThreadLocal}.  This makes it possible
 * to inject the current {@link IoSession}.
 *
 * Any call to this instance will throw an instance of {@link OutOfScopeException} if the session is not in scope.
 *
 * @author "Patrick Twohig" patrick@namazustudios.com
 *
 */
public class IoSessionProvider implements Provider<IoSession> {

	private static final ThreadLocal<IoSession> session = new ThreadLocal<IoSession>();

	@Override
	public IoSession get() {
		return IoSessionProvider.getSession();
	}

	static IoSession getSession() {

		final IoSession session = IoSessionProvider.session.get();
		if (session == null) throw new OutOfScopeException("Out of session scope.");

		return session;

	}

    static void setSession(final IoSession session) {

		if (session == null) throw new IllegalArgumentException("Session cannot be null.");

		final IoSession tmp = IoSessionProvider.session.get();
		if (tmp != null && !tmp.equals(session)) throw new OutOfScopeException("Already in scope.");

		IoSessionProvider.session.set(session);

	}

	static void freeSession(final IoSession session) {

		if (session == null) throw new IllegalArgumentException("Session cannot be null.");

		final IoSession tmp = IoSessionProvider.session.get();
		if (tmp != null && !tmp.equals(session)) throw new IllegalStateException("Already in a different session's scope.");

		IoSessionProvider.session.set(null);

	}

}
