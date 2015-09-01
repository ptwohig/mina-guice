package org.apache.mina.guice;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.filterchain.IoFilterChainBuilder;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.guice.filter.GuiceIoFilterChainBuilder;
import org.apache.mina.guice.filter.InjectProtocolCodecFactory;
import org.apache.mina.guice.filter.InjectProtocolCodecFilter;

import javax.inject.Provider;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Configures MINA to integrate with Guice.  This sets up the basic Guice bindings
 * for MINA as well as provides some extra options to configure the module.  This
 * provides support for scoping of the {@link IoSession} as well.
 * 
 * @author "Patrick Twohig" patrick@namazustudios.com
 * 
 */
public abstract class MinaModule extends AbstractModule {

    public static final String ORIGINAL_FILTER_SEQUENCE = "org.apache.mina.guice.MinaModule.ORIGINAL_FILTER_SEQUENCE";

	private static final Matcher<Method> IO_FILTER_EXCEPTION_CAUGHT = 
		method("exceptionCaught", NextFilter.class, IoSession.class, Throwable.class); 

	private static final Matcher<Method> IO_FILTER_FILTER_CLOSE = 
		method("filterClose", NextFilter.class, IoSession.class); 

	private static final Matcher<Method> IO_FILTER_FILTER_WRITE = 
		method("filterWrite", NextFilter.class, IoSession.class); 

	private static final Matcher<Method> IO_FILTER_MESSAGE_RECEIVED = 
		method("messageReceived", NextFilter.class, IoSession.class, Object.class); 

	private static final Matcher<Method> IO_FILTER_MESSAGE_SENT = 
		method("messageSent", NextFilter.class, IoSession.class, WriteRequest.class); 

	private static final Matcher<Method> IO_FILTER_SESSION_CLOSED = 
		method("sessionClosed", NextFilter.class, IoSession.class); 

	private static final Matcher<Method> IO_FILTER_SESSION_CREATED = 
		method("sessionCreated", NextFilter.class, IoSession.class); 

	private static final Matcher<Method> IO_FILTER_SESSION_IDLE = 
		method("sessionIdle", NextFilter.class, IoSession.class); 

	private static final Matcher<Method> IO_FILTER_SESSION_OPENED = 
		method("sessionOpened", NextFilter.class, IoSession.class); 

	private static final Matcher<Method> IO_FILTER_ON_POST_ADD = 
		method("onPostAdd", IoFilterChain.class, String.class, NextFilter.class); 

	private static final Matcher<Method> IO_FILTER_ON_POST_REMOVE = 
		method("onPostRemove", IoFilterChain.class, String.class, NextFilter.class); 

	private static final Matcher<Method> IO_FILTER_ON_PRE_ADD = 
		method("onPreAdd", IoFilterChain.class, String.class, NextFilter.class); 

	private static final Matcher<Method> IO_FILTER_ON_PRE_REMOVE = 
		method("onPreRemove", IoFilterChain.class, String.class, NextFilter.class); 

	private static final Matcher<Method> IO_HANDLER_EXCEPTION_CAUGHT = 
		method("exceptionCaught", IoSession.class, Throwable.class);

	private static final Matcher<Method> IO_HANDLER_MESSAGE_RECEIVED = 
		method("messageReceived", IoSession.class, Object.class);

	private static final Matcher<Method> IO_HANDLER_MESSAGE_SENT = 
		method("messageSent", IoSession.class, Object.class);

	private static final Matcher<Method> IO_HANDLER_SESSION_CLOSED = 
		method("sessionClosed", IoSession.class);

	private static final Matcher<Method> IO_HANDLER_SESSION_CREATED = 
		method("sessionCreated", IoSession.class);

	private static final Matcher<Method> IO_HANDLER_SESSION_IDLE = 
		method("sessionIdle", IoSession.class, IdleStatus.class);

	private static final Matcher<Method> IO_HANDLER_SESSION_OPENED = 
		method("sessionOpened", IoSession.class);

	private static final Matcher<? super TypeLiteral<?>> IO_ACCEPTOR_MATCHER =
			new AbstractMatcher<TypeLiteral<?>>() {

		@Override
		public boolean matches(TypeLiteral<?> t) {
			System.out.println(t);
			return TypeUtil.isLiteralAssignableFrom(IoAcceptor.class, t);
		}

	};

    private final LinkedList<String> filterNameList = new LinkedList<>();

