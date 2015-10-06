package org.apache.mina.guice;

import com.google.inject.Injector;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;

/**
 * A set of scopes for Mina.  This currently includes two scopes for the {@link IoSession}.  One which will ensure that
 * one instance of a type is stored in the {@link IoSession} and a second which will allow a proxy to be used
 * in {@link IoSession} instances.
 * 
 * @author "Patrick Twohig" patrick@namazustudios.com
 *
 */
public class MinaScopes {

	/**
	 * Lazily injects a type into the session on-demand.  The unscoped provided type is stored
     * in a session variable with the {@link Key} as the key.  Only one of the type is
     * scoped per session.
	 */
	public static final Scope SESSION = new Scope() {

		@Override
		public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {

			return new Provider<T>() {

				@SuppressWarnings("unchecked")
				@Override
				public T get() {
                    return getTypeFromSession(key, unscoped);
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
     * Can only be bound to interface types.  This returns a proxy for the desired type which doesn't
     * actually invoke the underlying provider until the the actual returned instance is first used.
     *
     * Upon the first method invocation, the underlying unscoped provider is invoked, the instance is
     * saved to the session, and then used to execute the actual business logic.  This is useful when
     * an object depends on the session, but must also be injected outside the session.
     */
    public static final Scope SESSION_PROXY = new Scope() {

        @Override
        public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {

            return new Provider<T>() {

                @Override
                @SuppressWarnings("unchecked")
                public T get() {
                    return getProxy(key.getTypeLiteral().getRawType());
                }

                public String toString() {
                    return String.format("%s[%s]", key, SESSION_PROXY);
                }

                private T getProxy(final Class<? super T> interfaceT) {

                    final Class<?>[] types = new Class<?>[] {interfaceT};

                    final Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), types,
                            new InvocationHandler() {
                                @Override
                                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                    final Object actual = getTypeFromSession(key, unscoped);
                                    return method.invoke(actual, args);
                                }
                            });

                    return (T) interfaceT.cast(proxy);

                }

            };

        }

        public String toString() {
            return "MinaScopes.SESSION_PROXY";
        }

    };

    private static <T> T getTypeFromSession(final Key<T> key, final Provider<T> unscoped) {

        final IoSession session = IoSessionProvider.getSession();

        T obj = (T) session.getAttribute(key);
        if (obj != null) return obj;

        obj = unscoped.get();
        session.setAttribute(key, obj);
        return obj;

    }

    /**
	 * Given the {@link ConnectFuture}, this will wait for the connection to succeed, and then execute the
     * given {@link Callable} immediately after a successful call.
     *
     * This is useful because you may want to bootstrap {@link IoSession} scope bound objects immediately
     * upon connection, and this will ensure that the calling thread will have all the appropriate scoping
     * properly configured.
     *
	 * @param connectFuture the {@link ConnectFuture} used to generate the {@link IoSession}
     * @param callable the {@link Callable} instance to obtain the desired type (presumably from an {@link Injector})
     * @param <T> the desired type
     * @return the instance of the desired type
	 * @throws Exception
	 */
	public static <T> T bootstrap(final ConnectFuture connectFuture, final Callable<T> callable) throws Exception {
        return bootstrap(new Callable<IoSession>() {
            @Override
            public IoSession call() throws Exception {
                return connectFuture.awaitUninterruptibly().getSession();
            }
        }, callable);
    }

    /**
     * Given two instances of {@link Callable} this will execute the first instance to obtain an instance of
     * {@link IoSession} and, apply the session to the scope and then run the second {@link Callable}.
     *
     * This is useful because you may want to bootstrap {@link IoSession} scope bound objects immediately
     * upon connection, and this will ensure that the calling thread will have all the appropriate scoping
     * properly configured.
     *
     * @param ioSessionCallable the {@link Callable} instance to obtain the {@link IoSession}
     * @param callable the {@link Callable} instance to obtain the desired type (presumably from an {@link Injector})
     * @param <T> the desired type
     * @return the instance of the desired type
     * @throws Exception if either {@link Callable} throws an exception
     */
    public static <T> T bootstrap(final Callable<IoSession> ioSessionCallable, final Callable<T> callable) throws Exception {

        final IoSession ioSession = ioSessionCallable.call();

        try {
            IoSessionProvider.setSession(ioSession);
            return callable.call();
        } finally {
            IoSessionProvider.freeSession(ioSession);
        }

    }

}
