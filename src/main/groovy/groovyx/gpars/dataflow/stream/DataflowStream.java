// GPars - Groovy Parallel Systems
//
// Copyright © 2008-11  The original author or authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package groovyx.gpars.dataflow.stream;

import groovy.lang.Closure;
import groovyx.gpars.actor.impl.MessageStream;
import groovyx.gpars.dataflow.DataflowVariable;

import java.util.Collection;

/**
 * Represents a deterministic dataflow channel. Unlike a DataflowQueue, DataflowStream allows multiple readers each to read all the messages.
 * Essentially, you may think of DataflowStream as a 1 to many communication channel, since when a reader consumes a messages,
 * other readers will still be able to read the message. Also, all messages arrive to all readers in the same order.
 * DataflowStream is implemented as a functional queue, which impacts the API in that users have to traverse the values in the stream themselves.
 * On the other hand in offers handy methods for value filtering or transformation together with interesting performance characteristics.
 * For convenience and for the ability to use DataflowStream with other dataflow constructs, like e.g. operators,
 * you can wrap DataflowStreams with DataflowReadAdapter for read access or DataflowWriteAdapter for write access.
 * <p/>
 * The DataflowStream class is designed for single-threaded producers and consumers. If multiple threads are supposed to read or write values
 * to the stream, their access to the stream must be serialized externally or the adapters should be used.
 *
 * @param <T> Type for values to pass through the stream
 * @author Johannes Link, Vaclav Pech
 */
@SuppressWarnings({"rawtypes", "TailRecursion", "unchecked", "StaticMethodNamingConvention", "ClassWithTooManyMethods"})
public final class DataflowStream<T> extends StreamCore<T> {

    public DataflowStream() {
        super(new DataflowVariable<T>());
    }

    public DataflowStream(final Closure toBeApplied) {
        super(new DataflowVariable<T>(), toBeApplied);
    }

    public DataflowStream(final Collection<MessageStream> wheneverBoundListeners) {
        super(new DataflowVariable<T>(), wheneverBoundListeners);
    }

    @Override
    public FList<T> getRest() {
        if (rest.get() == null)
            rest.compareAndSet(null, new DataflowStream<T>(wheneverBoundListeners));
        return rest.get();
    }

    @Override
    protected StreamCore<T> createNewStream() {
        return new DataflowStream<T>();
    }

    @Override
    public String toString() {
        if (!first.isBound())
            return "DataflowStream[?]";
        if (isEmpty())
            return "DataflowStream[]";
        return "DataflowStream[" + getFirst() + getRest().appendingString() + ']';
    }
}