	/**
	 * Sets up the basic MINA Guice integration.  This will bind the basic 
	 * framework necessary to instantiate MINA.  Additionally, this will
	 * bind the appropraite {@link MethodInterceptor}s to manage the scope
	 * within MINA.
	 * 
	 * This is marked as final, so you must provide your application-specific
	 * configuration by overriding the {@link MinaModule#configureMINA()} method.
	 */
	@Override
	protected final void configure() {

		binder().bindListener(IO_ACCEPTOR_MATCHER, new TypeListener() {

            @Override
            public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {

                final Provider<IoHandler> ioHandler = encounter.getProvider(IoHandler.class);
                final Provider<IoFilterChainBuilder> guiceIoFilterChainBuilder = encounter.getProvider(IoFilterChainBuilder.class);

                encounter.register(new InjectionListener<I>() {

                    @Override
                    public void afterInjection(I injectee) {
                        final IoAcceptor acceptor = (IoAcceptor) injectee;
                        acceptor.setHandler(ioHandler.get());
                        acceptor.setFilterChainBuilder(guiceIoFilterChainBuilder.get());
                    }

                });

            }

        });

		binder().bindInterceptor(Matchers.subclassesOf(IoFilter.class), IO_FILTER_EXCEPTION_CAUGHT, new SessionScopeMethodInterceptor(1));
		binder().bindInterceptor(Matchers.subclassesOf(IoFilter.class), IO_FILTER_FILTER_CLOSE, new SessionScopeMethodInterceptor(1));
		binder().bindInterceptor(Matchers.subclassesOf(IoFilter.class), IO_FILTER_FILTER_WRITE, new SessionScopeMethodInterceptor(1));
		binder().bindInterceptor(Matchers.subclassesOf(IoFilter.class), IO_FILTER_MESSAGE_RECEIVED, new SessionScopeMethodInterceptor(1));
		binder().bindInterceptor(Matchers.subclassesOf(IoFilter.class), IO_FILTER_MESSAGE_SENT, new SessionScopeMethodInterceptor(1));
		binder().bindInterceptor(Matchers.subclassesOf(IoFilter.class), IO_FILTER_SESSION_CLOSED, new SessionScopeMethodInterceptor(1));
		binder().bindInterceptor(Matchers.subclassesOf(IoFilter.class), IO_FILTER_SESSION_CREATED, new SessionScopeMethodInterceptor(1));
		binder().bindInterceptor(Matchers.subclassesOf(IoFilter.class), IO_FILTER_SESSION_IDLE, new SessionScopeMethodInterceptor(1));
		binder().bindInterceptor(Matchers.subclassesOf(IoFilter.class), IO_FILTER_SESSION_OPENED, new SessionScopeMethodInterceptor(1));

		binder().bindInterceptor(Matchers.subclassesOf(IoFilter.class), IO_FILTER_ON_POST_ADD, new FilterChainMethodInterceptor(0));
		binder().bindInterceptor(Matchers.subclassesOf(IoFilter.class), IO_FILTER_ON_POST_REMOVE, new FilterChainMethodInterceptor(0));
		binder().bindInterceptor(Matchers.subclassesOf(IoFilter.class), IO_FILTER_ON_PRE_ADD, new FilterChainMethodInterceptor(0));
		binder().bindInterceptor(Matchers.subclassesOf(IoFilter.class), IO_FILTER_ON_PRE_REMOVE, new FilterChainMethodInterceptor(0));

		binder().bindInterceptor(Matchers.subclassesOf(IoHandler.class), IO_HANDLER_EXCEPTION_CAUGHT, new SessionScopeMethodInterceptor(0));
		binder().bindInterceptor(Matchers.subclassesOf(IoHandler.class), IO_HANDLER_MESSAGE_RECEIVED, new SessionScopeMethodInterceptor(0));
		binder().bindInterceptor(Matchers.subclassesOf(IoHandler.class), IO_HANDLER_MESSAGE_SENT, new SessionScopeMethodInterceptor(0));
		binder().bindInterceptor(Matchers.subclassesOf(IoHandler.class), IO_HANDLER_SESSION_CLOSED, new SessionScopeMethodInterceptor(0));
		binder().bindInterceptor(Matchers.subclassesOf(IoHandler.class), IO_HANDLER_SESSION_CREATED, new SessionScopeMethodInterceptor(0));
		binder().bindInterceptor(Matchers.subclassesOf(IoHandler.class), IO_HANDLER_SESSION_IDLE, new SessionScopeMethodInterceptor(0));
		binder().bindInterceptor(Matchers.subclassesOf(IoHandler.class), IO_HANDLER_SESSION_OPENED, new SessionScopeMethodInterceptor(0));

		configureMINA();

        binder().bind(new TypeLiteral<List<String>>(){})
                .annotatedWith(Names.named(ORIGINAL_FILTER_SEQUENCE))
                .toInstance(filterNameList);

    }

