package org.apache.mina.guice;

import org.apache.mina.core.session.IoSession;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

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
			return new Provider<T>() {

				@SuppressWarnings("unchecked")
				@Override
				public T get() {

					final IoSession session = MinaSessionProvider.getSession();

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

}
