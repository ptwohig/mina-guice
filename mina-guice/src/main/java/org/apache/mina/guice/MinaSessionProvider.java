package org.apache.mina.guice;

import org.apache.mina.core.session.IoSession;

import com.google.inject.Provider;

/**
 * Provides the session object.
 * 
 * @author patricktwohig
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
