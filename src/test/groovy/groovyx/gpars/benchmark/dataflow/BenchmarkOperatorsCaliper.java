// GPars - Groovy Parallel Systems
//
// Copyright © 2008-2012  The original author or authors
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

package groovyx.gpars.benchmark.dataflow;

import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import groovy.lang.Script;

public class BenchmarkOperatorsCaliper extends SimpleBenchmark {
     private Script script;
    @Override protected void setUp() throws Exception{
        script = new BenchmarkOperators();
    }
    public void timeOperator(int reps) {
        for (int i = 0; i < reps; i++) {
                script.run();
        }

    }
}
