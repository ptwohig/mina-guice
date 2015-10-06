package org.apache.mina.guice;

import com.google.inject.OutOfScopeException;
import org.apache.mina.core.session.IoSession;

import javax.inject.Provider;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Instead of injecting the {@link IoSession} on demand as the {@link IoSessionProvider} does, this
 * will return a {@link Proxy} backed instance of the underlying {@link IoSession}.
 *
 * This uses the {@link IoSessionProvider} class to find the session.
 *
 * Any call to the returned {@link IoSession} instance will throw an instance of {@link OutOfScopeException}
 * if the session is not in scope.
 *
 * Created by patricktwohig on 8/28/15.
 */
public class IoSessionProxyProvider implements Provider<IoSession> {

    @Override
    public IoSession get() {

        final ClassLoader classLoader = IoSessionProvider.class.getClassLoader();

        return (IoSession) Proxy.newProxyInstance(classLoader, new Class<?>[]{IoSession.class},
            new InvocationHandler() {

                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    final IoSession currentSession = IoSessionProvider.getSession();
                    return method.invoke(currentSession, args);
                }

            });

    }

}
