package org.apache.mina.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.example.gettingstarted.timeserver.TimeServerHandler;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.textline.TextLineDecoder;
import org.apache.mina.filter.codec.textline.TextLineEncoder;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.guice.filter.InjectProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.net.InetSocketAddress;

/**
 * A remake of the TimeServer example using Guice.
 *
 * Original example here: https://mina.apache.org/mina-project/userguide/ch2-basics/sample-tcp-server.html
 *
 * Created by patricktwohig on 8/28/15.
 */
public class GuicyTimeServer {

    private static final int PORT = 9123;

    public static void main( final String[] args )  throws Exception {

        // To actually start up our server, we need to get an instance of Injector.
        final Injector injector = Guice.createInjector(new GuicyTimeServerModule());

        // Then we get our instance of acceptor to actuall give us the acceptor
        // we wish to use.

        final IoAcceptor ioAcceptor = injector.getInstance(IoAcceptor.class);

        // At this point, we need not configured anything else.  This should be configured
        // internally in  the Guice module.
        ioAcceptor.bind(new InetSocketAddress(PORT));

    }

    private static class GuicyTimeServerModule extends MinaModule {

        @Override
        protected void configureMINA() {

            // There are many ways you can get the instance of the IoAcceptor, however
            // this is here to show simplicity.  Other options such as using a Provider
            // and reading from system properties would be equally acceptable.

            bind(IoAcceptor.class).to(NioSocketAcceptor.class);

            // We want to follow as closely as possible to the original example, so let's
            // set up a logging filter.

            bindFilter().named("logging").atAndOfFilterChain().to(LoggingFilter.class);
            bindFilter().named("codec").atAndOfFilterChain().to(InjectProtocolCodecFilter.class);

            // This ensures that the protocol codec factory is install and managed by guice
            // as well as the protocol codec filter.

            bindFilterChainBuilder();
            bindProtocolCodecFactory();
            bind(ProtocolEncoder.class).to(TextLineEncoder.class);
            bind(ProtocolDecoder.class).to(TextLineDecoder.class);

            // Lastly, the most important part.  We need to bind the IoHandler instance to the
            // time server handler class.

            bind(IoHandler.class).to(TimeServerHandler.class);

            // Ensures that the application has bound the IoSession to the scope.
            bindIoSession();

        }

    }

}
