// GPars - Groovy Parallel Systems
//
// Copyright © 2014  The original author or authors
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

package groovyx.gpars.remote.netty.discovery

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.socket.DatagramPacket
import io.netty.util.CharsetUtil
import spock.lang.Specification

class DiscoveryRequestDecoderTest extends Specification {
    def "Decode"() {
        setup:
        def decoder = new DiscoveryRequestDecoder()
        def channelHandlerContext = Mock(ChannelHandlerContext)
        def testUrl = "test-group/test-actor"
        def packet = new DatagramPacket(Unpooled.copiedBuffer(testUrl, CharsetUtil.UTF_8), new InetSocketAddress(1234))
        def resultList = []

        when:
        decoder.decode channelHandlerContext, packet, resultList

        then:
        resultList.size() == 1

        def request = resultList.get(0) as DiscoveryRequestWithSender
        request.getRequest().getActorUrl() == testUrl
    }
}
