// GPars - Groovy Parallel Systems
//
// Copyright © 2008-10  The original author or authors
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

package groovyx.gpars.dataflow.operator

import groovyx.gpars.group.PGroup
import java.util.concurrent.Semaphore

/**
 * Dataflow selectors and operators (processors) form the basic units in dataflow networks. They are typically combined into oriented graphs that transform data.
 * They accept a set of input and output dataflow channels so that once values are available to be consumed in any
 * of the input channels the selector's body is triggered on the values, potentially generating values to be written into the output channels.
 * The output channels at the same time are suitable to be used as input channels by some other dataflow processors.
 * The channels allow processors to communicate.
 *
 * Dataflow selectors and operators enable creation of highly concurrent applications yet the abstraction hides the low-level concurrency primitives
 * and exposes much friendlier API.
 * Since selectors and operators internally leverage the actor implementation, they reuse a pool of threads and so the actual number of threads
 * used by the calculation can be kept much lower than the actual number of processors used in the network.
 *
 * @author Vaclav Pech
 * Date: Sep 9, 2009
 */
public final class DataFlowSelector extends DataFlowProcessor {

    /**
     * Creates a selector
     * After creation the selector needs to be started using the start() method.
     * @param channels A map specifying "inputs" and "outputs" - dataflow channels (instances of the DataFlowStream or DataFlowVariable classes) to use for inputs and outputs
     * @param code The selector's body to run each time all inputs have a value to read
     */
    protected def DataFlowSelector(final PGroup group, final Map channels, final Closure code) {
        super(group, channels, code)
        final int parameters = code.maximumNumberOfParameters
        if (verifyChannelParameters(channels, parameters))
            throw new IllegalArgumentException("The selector's body must accept 1 or two parameters, while it currently requests ${parameters} parameters.")
        if (shouldBeMultiThreaded(channels)) {
            if (channels.maxForks < 1) throw new IllegalArgumentException("The maxForks argument must be a positive value. ${channels.maxForks} was provided.")
            this.actor = new ForkingDataFlowSelectorActor(this, group, channels.outputs?.asImmutable(), channels.inputs.asImmutable(), code.clone(), channels.maxForks)
        } else {
            this.actor = new DataFlowSelectorActor(this, group, channels.outputs?.asImmutable(), channels.inputs.asImmutable(), code.clone())
        }
    }

    private boolean verifyChannelParameters(Map channels, int parameters) {
        return !channels || (channels.inputs == null) || !(parameters in [1, 2])
    }

    /**
     * Is invoked in case the actor throws an exception.
     */
    protected void reportError(Throwable e) {
        System.err.println "The dataflow selector experienced an exception and is about to terminate. $e"
        stop()
    }
}

/**
 * An selector's internal actor. Repeatedly polls inputs and once they're all available it performs the selector's body.
 *
 * Iteratively waits for enough values from inputs.
 * Once all required inputs are available (received as messages), the selector's body is run.
 */
private class DataFlowSelectorActor extends DataFlowProcessorActor {
    protected final boolean passIndex = false

    def DataFlowSelectorActor(owningOperator, group, outputs, inputs, code) {
        super(owningOperator, group, outputs, inputs, code)
        if (code.maximumNumberOfParameters == 2) {
            passIndex = true
        }
    }

    final void onMessage(def message) {
        final def index = message.attachment
        startTask(index, message.result)
        inputs[index].getValAsync(index, this)
    }

    def startTask(index, result) {
        try {
            if (passIndex) {
                code.call(result, index)
            } else {
                code.call(result)
            }
        } catch (Throwable e) {
            reportException(e)
        }
    }
}

/**
 * An selector's internal actor. Repeatedly polls inputs and once they're all available it performs the selector's body.
 * The selector's body is executed in as a separate task, allowing multiple copies of the body to be run concurrently.
 * The maxForks property guards the maximum number or concurrently run copies.
 */
private final class ForkingDataFlowSelectorActor extends DataFlowSelectorActor {
    private final Semaphore semaphore
    private final def threadPool

    def ForkingDataFlowSelectorActor(owningOperator, group, outputs, inputs, code, maxForks) {
        super(owningOperator, group, outputs, inputs, code)
        this.semaphore = new Semaphore(maxForks)
        this.threadPool = group.threadPool
    }

    def startTask(index, result) {
        semaphore.acquire()
        threadPool.execute {
            try {
                super.startTask(index, result)
            } catch (Throwable e) {
                reportException(e)
            } finally {
                semaphore.release()
            }
        }
    }
}
