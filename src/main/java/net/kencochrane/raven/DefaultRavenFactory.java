package net.kencochrane.raven;

import net.kencochrane.raven.connection.AsyncConnection;
import net.kencochrane.raven.connection.Connection;
import net.kencochrane.raven.connection.HttpConnection;
import net.kencochrane.raven.dsn.Dsn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultRavenFactory {
    //TODO: Add support for tags set by default
    /**
     * Protocol setting to disable security checks over an SSL connection.
     */
    public static final String NAIVE_PROTOCOL = "naive";
    /**
     * Option specific to raven-java, allowing to enable/disable the compression of requests to the Sentry Server.
     */
    public static final String COMPRESSION_OPTION = "raven.compression";
    /**
     * Option specific to raven-java, allowing to set a timeout (in ms) for a request to the Sentry server.
     */
    public static final String TIMEOUT_OPTION = "raven.timeout";
    /**
     * Option to send events asynchronously.
     */
    public static final String ASYNC_OPTION = "raven.async";
    /**
     * Option to disable the graceful shutdown.
     */
    public static final String GRACEFUL_SHUTDOWN_OPTION = "raven.async.gracefulshutdown";
    /**
     * Option for the number of threads assigned for the connection.
     */
    public static final String MAX_THREADS_OPTION = "raven.async.threads";
    /**
     * Option for the priority of threads assigned for the connection.
     */
    public static final String PRIORITY_OPTION = "raven.async.priority";
    /**
     * Option for the maximum size of the queue.
     */
    public static final String QUEUE_SIZE_OPTION = "raven.async.queuesize";
    /**
     * Option to hide common stackframes with enclosing exceptions.
     */
    private static final Logger logger = LoggerFactory.getLogger(DefaultRavenFactory.class);
    private static final String FALSE = Boolean.FALSE.toString();

    public static Raven createRavenInstance(Dsn dsn) {
        Raven raven = new Raven();
        raven.setConnection(createConnection(dsn));
        return raven;
    }

    /**
     * Creates a connection to the given DSN by determining the protocol.
     *
     * @param dsn Data Source Name of the Sentry server to use.
     * @return a connection to the server.
     */
    protected static Connection createConnection(Dsn dsn) {
        String protocol = dsn.getProtocol();
        Connection connection;

        if (protocol.equalsIgnoreCase("http") || protocol.equalsIgnoreCase("https")) {
            logger.info("Using an HTTP connection to Sentry.");
            connection = createHttpConnection(dsn);
        } else {
            throw new IllegalStateException("Couldn't create a connection for the protocol '" + protocol + "'");
        }

        // Enable async unless its value is 'false'.
        if (!FALSE.equalsIgnoreCase(dsn.getOptions().get(ASYNC_OPTION))) {
            connection = createAsyncConnection(dsn, connection);
        }

        return connection;
    }

    /**
     * Encapsulates an already existing connection in an {@link net.kencochrane.raven.connection.AsyncConnection} and get the async options from the
     * Sentry DSN.
     *
     * @param dsn        Data Source Name of the Sentry server.
     * @param connection Connection to encapsulate in an {@link net.kencochrane.raven.connection.AsyncConnection}.
     * @return the asynchronous connection.
     */
    protected static Connection createAsyncConnection(Dsn dsn, Connection connection) {

        int maxThreads;
        if (dsn.getOptions().containsKey(MAX_THREADS_OPTION)) {
            maxThreads = Integer.parseInt(dsn.getOptions().get(MAX_THREADS_OPTION));
        } else {
            maxThreads = Runtime.getRuntime().availableProcessors();
        }

        int priority;
        if (dsn.getOptions().containsKey(PRIORITY_OPTION)) {
            priority = Integer.parseInt(dsn.getOptions().get(PRIORITY_OPTION));
        } else {
            priority = Thread.MIN_PRIORITY;
        }

        BlockingDeque<Runnable> queue;
        if (dsn.getOptions().containsKey(QUEUE_SIZE_OPTION)) {
            queue = new LinkedBlockingDeque<>(Integer.parseInt(dsn.getOptions().get(QUEUE_SIZE_OPTION)));
        } else {
            queue = new LinkedBlockingDeque<>();
        }

        ExecutorService executorService = new ThreadPoolExecutor(
                maxThreads, maxThreads, 0L, TimeUnit.MILLISECONDS, queue,
                new DaemonThreadFactory(priority), new ThreadPoolExecutor.DiscardOldestPolicy());

        boolean gracefulShutdown = !FALSE.equalsIgnoreCase(dsn.getOptions().get(GRACEFUL_SHUTDOWN_OPTION));

        return new AsyncConnection(connection, executorService, gracefulShutdown);
    }

    /**
     * Creates an HTTP connection to the Sentry server.
     *
     * @param dsn Data Source Name of the Sentry server.
     * @return an {@link net.kencochrane.raven.connection.HttpConnection} to the server.
     */
    protected static Connection createHttpConnection(Dsn dsn) {
        URL sentryApiUrl = HttpConnection.getSentryApiUrl(dsn.getUri(), dsn.getProjectId());
        HttpConnection httpConnection = new HttpConnection(
                sentryApiUrl,
                dsn.getPublicKey(),
                dsn.getSecretKey());

        // Set the naive mode
        httpConnection.setBypassSecurity(dsn.getProtocolSettings().contains(NAIVE_PROTOCOL));
        // Set the HTTP timeout
        if (dsn.getOptions().containsKey(TIMEOUT_OPTION))
            httpConnection.setTimeout(Integer.parseInt(dsn.getOptions().get(TIMEOUT_OPTION)));
        return httpConnection;
    }

    /**
     * Thread factory generating daemon threads with a custom priority.
     * <p>
     * Those (usually) low priority threads will allow to send event details to sentry concurrently without slowing
     * down the main application.
     */
    @SuppressWarnings("PMD.AvoidThreadGroup")
    protected static final class DaemonThreadFactory implements ThreadFactory {
        private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        private final int priority;

        private DaemonThreadFactory(int priority) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "raven-pool-" + POOL_NUMBER.getAndIncrement() + "-thread-";
            this.priority = priority;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (!t.isDaemon())
                t.setDaemon(true);
            if (t.getPriority() != priority)
                t.setPriority(priority);
            return t;
        }
    }
}
