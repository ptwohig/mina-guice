package org.apache.mina.guice;

import org.apache.mina.core.session.IoSession;

import javax.inject.Provider;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Instead of injecting the {@link IoSession} on demand as the {@link IoSessionProvider} does, this
 * will return a {@link Proxy} backed instance of the underlying {@link IoSession}.
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