	/**
	 * Binds a filter in the filter chain.  You can specify the name of the filter to be handed to
     * to the {@link IoFilterChain#addLast(String, IoFilter)} method
	 * 
	 * @return a FilterNameBindingBuilder
	 * 
	 */
	protected final FilterNameBindingBuilder bindFilter() {
		return new FilterNameBindingBuilder() {
            @Override
            public FilterSequenceBindingBuilder named(final String filterName) {
                return bindFilterNamed(filterName);
            }
        };
	}

    private final FilterSequenceBindingBuilder bindFilterNamed(final String filterName) {
        return new FilterSequenceBindingBuilder() {

            @Override
            public LinkedBindingBuilder<? extends IoFilter> atBeginningOfChain() {

                final int index = filterNameList.indexOf(filterName);

                if (index >= 0) {
                    throw new IllegalArgumentException("Filter named \"" + filterName + "\" is already bound.");
                }

                filterNameList.addFirst(filterName);

                return binder().bind(IoFilter.class).annotatedWith(Names.named(filterName));

            }

            @Override
            public LinkedBindingBuilder<? extends IoFilter> after(String filterName) {

                final int index = filterNameList.indexOf(filterName);

                if (index < 0) {
                    throw new IllegalArgumentException("Filter named \"" + filterName + "\" is not bound.");
                }

                filterNameList.add(Math.min(index + 1, filterNameList.size()), filterName);

                return binder().bind(IoFilter.class).annotatedWith(Names.named(filterName));

            }

            @Override
            public LinkedBindingBuilder<? extends IoFilter> before(String filterName) {

                final int index = filterNameList.indexOf(filterName);

                if (index < 0) {
                    throw new IllegalArgumentException("Filter named \"" + filterName + "\" is not bound.");
                }

                filterNameList.add(index, filterName);

                return binder().bind(IoFilter.class).annotatedWith(Names.named(filterName));
            }

            @Override
            public LinkedBindingBuilder<? extends IoFilter> atAndOfFilterChain() {

                final int index = filterNameList.indexOf(filterName);

                if (index >= 0) {
                    throw new IllegalArgumentException("Filter named \"" + filterName + "\" is already bound.");
                }

                filterNameList.addLast(filterName);

                return binder().bind(IoFilter.class).annotatedWith(Names.named(filterName));

            }

        };
    }

    /**
     * Binds the {@link IoFilterChainBuilder} to {@link GuiceIoFilterChainBuilder}.  It is
     * not strictly necessary to bind this, but it makes life easier.
     */
    protected final void bindFilterChainBuilder() {
        binder().bind(IoFilterChainBuilder.class).to(GuiceIoFilterChainBuilder.class);
    }

    /**
     * Binds the {@link InjectProtocolCodecFactory} which will use the IoC container to install the various protocol
     * factories.
     */
    protected final void bindProtocolCodecFactory() {
        binder().bind(InjectProtocolCodecFactory.class);
    }

    /**
     * Binds the {@link IoSession} to the {@link IoSessionProvider}.
     */
    protected final void bindIoSession() {
        binder().bind(IoSession.class).toProvider(IoSessionProvider.class);
    }

    /**
     * Binds the {@link IoSession} to the {@link IoSessionProxyProvider}.
     */
    protected final void bindIoSessionProxy() {
        binder().bind(IoSession.class).toProvider(IoSessionProxyProvider.class);
    }

    /**
	 * Configures MINA using Guice. In this method you may bind all of the
	 * filters and other types specific to MINA.
	 */
	protected abstract void configureMINA();

	private static final class SessionScopeMethodInterceptor implements MethodInterceptor {

		private final int index;

		public SessionScopeMethodInterceptor(final int index) {
			this.index = index;
		}

		@Override
		public Object invoke(MethodInvocation invocation) throws Throwable {

			final IoSession session = (IoSession) invocation.getArguments()[index];

			try {
				IoSessionProvider.setSession(session);
				return invocation.proceed();
			} finally {
				IoSessionProvider.freeSession(session);
			}

		}

	}

	private static final class FilterChainMethodInterceptor implements MethodInterceptor {

		private final int index;

		public FilterChainMethodInterceptor(final int index) {
			this.index = index;
		}

		@Override
		public Object invoke(MethodInvocation invocation) throws Throwable {

			final IoSession session = ((IoFilterChain) invocation.getArguments()[index]).getSession();

			try {
				IoSessionProvider.setSession(session);
				return invocation.proceed();
			} finally {
				IoSessionProvider.freeSession(session);
			}

		}

	}

	private static final Matcher<Method> method(final String name, final Class<?> ... types) {

		return new AbstractMatcher<Method>() {

			@Override
			public boolean matches(Method t) {
				return 
					t.getName().equals(name) &&
					Arrays.deepEquals(types, t.getParameterTypes());
			}

		};

	}

}
