// GPars - Groovy Parallel Systems
//
// Copyright © 2014 The original author or authors
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

package groovyx.gpars.remote.netty

import groovyx.gpars.remote.LocalHost

class NettyClientServerTest extends GroovyTestCase implements NettyTest {
    def static LocalHost LOCALHOST = new LocalHost()

    public void testConnectionLocal() {
        NettyServer server = new NettyServer(LOCALHOST, LOCALHOST_ADDRESS, LOCALHOST_PORT)
        server.start()
        server.channelFuture.sync()

        NettyClient client = new NettyClient(LOCALHOST, LOCALHOST_ADDRESS, LOCALHOST_PORT)
        client.start()
        client.channelFuture.sync()

        assert client.channelFuture.isSuccess()

        client.stop()
        server.stop()
    }
}
