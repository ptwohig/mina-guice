package org.apache.mina.guice;

import java.lang.reflect.Method;
import java.util.Arrays;

import javax.inject.Provider;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.guice.filter.GuiceProtocolCodecFactory;
import org.apache.mina.guice.filter.GuiceProtocolCodecFilter;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * Configures MINA to integrate with Guice.  This sets up the basic Guice bindings
 * for MINA as well as provides some extra options to configure the module.  This
 * provides support for scoping of the {@link IoSession} as well.
 * 
 * @author "Patrick Twohig" patrick@namazustudios.com
 * 
 */
public abstract class MinaModule extends AbstractModule {

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

	private MapBinder<String, IoFilter> filters;

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

		filters = MapBinder.newMapBinder(binder(), String.class, IoFilter.class);
		
		binder().bindListener(IO_ACCEPTOR_MATCHER, new TypeListener() {

			@Override
			public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {

				final Provider<IoHandler> ioHandler = encounter.getProvider(IoHandler.class);
				final Provider<GuiceIoFilterChainBuilder> guiceIoFilterChainBuilder = encounter.getProvider(GuiceIoFilterChainBuilder.class);

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

		binder().bind(GuiceIoFilterChainBuilder.class);
		binder().bind(IoSession.class).toProvider(MinaSessionProvider.class);

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

	}

	/**
	 * Binds a filter in the filter chain.  You can specify the name of the filter to be handed to
     * to the {@link IoFilterChain#addLast(String, IoFilter)} method
	 * 
	 * @return a FilterBinding 
	 * 
	 */
	protected final FilterBinding bindFilter() {
		return new FilterBinding() {
			
			@Override
			public LinkedBindingBuilder<IoFilter> named(final String name) {
				return filters.addBinding(name);
			}

		};
	}

	/**
	 * Automatically binds the {@link GuiceProtocolCodecFilter} to the context
	 * configuration.  You may bind it yourself, or call it manually.  It is
	 * not strictly necessary to bind this, but it makes life easier.
	 * 
	 * @return an instance of {@link ScopedBindingBuilder}
	 * 
	 */
	protected final ScopedBindingBuilder bindProtcolCodecFilter() {
		final String name = GuiceProtocolCodecFilter.class.toString();
		return bindFilter().named(name).to(GuiceProtocolCodecFilter.class);
	}

	/**
	 * Binds the {@link GuiceProtocolCodecFactory} which will use Guice to
	 * install the various protocol factories.
	 * 
	 * @return and instance of {@ AnnotatedBindingBuilder<? estends ProtocolCodecFactory>}
	 */
	protected final AnnotatedBindingBuilder<? extends ProtocolCodecFactory> bindProtocolCodecFactory() {
		return binder().bind(GuiceProtocolCodecFactory.class);
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
				MinaSessionProvider.setSession(session);
				return invocation.proceed();
			}finally {
				MinaSessionProvider.freeSession(session);
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
				MinaSessionProvider.setSession(session);
				return invocation.proceed();
			} finally {
				MinaSessionProvider.freeSession(session);
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
