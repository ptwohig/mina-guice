package org.apache.mina.guice;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

import java.util.concurrent.Callable;

/**
 * A set of scopes for Mina.
 * 
 * @author "Patrick Twohig" patrick@namazustudios.com
 *
 */
public class MinaScopes {

	/**
	 * Lazily injects a type into the session on-demand.
	 */
	public static final Scope SESSION = new Scope() {

		@Override
		public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
            final IoSession
			return new Provider<T>() {

				@SuppressWarnings("unchecked")
				@Override
				public T get() {

					final IoSession session = IoSessionProvider.getSession();

					T obj = (T) session.getAttribute(key);
					if (obj != null) return obj;

					obj = unscoped.get();
					session.setAttribute(key, obj);
					return obj;

				}

				public String toString() {
					return String.format("%s[%s]", key, SESSION);
				}

			};
		}

		public String toString() {
			return "MinaScopes.SESSION";
		}

	};

	/**
	 * Given the {@link ConnectFuture}, this will wait for the connection to succeed, and then execute the
     * given {@link Callable} immediately after a successful call.
     *
     * This is useful because you may want to bootstrap {@link IoSession} scope bound objects immediately
     * upon connection, and this will enusre that the calling thread will ahve all the appropraite scoping
     * properly configured.
     *
	 * @param connectFuture
	 * @param callable
	 * @param <T>
	 * @return
	 * @throws Exception
	 */
	public static <T> T bootstrap(final ConnectFuture connectFuture, final Callable<T> callable) throws Exception {

        final IoSession ioSession = connectFuture.awaitUninterruptibly().getSession();

        try {
            IoSessionProvider.setSession(ioSession);
            return callable.call();
        } finally {
            IoSessionProvider.freeSession(ioSession);
        }

    }

}
