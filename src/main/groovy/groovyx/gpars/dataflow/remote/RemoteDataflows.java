package groovyx.gpars.dataflow.remote;

import groovyx.gpars.dataflow.DataflowBroadcast;
import groovyx.gpars.dataflow.DataflowQueue;
import groovyx.gpars.dataflow.DataflowReadChannel;
import groovyx.gpars.dataflow.DataflowVariable;
import groovyx.gpars.dataflow.stream.DataflowStreamWriteAdapter;
import groovyx.gpars.remote.LocalHost;
import groovyx.gpars.remote.netty.NettyTransportProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public final class RemoteDataflows {
    private static Map<String, DataflowVariable<?>> publishedVariables = new ConcurrentHashMap<>();

    private static Map<String, DataflowVariable<?>> remoteVariables = new ConcurrentHashMap<>();

    private static Map<String, DataflowBroadcast> publishedBroadcasts = new ConcurrentHashMap<>();

    private static Map<String, DataflowVariable<RemoteDataflowBroadcast>> remoteBroadcasts = new ConcurrentHashMap<>();

    private static Map<String, DataflowQueue<?>> publishedQueues = new ConcurrentHashMap<>();

    private static Map<String, DataflowVariable<RemoteDataflowQueue<?>>> remoteQueues = new ConcurrentHashMap<>();

    private static LocalHost clientLocalHost = new LocalHost(); // TODO what about server?

    private RemoteDataflows() {}

    /**
     * Publishes {@link groovyx.gpars.dataflow.DataflowVariable} under chosen name.
     * @param variable the variable to be published
     * @param name the name under which variable is published
     * @param <T> type of variable
     */
    public static <T> void publish(DataflowVariable<T> variable, String name) {
        publishedVariables.put(name, variable);
    }

    /**
     * Retrieves {@link groovyx.gpars.dataflow.DataflowVariable} published under specified name (locally).
     * @param name the name under which variable was published
     * @return the variable registered under specified name or <code>null</code> if none variable is registered under that name
     */
    public static DataflowVariable<?> get(String name) {
        return publishedVariables.get(name);
    }

    /**
     * Retrieves {@link groovyx.gpars.dataflow.DataflowVariable} published under specified name on remote host.
     * @param host the address of remote host
     * @param port the the port of remote host
     * @param name the name under which variable was published
     * @return future of {@link groovyx.gpars.dataflow.remote.RemoteDataflowVariable}
     * @see groovyx.gpars.dataflow.remote.RemoteDataflowVariableFuture
     */
    public static Future<DataflowVariable> get(String host, int port, String name) {
        // TODO wrong use of concurent map
        clientLocalHost.setRemoteDataflowsRegistry(remoteVariables);

        DataflowVariable remoteVariable = remoteVariables.get(name);
        if (remoteVariable == null) {
            remoteVariable = new DataflowVariable<>();
            remoteVariables.put(name, remoteVariable);
            NettyTransportProvider.getDataflowVariable(host, port, name, clientLocalHost);
        }
        return new RemoteDataflowVariableFuture(remoteVariable);
    }

    public static void publish(DataflowBroadcast broadcastStream, String name) {
        publishedBroadcasts.put(name, broadcastStream);
    }

    public static DataflowBroadcast getBroadcastStream(String name) {
        return publishedBroadcasts.get(name);

    }

    public static Future<DataflowReadChannel> getReadChannel(String host, int port, String name) {
        // TODO wrong use of concurent map
        NettyTransportProvider.setRemoteBroadcastsRegistry(remoteBroadcasts);

        DataflowVariable<RemoteDataflowBroadcast> remoteStreamVariable = remoteBroadcasts.get(name);
        if (remoteStreamVariable == null) {
            remoteStreamVariable = new DataflowVariable<>();
            remoteBroadcasts.put(name, remoteStreamVariable);
            NettyTransportProvider.getDataflowReadChannel(host, port, name);
        }

        return new RemoteDataflowReadChannelFuture(remoteStreamVariable);
    }

    public static DataflowQueue<?> getDataflowQueue(String name) {
        return publishedQueues.get(name);
    }

    public static void publish(DataflowQueue<?> queue, String name) {
        publishedQueues.put(name, queue);
    }

    public static Future<RemoteDataflowQueue<?>> getDataflowQueue(String host, int port, String name) {
        // TODO wrong use of concurent map
        clientLocalHost.setRemoteDataflowQueueRegistry(remoteQueues);

        DataflowVariable<RemoteDataflowQueue<?>> remoteQueueVariable = remoteQueues.get(name);
        if (remoteQueueVariable == null) {
            remoteQueueVariable = new DataflowVariable<>();
            remoteQueues.put(name, remoteQueueVariable);
            NettyTransportProvider.getDataflowQueue(host, port, name, clientLocalHost);
        }

        return new RemoteDataflowQueueFuture(remoteQueueVariable);
    }
}
